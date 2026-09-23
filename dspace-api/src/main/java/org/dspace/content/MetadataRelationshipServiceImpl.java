/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.audit.MetadataEvent;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.authority.Choices;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataRelationshipService;
import org.dspace.content.service.MetadataValueService;
import org.dspace.content.service.RelationshipConfigurationService;
import org.dspace.content.service.RelationshipService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

public class MetadataRelationshipServiceImpl implements MetadataRelationshipService {

    private static final Logger log = LogManager.getLogger(MetadataRelationshipServiceImpl.class);

    @Autowired
    protected AuthorizeService authorizeService;
    @Autowired
    private RelationshipService relationshipService;
    @Autowired
    private MetadataValueService metadataValueService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private RelationshipConfigurationService relationshipConfigurationService;

    @Override
    public Relationship createInternalRelationship(Context context, Item ownerItem, MetadataValue metadataValue,
                                                   Item targetItem) throws SQLException, AuthorizeException {
        requireOwner(metadataValue, ownerItem);

        authorizeService.authorizeAction(context, ownerItem, Constants.WRITE);
        authorizeService.authorizeAction(context, targetItem, Constants.READ);

        RelationshipTypeConfiguration configuration =
            relationshipConfigurationService.findForMetadataValue(metadataValue);
        if (configuration == null) {
            return null;
        }

        boolean ownerOnLeft =
            configuration.isOwnerOnLeft(RelationshipConfigurationServiceImpl.fieldName(metadataValue));
        Item left = ownerOnLeft ? ownerItem : targetItem;
        Item right = ownerOnLeft ? targetItem : ownerItem;

        relationshipConfigurationService.validate(context, configuration, left, right);

        Relationship relationship = relationshipService.createConfigBackedRelationship(
            context, left, right, configuration.getId());
        validateRelationshipOwner(metadataValue, relationship);
        metadataValue.setRelationship(relationship);
        metadataValueService.update(context, metadataValue);
        itemService.update(context, ownerItem);
        log.debug("Linked metadata {} to relationship {} using configuration {}",
                  metadataValue.getID(), relationship.getID(), configuration.getId());
        return relationship;
    }

    @Override
    public Relationship promoteToInternalRelationship(Context context, Item ownerItem, MetadataValue metadataValue,
                                                      Item targetItem) throws SQLException, AuthorizeException {
        if (metadataValue == null || targetItem == null) {
            return null;
        }

        requireOwner(metadataValue, ownerItem);

        if (!ownerItem.isArchived() || !targetItem.isArchived() ||
            metadataValue.getConfidence() == Choices.CF_REJECTED) {
            return null;
        }

        Relationship existing = metadataValue.getRelationship();
        if (existing != null) {
            // Never silently accept a stale or different authority as idempotency.
            if (!opposite(existing, ownerItem).getID().equals(targetItem.getID())) {
                throw new IllegalArgumentException(
                    "Use replaceTarget to change an existing relationship target");
            }
            validateAuthority(metadataValue, existing);
            return existing;
        }

        // Validate before persistence, so invalid input does not leave a half-created link.
        validateAuthorityTarget(metadataValue, targetItem);

        return createInternalRelationship(
            context,
            ownerItem,
            metadataValue,
            targetItem
        );
    }

    @Override
    public void attachMetadata(Context context, Relationship relationship, MetadataValue metadataValue)
        throws SQLException, AuthorizeException {
        // TO BE IMPLEMENTED LATER
    }

    @Override
    public Relationship replaceTarget(Context context, Relationship relationship, Item newTarget)
        throws SQLException, AuthorizeException {
        // TO BE IMPLEMENTED LATER
        return null;
    }

    @Override
    public void detachRelationship(Context context, MetadataValue metadataValue)
        throws SQLException, AuthorizeException {

        Relationship relationship = metadataValue.getRelationship();
        if (relationship == null) {
            return;
        }

        requireConfiguredRelationship(relationship);

        List<MetadataValue> values = metadataValueService.findByRelationship(context, relationship);

        authorizeProjections(context, values);

        for (MetadataValue value : values) {
            value.setRelationship(null);
            if (isUuid(value.getAuthority())) {
                value.setAuthority(null);
            }

            // Explicit detachment is not an invitation to resolve again at next update.
            value.setConfidence(Choices.CF_REJECTED);

            itemService.update(context, (Item) value.getDSpaceObject());
            metadataValueService.update(context, value);
        }

        relationshipService.delete(context, relationship);

    }

