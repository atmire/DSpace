/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.external.provider.metadata.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchemaEnum;
import org.dspace.content.NonUniqueMetadataException;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.dto.MetadataValueDTO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.InstallItemService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.content.service.MetadataSchemaService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link BTEMetadataSuggestionProvider} class
 *
 * @author Maria Verdonck (Atmire) on 12/05/2020
 */
public class BTEMetadataSuggestionProviderTest extends AbstractUnitTest {
    /**
     * log4j category
     */
    private static final Logger log =
        org.apache.logging.log4j.LogManager.getLogger(BTEMetadataSuggestionProviderTest.class);

    private static Bitstream bitstream;
    private static File file;

    private static Collection collection;
    private static Community owningCommunity;
    private static Item doiItem;

    private static MetadataField doiMdField;

    protected BitstreamService bitstreamService;
    protected ItemService itemService;
    protected CommunityService communityService;
    protected CollectionService collectionService;
    protected WorkspaceItemService workspaceItemService;
    protected InstallItemService installItemService;
    protected MetadataFieldService metadataFieldService;
    protected MetadataSchemaService metadataSchemaService;

    private BTEMetadataSuggestionProvider bteMetadataSuggestionProvider;

    public BTEMetadataSuggestionProviderTest() {
    }

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init() {
        super.init();
        // Initialize services
        bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
        itemService = ContentServiceFactory.getInstance().getItemService();
        communityService = ContentServiceFactory.getInstance().getCommunityService();
        collectionService = ContentServiceFactory.getInstance().getCollectionService();
        workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();
        installItemService = ContentServiceFactory.getInstance().getInstallItemService();
        metadataFieldService = ContentServiceFactory.getInstance().getMetadataFieldService();
        metadataSchemaService = ContentServiceFactory.getInstance().getMetadataSchemaService();
        bteMetadataSuggestionProvider =
            DSpaceServicesFactory.getInstance().getServiceManager()
                                 .getServicesByType(BTEMetadataSuggestionProvider.class).get(0);

        context.turnOffAuthorisationSystem();

        file = new File(testProps.get("test.bitstream.ris").toString());
        try {
            this.doiMdField = metadataFieldService.findByElement(context,
                                                                 MetadataSchemaEnum.DC.getName(), "identifier", "doi");
            if (this.doiMdField == null) {
                context.turnOffAuthorisationSystem();
                this.doiMdField = metadataFieldService
                    .create(context, metadataSchemaService.find(context, MetadataSchemaEnum.DC.getName()),
                            "identifier", "doi", "DOI");
                context.restoreAuthSystemState();
            }
            bitstream = bitstreamService.create(context, new FileInputStream(file));
            this.owningCommunity = communityService.create(null, context);
            this.collection = collectionService.create(context, owningCommunity);
            WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, true);
            this.doiItem = installItemService.installItem(context, workspaceItem);
            this.itemService.addMetadata(context, this.doiItem, "dc", "identifier", "doi", Item.ANY,
                                         "10.1016/j.ctrv.2019.01.001");
        } catch (IOException e) {
            log.error("IO Error in init", e);
            fail("IO Error in init: " + e.getMessage());
        } catch (SQLException e) {
            log.error("SQL Error in init", e);
            fail("SQL Error in init: " + e.getMessage());
        } catch (AuthorizeException e) {
            log.error("Authorize Error in init", e);
            fail("Authorize Error in init: " + e.getMessage());
        } catch (NonUniqueMetadataException e) {
            log.error("NonUniqueMetadata Error in init", e);
            fail("NonUniqueMetadata Error in init: " + e.getMessage());
        }
        context.restoreAuthSystemState();
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy() {
        file = null;
        bitstream = null;
        super.destroy();
    }

    /**
     * Test of {@link BTEMetadataSuggestionProvider#bitstreamQuery(Bitstream)}
     */
    @Test
    public void testBitstreamQuery() {
        String risSampleFileTitle =
            "Development and external validation of a clinical prognostic score for death in visceral " +
            "leishmaniasis patients in a high HIV co-infection burden area in Ethiopia";
        String risSampleFileIssn = "1932-6203";
        List<ExternalDataObject> externalDataObjects =
            this.bteMetadataSuggestionProvider.bitstreamQuery(this.bitstream);
        assertEquals(risSampleFileTitle, externalDataObjects.get(0).getDisplayValue());
        assertEquals(2, externalDataObjects.get(0).getMetadata().size());
        for (MetadataValueDTO metadataValueDTO : externalDataObjects.get(0).getMetadata()) {
            if (metadataValueDTO.getSchema().equals("dc") && metadataValueDTO.getElement().equals("title") &&
                metadataValueDTO.getQualifier() == null) {
                assertEquals(risSampleFileTitle, metadataValueDTO.getValue());
            }
            if (metadataValueDTO.getSchema().equals("dc") && metadataValueDTO.getElement().equals("identifier") &&
                metadataValueDTO.getQualifier().equals("issn")) {
                assertEquals(risSampleFileIssn, metadataValueDTO.getValue());
            }
        }
    }


    /**
     * Test of {@link BTEMetadataSuggestionProvider#metadataQuery(Item, int, int)}
     */
    @Test
    public void testMetadataQuery() {
        String doiItemTitle = "Innovation in oncology clinical trial design.";
        String doiItemSource = "Cancer treatment reviews";
        List<ExternalDataObject> externalDataObjects =
            this.bteMetadataSuggestionProvider.metadataQuery(this.doiItem, 0, 0);
        assertEquals(doiItemTitle, externalDataObjects.get(0).getDisplayValue());
        assertEquals(11, externalDataObjects.get(0).getMetadata().size());
        for (MetadataValueDTO metadataValueDTO : externalDataObjects.get(0).getMetadata()) {
            if (metadataValueDTO.getSchema().equals("dc") && metadataValueDTO.getElement().equals("title") &&
                metadataValueDTO.getQualifier() == null) {
                assertEquals(doiItemTitle, metadataValueDTO.getValue());
            }
            if (metadataValueDTO.getSchema().equals("dc") && metadataValueDTO.getElement().equals("source") &&
                metadataValueDTO.getQualifier() == null) {
                assertEquals(doiItemSource, metadataValueDTO.getValue());
            }
        }
    }
}
