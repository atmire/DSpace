/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import org.dspace.content.authority.ItemAuthorityExtraMetadataGenerator;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.content.service.RelationshipConfigurationService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * POC bridge: keep existing CRIS authority configuration intact. Explicit Spring
 * definitions take precedence. The fallback stores an "authority:<field>" key on
 * the relationship, rather than inferring its meaning from a surviving metadata row.
 * Repositories can replace that fallback with a bean using the same stable key.
 *
 * @author Ben Bosman (ben . bosman at atmire.com)
 */
public class RelationshipConfigurationServiceImpl implements RelationshipConfigurationService {
    public static final String AUTHORITY_PREFIX = "authority:";

    @Autowired
    private ChoiceAuthorityService choiceAuthorityService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private MetadataFieldService metadataFieldService;

    @Autowired(required = false)
    private List<RelationshipTypeConfiguration> configurations = new ArrayList<>();

    /**
     * Validates all explicitly configured relationship definitions at startup.
     *
     * @throws IllegalArgumentException if a relationship configuration is invalid
     */
    @PostConstruct
    public void validateConfigurations() {
        Set<String> ids = new HashSet<>();

        for (RelationshipTypeConfiguration configuration : configurations) {
            // Reject null relationship configurations.
            if (configuration == null) {
                throw new IllegalArgumentException("Relationship configuration cannot be null");
            }

            // Ensure every configuration has a valid ID.
            String id = configuration.getId();
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Relationship configuration must have an ID");
            }

            // Ensure configuration IDs are unique.
            if (!ids.add(id)) {
                throw new IllegalArgumentException("Duplicate relationship configuration ID: " + id);
            }

            // Validate endpoint entity types, metadata fields, and generator mappings.
            validateConfiguration(configuration);
        }
    }

    /**
     * Validates the metadata fields, entity types and generators of a relationship configuration.
     *
     * @param configuration relationship configuration to validate
     * @throws IllegalArgumentException if the configuration contains invalid or conflicting values
     */
    private void validateConfiguration(RelationshipTypeConfiguration configuration) {
        String id = configuration.getId();

        Optional<String> leftPrimary = configuration.getLeftPrimaryMetadataField();
        Optional<String> rightPrimary = configuration.getRightPrimaryMetadataField();

        // Both endpoints of an explicit relationship configuration must define at least one allowed entity type.
        if (configuration.getLeftEntityTypes().isEmpty() || configuration.getRightEntityTypes().isEmpty()) {
            throw new IllegalArgumentException(
                "Relationship configuration " + id + " must define at least one entity type for each endpoint");
        }

        // Validate the primary metadata field and additional metadata generator mappings for each endpoint.
        validateEndpointConfiguration(id, "left", leftPrimary, configuration.getLeftMetadataGenerators());
        validateEndpointConfiguration(id, "right", rightPrimary, configuration.getRightMetadataGenerators());
    }

    /**
     * Validates the primary metadata field and additional metadata generators of an endpoint.
     *
     * @param configurationId stable identifier of the relationship configuration
     * @param side            endpoint being validated
     * @param primaryField    optional primary metadata field for the endpoint
     * @param generators      additional metadata generators keyed by metadata field
     * @throws IllegalArgumentException if the endpoint configuration is invalid
     */
    private void validateEndpointConfiguration(
        String configurationId,
        String side,
        Optional<String> primaryField,
        Map<String, ItemAuthorityExtraMetadataGenerator> generators) {

        // Validate the primary metadata field, if configured.
        primaryField.ifPresent(field -> validateMetadataField(configurationId, side, field));

        for (Map.Entry<String, ItemAuthorityExtraMetadataGenerator> entry : generators.entrySet()) {
            String field = entry.getKey();

            // Validate each additional metadata field.
            validateMetadataField(configurationId, side, field);

            // Prevent a primary metadata field from also being configured as additional metadata.
            if (primaryField.filter(field::equals).isPresent()) {
                throw new IllegalArgumentException(
                    "Primary metadata field cannot also be additional metadata in relationship configuration "
                        + configurationId + " on the " + side + " endpoint: " + field);
            }
        }
    }

    /**
     * Validates the syntax of a configured metadata field, without checking its existence in the metadata registry.
     * Accepts fields like "dc.title" and "dc.contributor.author", while rejecting empty or malformed field names.
     *
     * @param configurationId stable identifier of the relationship configuration
     * @param side            endpoint containing the field
     * @param field           metadata field name to validate
     * @throws IllegalArgumentException if the metadata field name is invalid
     */
    private void validateMetadataField(String configurationId, String side, String field) {
        if (field == null || !field.matches("[^.\\s]+\\.[^.\\s]+(\\.[^.\\s]+)?")) {
            throw new IllegalArgumentException(
                "Invalid metadata field '" + field + "' in relationship configuration "
                    + configurationId + " on the " + side + " endpoint");
        }
    }

    /**
     * Validates that all primary and additional metadata fields referenced by a relationship
     * configuration exist in the metadata registry.
     *
     * This validation is performed when resolving a configuration, rather than at startup,
     * to ensure the metadata registry is initialized.
     *
     * @param context       the DSpace context used to access the metadata registry
     * @param configuration relationship configuration containing the metadata fields to validate
     * @throws SQLException             if an error occurs while accessing the metadata registry
     * @throws IllegalArgumentException if a configured metadata field does not exist
     */
    private void validateMetadataFields(Context context, RelationshipTypeConfiguration configuration)
        throws SQLException {
        Set<String> fields = new HashSet<>();

        // Collect all primary and additional metadata fields from both endpoints.
        configuration.getLeftPrimaryMetadataField().ifPresent(fields::add);
        configuration.getRightPrimaryMetadataField().ifPresent(fields::add);
        fields.addAll(configuration.getLeftAdditionalMetadataFields());
        fields.addAll(configuration.getRightAdditionalMetadataFields());

        for (String field : fields) {
            // Split the field into schema, element, and optional qualifier.
            String[] parts = field.split("\\.", -1);
            String qualifier = parts.length == 3 ? parts[2] : null;

            // Reject configurations referencing metadata fields that do not exist in the registry.
            if (metadataFieldService.findByElement(context, parts[0], parts[1], qualifier) == null) {
                throw new IllegalArgumentException(
                    "Relationship configuration " + configuration.getId()
                        + " references a nonexistent metadata field: " + field);
            }
        }
    }

    @Override
    public RelationshipTypeConfiguration findForMetadataValue(Context context, MetadataValue value)
        throws SQLException {
        String field = fieldName(value);
        RelationshipTypeConfiguration match = null;
        for (RelationshipTypeConfiguration candidate : configurations) {
            if (candidate.mapsField(field)) {
                if (match != null) {
                    throw new IllegalArgumentException("Ambiguous relationship configuration for field " + field);
                }
                match = candidate;
            }
        }

        RelationshipTypeConfiguration configuration = match != null ? match : fromLinkedEntityAuthority(field);

        // Validate metadata fields against the registry before returning an explicit or authority-derived
        // configuration.
        if (configuration != null) {
            validateMetadataFields(context, configuration);
        }

        return configuration;
    }

    @Override
    public RelationshipTypeConfiguration getByKey(Context context, String key) throws SQLException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Missing relationship configuration key");
        }
        RelationshipTypeConfiguration match = null;
        for (RelationshipTypeConfiguration candidate : configurations) {
            if (key.equals(candidate.getId())) {
                if (match != null) {
                    throw new IllegalArgumentException("Duplicate relationship configuration key " + key);
                }
                match = candidate;
            }
        }
        if (match == null && key.startsWith(AUTHORITY_PREFIX)) {
            match = fromLinkedEntityAuthority(key.substring(AUTHORITY_PREFIX.length()));
        }
        if (match == null) {
            throw new IllegalArgumentException("Unknown relationship configuration " + key);
        }

        // Validate metadata fields against the registry before returning the resolved configuration.
        validateMetadataFields(context, match);

        return match;
    }

    /**
     * Builds a relationship configuration for an authority field linked to an entity type.
     *
     * @return the relationship configuration, or {@code null} if the field is not configured
     *         as a linked entity authority
     */
    private RelationshipTypeConfiguration fromLinkedEntityAuthority(String field) {
        String fieldKey = field.replace('.', '_');
        if (!choiceAuthorityService.isChoicesConfigured(fieldKey, Constants.ITEM, (Collection) null)) {
            return null;
        }
        String[] targetTypes = choiceAuthorityService.getLinkedEntityTypes(fieldKey);
        if (targetTypes == null || targetTypes.length == 0) {
            return null;
        }

        // Validate the dynamically resolved authority field, which bypasses startup validation.
        validateMetadataField(AUTHORITY_PREFIX + field, "left", field);

        RelationshipTypeConfiguration configuration = new RelationshipTypeConfiguration();
        configuration.setId(AUTHORITY_PREFIX + field);
        configuration.setLeftPrimaryMetadataField(field);
        // Allow all entity types supported by the configured authority.
        // Use an explicit Spring definition for custom relationship configurations or inverse anchors.
        configuration.setRightEntityTypes(Set.of(targetTypes));
        return configuration;
    }

    @Override
    public void validate(Context context, RelationshipTypeConfiguration configuration, Item left, Item right)
        throws SQLException {
        validate(context, configuration, left, right, false);
    }

    @Override
    public void validate(Context context, RelationshipTypeConfiguration configuration, Item left, Item right,
                         boolean allowUnarchivedItems) throws SQLException {
        if (left == null || right == null || configuration == null || configuration.getId() == null) {
            throw new IllegalArgumentException("A relationship requires two items and configured semantics");
        }
        // Allow unarchived Items when copying relationships to a new Item version.
        if (!allowUnarchivedItems && (!left.isArchived() || !right.isArchived())) {
            throw new IllegalArgumentException("Concrete relationships require two archived items");
        }
        validateType(configuration.getLeftEntityTypes(), left, "left");
        validateType(configuration.getRightEntityTypes(), right, "right");
    }

    @Override
    public Optional<String> getPrimaryMetadataField(Context context, String configurationKey, boolean left)
        throws SQLException {
        RelationshipTypeConfiguration configuration = getByKey(context, configurationKey);
        return left
            ? configuration.getLeftPrimaryMetadataField()
            : configuration.getRightPrimaryMetadataField();
    }

    @Override
    public boolean isPrimaryMetadata(Context context, String configurationKey, boolean left, String metadataField)
        throws SQLException {
        return getPrimaryMetadataField(context, configurationKey, left)
            .filter(field -> field.equals(metadataField))
            .isPresent();
    }

    @Override
    public boolean isAdditionalMetadata(Context context, String configurationKey, boolean left, String metadataField)
        throws SQLException {
        return getAdditionalMetadataFields(context, configurationKey, left).contains(metadataField);
    }

    @Override
    public Set<String> getAdditionalMetadataFields(Context context, String configurationKey, boolean left)
        throws SQLException {
        RelationshipTypeConfiguration configuration = getByKey(context, configurationKey);
        return left
            ? configuration.getLeftAdditionalMetadataFields()
            : configuration.getRightAdditionalMetadataFields();
    }

    @Override
    public Optional<ItemAuthorityExtraMetadataGenerator> getMetadataGenerator(
        Context context, String configurationKey, boolean left, String metadataField) throws SQLException {

        RelationshipTypeConfiguration configuration = getByKey(context, configurationKey);

        Map<String, ItemAuthorityExtraMetadataGenerator> generators = left
            ? configuration.getLeftMetadataGenerators()
            : configuration.getRightMetadataGenerators();

        return Optional.ofNullable(generators.get(metadataField));
    }

    private void validateType(Set<String> allowed, Item item, String side) {
        if (!allowed.isEmpty() && !allowed.contains(itemService.getEntityTypeLabel(item))) {
            throw new IllegalArgumentException("Item type is not allowed on the " + side + " side of the relationship");
        }
    }

    public static String fieldName(MetadataValue value) {
        return value.getSchema() + "." + value.getElement()
            + (value.getQualifier() == null ? "" : "." + value.getQualifier());
    }
}
