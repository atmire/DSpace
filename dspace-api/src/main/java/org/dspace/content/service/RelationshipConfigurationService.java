/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.RelationshipTypeConfiguration;
import org.dspace.content.authority.ItemAuthorityExtraMetadataGenerator;
import org.dspace.core.Context;

/**
 * Resolves and validates the configured semantics of configuration-backed relationships.
 *
 * Relationship behavior is defined in configuration rather than stored as a
 * {@link org.dspace.content.RelationshipType} database entity. A persisted {@link org.dspace.content.Relationship}
 * may store a configuration key identifying which configured semantics apply to that relationship.
 *
 * Implementations may also provide compatibility mappings from existing authority configuration, for example while
 * migrating existing CRIS authority-backed relationships to the configuration-backed relationship model.
 */
public interface RelationshipConfigurationService {

    /**
     * Resolves the relationship configuration applicable to the supplied metadata value.
     *
     * The metadata field is used to determine whether it participates in a configured relationship. If no
     * relationship configuration applies to the metadata value, this method returns {@code null}.
     *
     * @param context       current DSpace context
     * @param value metadata value for which relationship configuration should be resolved
     * @return the matching relationship configuration, or {@code null} if the metadata value is not mapped to a
     * configured relationship
     * @throws IllegalArgumentException if multiple configurations match the metadata field
     * @throws SQLException if an error occurs while accessing the metadata registry
     */
    RelationshipTypeConfiguration findForMetadataValue(Context context, MetadataValue value) throws SQLException;

    /**
     * Resolves a relationship configuration by its stable configuration key.
     *
     * The key identifies configured relationship semantics; it is not itself a persisted relationship type definition.
     *
     * @param context       current DSpace context
     * @param key stable identifier of the relationship configuration
     * @return the relationship configuration identified by the supplied key
     * @throws IllegalArgumentException if the key is missing, unknown or ambiguous
     * @throws SQLException if an error occurs while accessing the metadata registry
     */
    RelationshipTypeConfiguration getByKey(Context context, String key) throws SQLException;

    /**
     * Validates that the supplied Items can be used as the left and right endpoints of a relationship using the given
     * configuration.
     *
     * Both Items must be archived. Validation is based on the endpoint requirements defined by the relationship
     * configuration, such as the expected entity types for the left and right Items.
     * This method validates the configured relationship semantics; authorization to create or modify the
     * Relationship is handled separately by the relationship service.
     *
     * @param context       current DSpace context
     * @param configuration relationship configuration to validate against
     * @param left          proposed left endpoint of the Relationship
     * @param right         proposed right endpoint of the Relationship
     * @throws IllegalArgumentException if either endpoint does not satisfy the configured relationship requirements
     * @throws SQLException             if a database error occurs during validation
     */
    void validate(Context context, RelationshipTypeConfiguration configuration, Item left, Item right)
        throws SQLException;

    /**
     * Validates the relationship endpoints using the given configuration, optionally allowing unarchived Items.
     *
     * When {@code allowUnarchivedItems} is true, the archived-state requirement is skipped for both Items.
     * All other configured endpoint requirements, including entity types, are still validated.
     * Authorization is handled separately by the relationship service.
     *
     * @param context              current DSpace context
     * @param configuration        relationship configuration to validate against
     * @param left                 proposed left endpoint of the Relationship
     * @param right                proposed right endpoint of the Relationship
     * @param allowUnarchivedItems whether to allow unarchived Items during validation
     * @throws IllegalArgumentException if either endpoint does not satisfy the configured relationship requirements
     * @throws SQLException             if a database error occurs during validation
     */
    void validate(Context context, RelationshipTypeConfiguration configuration, Item left, Item right,
                  boolean allowUnarchivedItems) throws SQLException;

