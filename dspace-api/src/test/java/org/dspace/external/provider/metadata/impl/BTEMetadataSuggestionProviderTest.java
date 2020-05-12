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
import org.dspace.content.Bitstream;
import org.dspace.content.dto.MetadataValueDTO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
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

    protected BitstreamService bitstreamService;

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
        bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
        bteMetadataSuggestionProvider =
            DSpaceServicesFactory.getInstance().getServiceManager()
                                 .getServicesByType(BTEMetadataSuggestionProvider.class).get(0);
        context.turnOffAuthorisationSystem();

        file = new File(testProps.get("test.bitstream.ris").toString());
        try {
            bitstream = bitstreamService.create(context, new FileInputStream(file));
        } catch (IOException e) {
            log.error("IO Error in init", e);
            fail("IO Error in init: " + e.getMessage());
        } catch (SQLException e) {
            log.error("SQL Error in init", e);
            fail("SQL Error in init: " + e.getMessage());
        }

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
}
