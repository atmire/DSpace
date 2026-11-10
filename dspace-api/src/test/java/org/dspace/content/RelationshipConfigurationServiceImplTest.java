/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.dspace.content.authority.ItemAuthorityExtraMetadataGenerator;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link RelationshipConfigurationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RelationshipConfigurationServiceImplTest {

    @Mock
    private ChoiceAuthorityService choiceAuthorityService;

    @Mock
    private ItemService itemService;

    @Mock
    private MetadataFieldService metadataFieldService;

    @Mock
    private Context context;

    @InjectMocks
    private RelationshipConfigurationServiceImpl service;

    private RelationshipTypeConfiguration configuration;

    @Before
    public void setUp() {
        configuration = configuration("publication-author", "dc.contributor.author",
                                      "dc.relation.isAuthorOfPublication");
        setConfigurations(configuration);
    }

    /**
     * Creates a relationship configuration between Publication and Person entities.
     */
    private RelationshipTypeConfiguration configuration(String id, String leftField, String rightField) {
        RelationshipTypeConfiguration result = new RelationshipTypeConfiguration();
        result.setId(id);
        result.setLeftEntityTypes(Set.of("Publication"));
        result.setRightEntityTypes(Set.of("Person"));
        result.setLeftPrimaryMetadataField(leftField);
        result.setRightPrimaryMetadataField(rightField);
        return result;
    }

    /**
     * Sets the relationship configurations used by the service in tests.
     */
    private void setConfigurations(RelationshipTypeConfiguration... configurations) {
        ReflectionTestUtils.setField(service, "configurations", Arrays.asList(configurations));
    }

    /**
     * Creates a mocked metadata value with the specified field components.
     */
    private MetadataValue metadataValue(String schema, String element, String qualifier) {
        MetadataValue value = mock(MetadataValue.class);
        when(value.getSchema()).thenReturn(schema);
        when(value.getElement()).thenReturn(element);
        when(value.getQualifier()).thenReturn(qualifier);
        return value;
    }

    /**
     * Mocks metadata field lookup to treat all fields as registered.
     */
    private void registerMetadataFields() throws SQLException {
        when(metadataFieldService.findByElement(eq(context), any(String.class),
                                                any(String.class), org.mockito.ArgumentMatchers.nullable(String.class)))
            .thenReturn(mock(MetadataField.class));
    }

    /**
     * Mocks a linked-entity authority for the given field and target entity types.
     */
    private void enableAuthority(String field, String... entityTypes) {
        String authorityKey = field.replace('.', '_');

        when(choiceAuthorityService.isChoicesConfigured(
            eq(authorityKey), eq(Constants.ITEM), isNull(Collection.class)
        )).thenReturn(true);

        when(choiceAuthorityService.getLinkedEntityTypes(authorityKey)).thenReturn(entityTypes);
    }

    /**
     * Verifies that duplicate configuration IDs are rejected at startup.
     */
    @Test
    public void testDuplicateIdsRejectedAtStartup() {
        setConfigurations(configuration, configuration("publication-author", "dc.title", null));
        assertThrows(IllegalArgumentException.class, () -> service.validateConfigurations());
    }

    /**
     * Verifies that endpoint entity types are required.
     */
    @Test
    public void testMissingLeftEntityTypesRejectedAtStartup() {
        configuration.setLeftEntityTypes(Set.of());
        assertThrows(IllegalArgumentException.class, () -> service.validateConfigurations());
    }

    /**
     * Verifies that a configuration can omit both primary metadata fields.
     */
    @Test
    public void testConfigurationWithoutPrimaryFieldsIsAllowed() {
        configuration.setLeftPrimaryMetadataField(null);
        configuration.setRightPrimaryMetadataField(null);
        service.validateConfigurations();
    }

    /**
     * Verifies that invalid primary metadata field syntax is rejected at startup.
     */
    @Test
    public void testInvalidPrimaryMetadataFieldRejectedAtStartup() {
        configuration.setLeftPrimaryMetadataField("dc..author");
        assertThrows(IllegalArgumentException.class, () -> service.validateConfigurations());
    }

    /**
     * Verifies that a primary field cannot also be configured as additional metadata.
     */
    @Test
    public void testPrimaryFieldCannotAlsoBeAdditionalOnLeft() {
        ItemAuthorityExtraMetadataGenerator generator = mock(ItemAuthorityExtraMetadataGenerator.class);
        configuration.setLeftMetadataGenerators(Map.of("dc.contributor.author", generator));
        assertThrows(IllegalArgumentException.class, () -> service.validateConfigurations());
    }

    /**
     * Verifies that explicit relationship configurations can be resolved by ID.
     */
    @Test
    public void testGetByKeyResolvesExplicitConfiguration() throws SQLException {
        registerMetadataFields();
        assertSame(configuration, service.getByKey(context, "publication-author"));
    }

    /**
     * Verifies that either endpoint's primary metadata field resolves the configuration.
     */
    @Test
    public void testFindForMetadataValueMatchesEitherPrimaryField() throws SQLException {
        registerMetadataFields();
        assertSame(configuration, service.findForMetadataValue(context,
                                                               metadataValue("dc", "contributor", "author")));
        assertSame(configuration, service.findForMetadataValue(context,
                                                               metadataValue("dc", "relation",
                                                                             "isAuthorOfPublication")));
    }

    /**
     * Verifies that relationships can define primary metadata on only one endpoint.
     */
    @Test
    public void testOneSidedPrimaryMetadataConfiguration() throws SQLException {
        configuration.setRightPrimaryMetadataField(null);
        registerMetadataFields();

        assertSame(configuration, service.findForMetadataValue(context,
                                                               metadataValue("dc", "contributor", "author")));

        assertTrue(service.getPrimaryMetadataField(context, "publication-author", false).isEmpty());
    }

    /**
     * Verifies that additional metadata fields are not used to resolve relationships.
     */
    @Test
    public void testFindForMetadataValueDoesNotMatchAdditionalFields() throws SQLException {
        ItemAuthorityExtraMetadataGenerator generator = mock(ItemAuthorityExtraMetadataGenerator.class);
        configuration.setLeftMetadataGenerators(Map.of("person.identifier.orcid", generator));
        assertNull(service.findForMetadataValue(context,
                                                metadataValue("person", "identifier", "orcid")));
    }

    /**
     * Verifies that ambiguous primary metadata mappings are rejected.
     */
    @Test
    public void testFindForMetadataValueRejectsAmbiguousPrimaryField() {
        setConfigurations(configuration, configuration("another", "dc.contributor.author", null));
        assertThrows(IllegalArgumentException.class, () -> service.findForMetadataValue(context,
                                                                                        metadataValue("dc",
                                                                                                      "contributor",
                                                                                                      "author")));
    }

    /**
     * Verifies that primary metadata fields must exist in the metadata registry.
     */
    @Test
    public void testGetByKeyRejectsNonexistentPrimaryMetadataField() {
        assertThrows(IllegalArgumentException.class,
                     () -> service.getByKey(context, "publication-author"));
    }

    /**
     * Verifies that additional metadata fields must exist in the metadata registry.
     */
    @Test
    public void testNonexistentAdditionalMetadataFieldIsRejected() throws SQLException {
        ItemAuthorityExtraMetadataGenerator generator = mock(ItemAuthorityExtraMetadataGenerator.class);
        configuration.setLeftMetadataGenerators(Map.of("person.identifier.orcid", generator));
        when(metadataFieldService.findByElement(eq(context), any(String.class),
                                                any(String.class), org.mockito.ArgumentMatchers.nullable(String.class)))
            .thenAnswer(invocation -> {
                String schema = invocation.getArgument(1);
                return "person".equals(schema) ? null : mock(MetadataField.class);
            });
        assertThrows(IllegalArgumentException.class,
                     () -> service.getByKey(context, "publication-author"));
    }

    /**
     * Verifies that linked-entity authority settings are used when no explicit relationship configuration exists.
     */
    @Test
    public void testAuthorityFallbackResolvesValidField() throws SQLException {
        setConfigurations();
        enableAuthority("dc.contributor.author", "Person");
        registerMetadataFields();

        RelationshipTypeConfiguration result = service.findForMetadataValue(context,
                                                                            metadataValue("dc", "contributor",
                                                                                          "author"));

        assertEquals("authority:dc.contributor.author", result.getId());
        assertEquals("dc.contributor.author", result.getLeftPrimaryMetadataField().orElse(null));
        assertEquals(Set.of("Person"), result.getRightEntityTypes());
        assertTrue(result.getLeftEntityTypes().isEmpty());
    }

    /**
     * Verifies that dynamically resolved authority fields are validated for correct syntax.
     */
    @Test
    public void testAuthorityFallbackRejectsMalformedMetadataField() {
        setConfigurations();
        enableAuthority("dc..author", "Person");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> service.getByKey(context, "authority:dc..author"));

        assertTrue(exception.getMessage().contains("dc..author"));
        assertTrue(exception.getMessage().contains("left endpoint"));
    }

    /**
     * Verifies that unconfigured linked-entity authorities do not resolve.
     */
    @Test
    public void testUnconfiguredAuthorityDoesNotResolve() throws SQLException {
        setConfigurations();
        assertNull(service.findForMetadataValue(context,
                                                metadataValue("dc", "contributor", "author")));
        assertThrows(IllegalArgumentException.class,
                     () -> service.getByKey(context, "authority:dc.contributor.author"));
    }

    /**
     * Verifies primary metadata, additional metadata, and generator lookup for both endpoints.
     */
    @Test
    public void testMetadataHelpersForBothEndpoints() throws SQLException {
        ItemAuthorityExtraMetadataGenerator leftGenerator = mock(ItemAuthorityExtraMetadataGenerator.class);
        ItemAuthorityExtraMetadataGenerator rightGenerator = mock(ItemAuthorityExtraMetadataGenerator.class);
        configuration.setLeftMetadataGenerators(Map.of("person.identifier.orcid", leftGenerator));
        configuration.setRightMetadataGenerators(Map.of("dc.description", rightGenerator));
        registerMetadataFields();

        assertEquals("dc.contributor.author",
                     service.getPrimaryMetadataField(context, "publication-author", true).orElse(null));
        assertEquals("dc.relation.isAuthorOfPublication",
                     service.getPrimaryMetadataField(context, "publication-author", false).orElse(null));
        assertTrue(service.isPrimaryMetadata(context, "publication-author", true, "dc.contributor.author"));
        assertFalse(service.isPrimaryMetadata(context, "publication-author", false, "dc.contributor.author"));
        assertEquals(Set.of("person.identifier.orcid"),
                     service.getAdditionalMetadataFields(context, "publication-author", true));
        assertEquals(Set.of("dc.description"),
                     service.getAdditionalMetadataFields(context, "publication-author", false));
        assertTrue(service.isAdditionalMetadata(context, "publication-author", true, "person.identifier.orcid"));
        assertFalse(service.isAdditionalMetadata(context, "publication-author", false, "person.identifier.orcid"));
        assertSame(leftGenerator,
                   service.getMetadataGenerator(context, "publication-author", true, "person.identifier.orcid")
                          .orElse(null));
        assertSame(rightGenerator,
                   service.getMetadataGenerator(context, "publication-author", false, "dc.description").orElse(null));
        assertTrue(service.getMetadataGenerator(context, "publication-author", true, "dc.title").isEmpty());
    }

    /**
     * Verifies that concrete relationships require archived items.
     */
    @Test
    public void testValidateRequiresArchivedItems() {
        Item left = mock(Item.class);
        Item right = mock(Item.class);
        assertThrows(IllegalArgumentException.class,
                     () -> service.validate(context, configuration, left, right));
    }

    /**
     * Verifies that relationship endpoints must have an allowed entity type.
     */
    @Test
    public void testValidateRejectsIncorrectEntityType() {
        Item left = mock(Item.class);
        Item right = mock(Item.class);
        when(left.isArchived()).thenReturn(true);
        when(right.isArchived()).thenReturn(true);
        when(itemService.getEntityTypeLabel(left)).thenReturn("Collection");

        assertThrows(IllegalArgumentException.class,
                     () -> service.validate(context, configuration, left, right));
    }

    /**
     * Verifies that relationship endpoints are accepted in their configured orientation.
     */
    @Test
    public void testValidateAcceptsMatchingEndpointTypes() throws SQLException {
        Item left = mock(Item.class);
        Item right = mock(Item.class);

        when(left.isArchived()).thenReturn(true);
        when(right.isArchived()).thenReturn(true);
        when(itemService.getEntityTypeLabel(left)).thenReturn("Publication");
        when(itemService.getEntityTypeLabel(right)).thenReturn("Person");

        service.validate(context, configuration, left, right);
    }

    /**
     * Verifies that unarchived items are accepted when explicitly allowed.
     */
    @Test
    public void testValidateCanAllowUnarchivedItems() throws SQLException {
        Item left = mock(Item.class);
        Item right = mock(Item.class);
        when(itemService.getEntityTypeLabel(left)).thenReturn("Publication");
        when(itemService.getEntityTypeLabel(right)).thenReturn("Person");

        service.validate(context, configuration, left, right, true);
    }
}
