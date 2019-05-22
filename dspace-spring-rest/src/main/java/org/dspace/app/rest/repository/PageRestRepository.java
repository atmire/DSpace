/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.PageConverter;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.PageRest;
import org.dspace.app.rest.model.hateoas.DSpaceResource;
import org.dspace.app.rest.model.hateoas.PageResource;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.SiteService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.pages.Page;
import org.dspace.pages.service.PageService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is the repository that is responsible for the Page REST objects.
 */
@Component(PageRest.CATEGORY + "." + PageRest.NAME)
public class PageRestRepository extends DSpaceRestRepository<PageRest, UUID> {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    @Autowired
    PageService pageService;

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    PageConverter pageConverter;

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    SiteService siteService;

    //TODO Permission
    @Override
    public PageRest findOne(Context context, UUID uuid) {
        Page page = null;
        try {
            page = pageService.findByUuid(context, uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (page == null) {
            return null;
        }
        return pageConverter.fromModel(page);
    }

    //TODO Permission
    @Override
    public org.springframework.data.domain.Page<PageRest> findAll(Context context, Pageable pageable) {
        List<Page> pages = new ArrayList<Page>();
        int total = 0;
        try {
            pages = pageService.findAll(context);
            total = pages.size();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new PageImpl<Page>(pages, pageable, total).map(pageConverter);

    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    protected PageRest createAndReturn(Context context, MultipartFile uploadfile, String properties)
        throws SQLException, AuthorizeException {
        HttpServletRequest httpServletRequest = getRequestService().getCurrentRequest().getHttpServletRequest();
        DSpaceObject dSpaceObject = getdSpaceObjectFromRequestParameter(context, httpServletRequest);
        ObjectMapper objectMapper = new ObjectMapper();
        PageRest pageRest;
        try {
            pageRest = objectMapper.readValue(properties, PageRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("Error parsing request body: " + e1.toString());
        }
        if (pageService.findByNameLanguageAndDSpaceObject(context, pageRest.getName(), pageRest.getLanguage(),
                                                          dSpaceObject) != null) {
            throw new DSpaceBadRequestException("The given name and language combination in the request " +
                                                    "already existed in the database. This is not allowed");
        }
        Page page = pageService.create(context, pageRest.getName(), pageRest.getLanguage(),
                                       dSpaceObject);
        page.setTitle(pageRest.getTitle());
        try {
            pageService.attachFile(context, utils.getInputStreamFromMultipart(uploadfile),
                                   uploadfile.getOriginalFilename(), uploadfile.getContentType(), page);
        } catch (IOException e) {
            throw new RuntimeException("The bitstream could not be created from the given file in the request", e);
        }
        pageService.update(context, page);
        return pageConverter.fromModel(page);
    }

    private DSpaceObject getdSpaceObjectFromRequestParameter(Context context, HttpServletRequest httpServletRequest)
        throws SQLException {
        String dspaceObjectUUIDString = httpServletRequest.getParameter("dspaceobject");
        if (StringUtils.isBlank(dspaceObjectUUIDString)) {
            throw new DSpaceBadRequestException("The dspaceobject parameter cannot be missing or empty");
        }
        UUID dspaceObjectUUID = UUID.fromString(dspaceObjectUUIDString);
        if (dspaceObjectUUID == null) {
            throw new DSpaceBadRequestException("The dspaceobject parameter was not a valid uuid");
        }
        DSpaceObject dSpaceObject = utils.getDSpaceObjectFromUUID(context, dspaceObjectUUID);
        if (dSpaceObject == null ||
            (dSpaceObject.getType() != Constants.SITE &&
                dSpaceObject.getType() != Constants.COLLECTION &&
                dSpaceObject.getType() != Constants.COMMUNITY)) {
            throw new DSpaceBadRequestException("The dspaceobject UUID did not resolve to a Site," +
                                                    " Community or Collection");
        }
        return dSpaceObject;
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    protected void delete(Context context, UUID id) throws AuthorizeException {
        Page page = null;
        try {
            page = pageService.findByUuid(context, id);
            if (page == null) {
                throw new ResourceNotFoundException(
                    PageRest.CATEGORY + "." + PageRest.NAME + " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to find Page with id = " + id, e);
        }
        try {
            pageService.delete(context, page);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to delete Page with id = " + id, e);
        }
    }

    @Override
    public Class<PageRest> getDomainClass() {
        return PageRest.class;
    }

    @Override
    public DSpaceResource<PageRest> wrapResource(PageRest model, String... rels) {
        return new PageResource(model, utils, rels);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    protected PageRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                           String properties, MultipartFile uploadfile) {
        Page page = null;
        try {
            page = pageService.findByUuid(context, uuid);
            HttpServletRequest httpServletRequest = getRequestService().getCurrentRequest().getHttpServletRequest();
            DSpaceObject dSpaceObject = getdSpaceObjectFromRequestParameter(context, httpServletRequest);
            if (page == null) {
                throw new ResourceNotFoundException(apiCategory + "." + model + " with id: " + uuid + " not found");
            }
            PageRest pageRest = null;
            try {
                pageRest = new ObjectMapper().readValue(properties, PageRest.class);
            } catch (IOException e) {
                throw new UnprocessableEntityException("error parsing the body ..." + e.getMessage());
            }
            Page foundPage = pageService.findByNameLanguageAndDSpaceObject(context, pageRest.getName(),
                                                                           pageRest.getLanguage(),
                                                                           dSpaceObject);

            if (foundPage != null && !StringUtils.equals(foundPage.getID().toString(), uuid.toString())) {
                throw new RuntimeException("The language and name combination for this PUT update" +
                                                       " already exists in the database");
            }
            page.setLanguage(pageRest.getLanguage());
            page.setTitle(pageRest.getTitle());
            page.setdSpaceObject(dSpaceObject);
            if (uploadfile != null) {
                pageService.attachFile(context, utils.getInputStreamFromMultipart(uploadfile),
                                      uploadfile.getOriginalFilename(), uploadfile.getContentType(), page);
            }
            pageService.update(context, page);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to process page with id: " + uuid, e);
        } catch (AuthorizeException e) {
            throw new AccessDeniedException("The current user was not allowed to make changes to the page with id: "
                                                + uuid, e);
        } catch (IOException e) {
            throw new RuntimeException("The bitstream could not be created from the given file in the request", e);
        }
        return pageConverter.fromModel(page);
    }

    @SearchRestMethod(name = "dso")
    public org.springframework.data.domain.Page<PageRest> searchPages(
        @Parameter(value = "uuid", required = true) UUID uuid,
        @Parameter(value = "name") String name,
        @Parameter(value = "format") String format,
        @Parameter(value = "language") String language,
        Pageable pageable) throws SQLException {
        Context context = obtainContext();
        DSpaceObject dSpaceObject = utils.getDSpaceObjectFromUUID(context, uuid);
        if (dSpaceObject == null) {
            throw new ResourceNotFoundException("The DSpaceObject for UUID: " + uuid +
                                                    " was not found in the database");
        }
        List<Page> pages;

        if (StringUtils.isNotBlank(language)) {
            pages = pageService.findPagesByParameters(context, name, format, language, dSpaceObject);
            if (pages.isEmpty()) {
                throw new DSpaceBadRequestException("There were no Pages found for the given name: " + name + ", " +
                    "format: " + format + ", language: " + language + " and DSpaceObject: " + dSpaceObject.getID());
            }
        } else {
            pages = new LinkedList<>();
            String acceptLanguageHeader = requestService.getCurrentRequest().getHttpServletRequest()
                                                        .getHeader("Accept-Language");
            if (StringUtils.isNotBlank(acceptLanguageHeader)) {
                for (Locale.LanguageRange languageRange : Locale.LanguageRange.parse(
                    acceptLanguageHeader)) {
                    pages.addAll(pageService.findPagesByParameters(context, name, format, languageRange.getRange(),
                                                                   dSpaceObject));
                }
            }
            if (pages.isEmpty()) {
                pages = pageService.findPagesByParameters(context, name, format, null, dSpaceObject);
            }
        }

        org.springframework.data.domain.Page<PageRest> page = utils.getPage(pages, pageable).map(pageConverter);
        return page;
    }

}
