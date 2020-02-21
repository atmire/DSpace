/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.step;

import org.atteo.evo.inflector.English;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.step.DataLicense;
import org.dspace.app.rest.submit.AbstractRestProcessingStep;
import org.dspace.app.rest.submit.SubmissionService;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.Bitstream;
import org.dspace.content.InProgressSubmission;
import org.dspace.core.Constants;
/**
 * License step for DSpace Spring Rest. Expose the license information about the in progress submission.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class LicenseStep extends org.dspace.submit.step.LicenseStep implements AbstractRestProcessingStep {

    private static final String DCTERMS_RIGHTSDATE = "dcterms.accessRights";

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
                configurationService.getProperty("dspace.server.url") + "/api/" + BitstreamRest.CATEGORY + "/" + English
                    .plural(BitstreamRest.NAME) + "/" + bitstream.getID() + "/content");
            result.setGranted(true);
        }
        return result;
    }
}
