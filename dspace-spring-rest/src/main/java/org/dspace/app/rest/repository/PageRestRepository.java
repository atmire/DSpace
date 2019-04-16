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
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.dspace.content.service.BitstreamService;
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
        ObjectMapper objectMapper = new ObjectMapper();
        PageRest pageRest;
        try {
            pageRest = objectMapper.readValue(properties, PageRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("Error parsing request body: " + e1.toString());
        }
        if (pageService.findByNameAndLanguage(context, pageRest.getName(), pageRest.getLanguage()) != null) {
            throw new DSpaceBadRequestException("The given name and language combination in the request " +
                                                    "already existed in the database. This is not allowed");
        }
        Page page = pageService.create(context, pageRest.getName(), pageRest.getLanguage());
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public PageRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                           MultipartFile file) {

        Page page = null;
        try {
            page = pageService.findByUuid(context, uuid);
            if (page == null) {
                throw new ResourceNotFoundException(apiCategory + "." + model + " with id: " + uuid + " not found");
            }
            pageService.attachFile(context, utils.getInputStreamFromMultipart(file), file.getName(),
                                   file.getContentType(), page);
        } catch (IOException e) {
            throw new RuntimeException("The bitstream could not be created from the given file in the request", e);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to process page with id: " + uuid, e);
        } catch (AuthorizeException e) {
            throw new AccessDeniedException("The current user was not allowed to make changes to the page with id: "
                                                + uuid, e);
        }
        return pageConverter.fromModel(page);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public PageRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                    JsonNode jsonNode) {

        Page page = null;
        try {
            page = pageService.findByUuid(context, uuid);
            if (page == null) {
                throw new ResourceNotFoundException(apiCategory + "." + model + " with id: " + uuid + " not found");
            }
            PageRest pageRest = null;
            try {
                pageRest = new ObjectMapper().readValue(jsonNode.toString(), PageRest.class);
            } catch (IOException e) {
                throw new UnprocessableEntityException("error parsing the body ..." + e.getMessage());
            }
            page.setLanguage(pageRest.getLanguage());
            page.setTitle(pageRest.getTitle());
            pageService.update(context, page);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to process page with id: " + uuid, e);
        } catch (AuthorizeException e) {
            throw new AccessDeniedException("The current user was not allowed to make changes to the page with id: "
                                                + uuid, e);
        }
        return pageConverter.fromModel(page);
    }
    @Override
    public Class<PageRest> getDomainClass() {
        return PageRest.class;
    }

    @Override
    public DSpaceResource<PageRest> wrapResource(PageRest model, String... rels) {
        return new PageResource(model, utils, rels);
    }

    @SearchRestMethod(name = "languages")
    public org.springframework.data.domain.Page<PageRest> findByName(
        @Parameter(value = "name", required = true) String pageName, Pageable pageable) throws SQLException {
        Context context = obtainContext();
        List<Page> pages = pageService.findByName(context, pageName);

        org.springframework.data.domain.Page<PageRest> page = utils.getPage(pages, pageable).map(pageConverter);

        return page;
    }
}
