/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.step;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.atteo.evo.inflector.English;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.step.DataLicense;
import org.dspace.app.rest.repository.patch.ResourcePatch;
import org.dspace.app.rest.submit.AbstractRestProcessingStep;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.InProgressSubmission;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.services.model.Request;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * License step for DSpace Spring Rest. Expose the license information about the in progress submission.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class LicenseStep extends org.dspace.submit.step.LicenseStep implements AbstractRestProcessingStep {

    private static final String DCTERMS_RIGHTSDATE = "dcterms.accessRights";

    @Autowired
    ResourcePatch resourcePatch;

    @Override
    public DataLicense getData(SubmissionService submissionService, InProgressSubmission obj,
            SubmissionStepConfig config)
        throws Exception {
        DataLicense result = new DataLicense();
        Bitstream bitstream = bitstreamService
            .getBitstreamByName(obj.getItem(), Constants.LICENSE_BUNDLE_NAME, Constants.LICENSE_BITSTREAM_NAME);
        if (bitstream != null) {
            String acceptanceDate = bitstreamService.getMetadata(bitstream, DCTERMS_RIGHTSDATE);
            result.setAcceptanceDate(acceptanceDate);
            result.setUrl(
                configurationService.getProperty("dspace.url") + "/api/" + BitstreamRest.CATEGORY + "/" + English
                    .plural(BitstreamRest.NAME) + "/" + bitstream.getID() + "/content");
            result.setGranted(true);
        }
        return result;
    }

    @Override
    public void doPatchProcessing(Context context, Request currentRequest, InProgressSubmission source, Operation op)
            throws SQLException, DCInputsReaderException, IOException, AuthorizeException, IllegalAccessException {
        resourcePatch.patch(context, source, Arrays.asList(op));
    }
}
