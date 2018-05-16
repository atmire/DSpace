/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * 
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.atteo.evo.inflector.English;

import org.dspace.app.rest.converter.DSpaceObjectConverter;
import org.dspace.app.rest.model.DSpaceObjectRest;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.identifier.IdentifierNotFoundException;
import org.dspace.identifier.IdentifierNotResolvableException;
import org.dspace.identifier.factory.IdentifierServiceFactory;
import org.dspace.identifier.service.IdentifierService;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pid")
public class IdentifierRestController implements InitializingBean {

    private static final String REGEX_HANDLE = "^\\d+/\\d+$";

    private static final Logger log =
            Logger.getLogger(IdentifierRestController.class);

    @Autowired
    private List<DSpaceObjectConverter> converters;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<Link> links = new ArrayList<Link>();

        Link l = new Link("/api/pid", "pid");
        links.add(l);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/find", params = "id")
    @SuppressWarnings("unchecked")
    public void getDSObyIdentifier(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam("id") String id)
            throws IOException, SQLException {

        DSpaceObject dso = null;
        Context context = ContextUtil.obtainContext(request);
        IdentifierService identifierService = IdentifierServiceFactory
                .getInstance().getIdentifierService();
        try {
            dso = identifierService.resolve(context, id);
            if (dso != null) {
                DSpaceObjectRest dsor = convertDSpaceObject(dso);
                URI link = linkTo(dsor.getController(), dsor.getCategory(),
                        English.plural(dsor.getType()))
                        .slash(dsor.getId()).toUri();
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.sendRedirect(link.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IdentifierNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IdentifierNotResolvableException e) {
            response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        } finally {
            context.abort();
        }
    }

    /**
     *
     */
    private DSpaceObjectRest convertDSpaceObject(DSpaceObject dspaceObject) {
        for (DSpaceObjectConverter converter : converters) {
            if (converter.supportsModel(dspaceObject)) {
                return converter.fromModel(dspaceObject);
            }
        }
        return null;
    }
}