    @Override
    public void removeMetadataValue(Context context, MetadataValue metadataValue)
        throws SQLException, AuthorizeException {
        // TO BE IMPLEMENTED LATER
    }

    @Override
    public void removeRelationship(Context context, Relationship relationship) throws SQLException, AuthorizeException {
        requireConfiguredRelationship(relationship);

        List<MetadataValue> values = metadataValueService.findByRelationship(context, relationship);

        authorizeProjections(context, values);

        // Relationship deletion checks write access even if there are no projections.
        assertWriteOnRelationship(context, relationship);

        for (MetadataValue value : values) {
            deleteProjection(context, value);
        }

        relationshipService.delete(context, relationship);
    }

    @Override
    public List<MetadataValue> findMetadataByRelationship(Context context, Relationship relationship)
        throws SQLException {
        return metadataValueService.findByRelationship(context, relationship);
    }

    @Override
    public boolean isRelationshipBacked(MetadataValue metadataValue) {
        return metadataValue.isRelationshipBacked();
    }

    private void deleteProjection(Context context, MetadataValue value)
        throws SQLException, AuthorizeException {

        DSpaceObject owner = value.getDSpaceObject();
        owner.addMetadataEventDetails(new MetadataEvent(value, MetadataEvent.REMOVE));

        value.setRelationship(null);
        owner.getMetadata().remove(value);
        metadataValueService.delete(context, value);
    }

    private void authorizeProjections(Context context, List<MetadataValue> values)
        throws SQLException, AuthorizeException {
        // Validate all permissions before the first mutation, including the other side.
        for (MetadataValue value : values) {
            authorizeService.authorizeAction(context, value.getDSpaceObject(), Constants.WRITE);
        }
    }

    private void assertWriteOnRelationship(Context context, Relationship relationship)
        throws SQLException, AuthorizeException {
        if (!authorizeService.authorizeActionBoolean(context, relationship.getLeftItem(), Constants.WRITE)
            && !authorizeService.authorizeActionBoolean(context, relationship.getRightItem(), Constants.WRITE)) {
            throw new AuthorizeException("Write access to a relationship endpoint is required");
        }
    }

    private void requireOwner(MetadataValue value, Item owner) {
        if (owner == null || value.getDSpaceObject() == null
            || !owner.getID().equals(value.getDSpaceObject().getID())) {
            throw new IllegalArgumentException("Metadata does not belong to the supplied owner");
        }
    }

    private void requirePersistentRelationship(Relationship relationship) {
        if (relationship == null || relationship.getID() == null || relationship.getID() <= 0) {
            throw new IllegalArgumentException("A persistent relationship is required");
        }
    }

    private void requireConfiguredRelationship(Relationship relationship) {
        requirePersistentRelationship(relationship);
        if (!relationship.isConfigurationBacked()) {
            throw new IllegalArgumentException("This operation requires a configuration-backed relationship");
        }
    }

    private Item opposite(Relationship relationship, DSpaceObject owner) {
        if (owner == null) {
            throw new IllegalArgumentException("A metadata owner is required");
        }
        if (owner.getID().equals(relationship.getLeftItem().getID())) {
            return relationship.getRightItem();
        }
        if (owner.getID().equals(relationship.getRightItem().getID())) {
            return relationship.getLeftItem();
        }
        throw new IllegalArgumentException("Metadata owner must be an endpoint of its relationship");
    }

    private void validateAuthority(MetadataValue value, Relationship relationship) {
        validateAuthorityTarget(value, opposite(relationship, value.getDSpaceObject()));
    }

    private void validateAuthorityTarget(MetadataValue value, Item target) {
        if (isUuid(value.getAuthority()) && !UUID.fromString(value.getAuthority()).equals(target.getID())) {
            throw new IllegalArgumentException("UUID authority cache disagrees with the durable relationship");
        }
    }

    private boolean isUuid(String value) {
        if (value == null || value.length() != 36) {
            return false;
        }
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void validateRelationshipOwner(MetadataValue metadataValue, Relationship relationship) {
        UUID owner = metadataValue.getDSpaceObject().getID();
        if (!owner.equals(relationship.getLeftItem().getID())
            && !owner.equals(relationship.getRightItem().getID())) {
            throw new IllegalArgumentException("Metadata owner must be an endpoint of its relationship");
        }
    }
}
