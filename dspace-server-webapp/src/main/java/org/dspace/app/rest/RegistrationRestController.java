/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import java.io.IOException;
import java.sql.SQLException;
import javax.mail.MessagingException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.authorization.AuthorizationFeature;
import org.dspace.app.rest.authorization.AuthorizationFeatureService;
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.RegistrationRest;
import org.dspace.app.rest.model.SiteRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Site;
import org.dspace.content.service.SiteService;
import org.dspace.core.Context;
import org.dspace.eperson.service.AccountService;
import org.dspace.eperson.service.EPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ControllerUtils;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/" + RegistrationRest.CATEGORY + "/" + RegistrationRest.NAME_PLURAL)
public class RegistrationRestController {

    @Autowired
    private AuthorizationFeatureService authorizationFeatureService;

    @Autowired
    private SiteService siteService;

    @Autowired
    private ConverterService converterService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EPersonService ePersonService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ResourceSupport> register(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, IOException, MessagingException, AuthorizeException {

        Context context = ContextUtil.obtainContext(request);
        AuthorizationFeature epersonRegistration = authorizationFeatureService.find("epersonRegistration");
        Site site = siteService.findSite(context);
        SiteRest siteRest = converterService.toRest(site, Projection.DEFAULT);
        if (!authorizationFeatureService.isAuthorized(context, epersonRegistration, siteRest)) {
            throw new AccessDeniedException(
                "Registration is disabled, you are not authorized to create a new Authorization");
        }
        ObjectMapper mapper = new ObjectMapper();
        RegistrationRest registrationRest;
        try {
            ServletInputStream input = request.getInputStream();
            registrationRest = mapper.readValue(input, RegistrationRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("Error parsing request body.", e1);
        }
        if (StringUtils.isBlank(registrationRest.getEmail())) {
            throw new UnprocessableEntityException("The email cannot be omitted from the Registration endpoint");
        }
        if (ePersonService.findByEmail(context, registrationRest.getEmail()) != null) {
            accountService.sendForgotPasswordInfo(context, registrationRest.getEmail());
        } else {
            accountService.sendRegistrationInfo(context, registrationRest.getEmail());
        }
        context.complete();
        return ControllerUtils.toEmptyResponse(HttpStatus.CREATED);
    }

}