    /**
     * Returns the primary metadata field configured for the specified relationship endpoint.
     * A relationship endpoint may have no primary metadata field configured.
     *
     * @param context          current DSpace context
     * @param configurationKey stable identifier of the relationship configuration
     * @param left             whether to retrieve the field for the left endpoint ({@code true})
     *                         or the right endpoint ({@code false})
     * @return the configured primary metadata field, or {@link Optional#empty()} if none is configured
     * @throws IllegalArgumentException if the relationship configuration key is missing, unknown or ambiguous
     * @throws SQLException             if an error occurs while accessing the metadata registry
     */
    Optional<String> getPrimaryMetadataField(Context context, String configurationKey, boolean left)
        throws SQLException;

    /**
     * Checks whether the supplied metadata field is the primary metadata field
     * configured for the specified relationship endpoint.
     *
     * @param context          current DSpace context
     * @param configurationKey stable identifier of the relationship configuration
     * @param left             whether to check the left endpoint ({@code true})
     *                         or the right endpoint ({@code false})
     * @param metadataField    metadata field to check, in schema.element.qualifier format
     * @return {@code true} if the metadata field is primary on the specified endpoint,
     * {@code false} otherwise
     * @throws IllegalArgumentException if the relationship configuration key is missing, unknown or ambiguous
     * @throws SQLException             if an error occurs while accessing the metadata registry
     */
    boolean isPrimaryMetadata(Context context, String configurationKey, boolean left, String metadataField)
        throws SQLException;

    /**
     * Checks whether the supplied metadata field is configured as additional
     * relationship metadata on the specified endpoint.
     *
     * Additional metadata fields are derived from the keys of the metadata generator
     * map configured for that endpoint.
     *
     * @param context          current DSpace context
     * @param configurationKey stable identifier of the relationship configuration
     * @param left             whether to check the left endpoint ({@code true})
     *                         or the right endpoint ({@code false})
     * @param metadataField    metadata field to check, in schema.element.qualifier format
     * @return {@code true} if the metadata field is additional on the specified endpoint,
     * {@code false} otherwise
     * @throws IllegalArgumentException if the relationship configuration key is missing, unknown or ambiguous
     * @throws SQLException             if an error occurs while accessing the metadata registry
     */
    boolean isAdditionalMetadata(Context context, String configurationKey, boolean left, String metadataField)
        throws SQLException;

    /**
     * Returns the additional metadata fields configured for the specified relationship endpoint.
     *
     * The fields are derived from the keys of the metadata generator map configured
     * for that endpoint.
     *
     * @param context          current DSpace context
     * @param configurationKey stable identifier of the relationship configuration
     * @param left             whether to retrieve fields for the left endpoint ({@code true})
     *                         or the right endpoint ({@code false})
     * @return the configured additional metadata fields, or an empty set if none are configured
     * @throws IllegalArgumentException if the relationship configuration key is missing, unknown or ambiguous
     * @throws SQLException             if an error occurs while accessing the metadata registry
     */
    Set<String> getAdditionalMetadataFields(Context context, String configurationKey, boolean left) throws SQLException;

    /**
     * Resolves the metadata generator configured for an additional metadata field
     * on the specified relationship endpoint.
     *
     * @param context          current DSpace context
     * @param configurationKey stable identifier of the relationship configuration
     * @param left             whether to retrieve the generator for the left endpoint ({@code true})
     *                         or the right endpoint ({@code false})
     * @param metadataField    additional metadata field for which to retrieve the generator,
     *                         in schema.element.qualifier format
     * @return the configured metadata generator, or {@link Optional#empty()} if none is configured
     * for the supplied field and endpoint
     * @throws IllegalArgumentException if the relationship configuration key is missing, unknown or ambiguous
     * @throws SQLException             if an error occurs while accessing the metadata registry
     */
    Optional<ItemAuthorityExtraMetadataGenerator> getMetadataGenerator(Context context, String configurationKey,
                                                                       boolean left, String metadataField)
        throws SQLException;
}
