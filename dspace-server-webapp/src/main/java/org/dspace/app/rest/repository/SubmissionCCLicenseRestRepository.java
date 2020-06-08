/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.model.SubmissionCCLicenseRest;
import org.dspace.app.rest.model.SubmissionCCLicenseUrlRest;
import org.dspace.app.rest.model.hateoas.SubmissionCCLicenseUrlResource;
import org.dspace.core.Context;
import org.dspace.license.CCLicense;
import org.dspace.license.service.CreativeCommonsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * This is the repository that is responsible to manage CCLicense Rest objects
 */
@Component(SubmissionCCLicenseRest.CATEGORY + "." + SubmissionCCLicenseRest.NAME)
public class SubmissionCCLicenseRestRepository extends DSpaceRestRepository<SubmissionCCLicenseRest, String> {

    @Autowired
    protected CreativeCommonsService creativeCommonsService;

    @Override
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public SubmissionCCLicenseRest findOne(final Context context, final String licenseId) {
        CCLicense ccLicense = creativeCommonsService.findOne(licenseId);
        if (ccLicense == null) {
            throw new ResourceNotFoundException("No CC license could be found for ID: " + licenseId );
        }
        return converter.toRest(ccLicense, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public Page<SubmissionCCLicenseRest> findAll(final Context context, final Pageable pageable) {

        List<CCLicense> allCCLicenses = creativeCommonsService.findAllCCLicenses();
        return converter.toRestPage(allCCLicenses, pageable, utils.obtainProjection());
    }

    /**
     * Retrieves the CC License URI based on the license ID and answers in the field questions, provided as parameters
     * to this request
     *
     * @return the CC License URI as a SubmissionCCLicenseUrlResource
     */
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    @SearchRestMethod(name = "rightsByQuestions")
    public SubmissionCCLicenseUrlRest findByRightsByQuestions() {
        ServletRequest servletRequest = requestService.getCurrentRequest()
                                                      .getServletRequest();
        Map<String, String[]> requestParameterMap = servletRequest
                .getParameterMap();
        Map<String, String> parameterMap = new HashMap<>();
        String licenseId = servletRequest.getParameter("license");
        if (StringUtils.isBlank(licenseId)) {
            throw new DSpaceBadRequestException(
                    "A \"license\" parameter needs to be provided.");
        }
        for (String parameter : requestParameterMap.keySet()) {
            if (StringUtils.startsWith(parameter, "answer_")) {
                String field = StringUtils.substringAfter(parameter, "answer_");
                String answer = "";
                if (requestParameterMap.get(parameter).length > 0) {
                    answer = requestParameterMap.get(parameter)[0];
                }
                parameterMap.put(field, answer);
            }
        }

        Map<String, String> fullParamMap = creativeCommonsService.retrieveFullAnswerMap(licenseId, parameterMap);
        if (fullParamMap == null) {
            throw new ResourceNotFoundException("No CC License could be matched on the provided ID: " + licenseId);
        }
        boolean licenseContainsCorrectInfo = creativeCommonsService.verifyLicenseInformation(licenseId, fullParamMap);
        if (!licenseContainsCorrectInfo) {
            throw new DSpaceBadRequestException(
                    "The provided answers do not match the required fields for the provided license.");
        }

        String licenseUri = creativeCommonsService.retrieveLicenseUri(licenseId, fullParamMap);

        if (StringUtils.isBlank(licenseUri)) {
            throw new ResourceNotFoundException("No CC License URI could be found for ID: " + licenseId);
        }

        SubmissionCCLicenseUrlRest submissionCCLicenseUrlRest = converter.toRest(licenseUri, utils.obtainProjection());
        return submissionCCLicenseUrlRest;

    }

    @Override
    public Class<SubmissionCCLicenseRest> getDomainClass() {
        return SubmissionCCLicenseRest.class;
    }
}
