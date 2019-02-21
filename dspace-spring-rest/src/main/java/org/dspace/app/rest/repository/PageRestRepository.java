/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.PageConverter;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

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
    protected PageRest createAndReturn(Context context) throws AuthorizeException, SQLException {
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        PageRest pageRest;
        try {
            ServletInputStream input = req.getInputStream();
            pageRest = objectMapper.readValue(input, PageRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("Error parsing request body: " + e1.toString());
        }
        StringBuilder filePath = new StringBuilder();
        filePath.append(configurationService.getProperty("dspace.dir")).append(File.separatorChar).append("config")
                .append(File.separatorChar).append("news-top.html");

        File newsTopFile = new File(filePath.toString());
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(newsTopFile);
            Page page = pageService.create(context, pageRest.getName(), pageRest.getLanguage(), fileInputStream);
            page.setTitle(pageRest.getTitle());
            pageService.update(context, page);
            return pageConverter.fromModel(page);
        } catch (IOException e) {
            log.error("Reading in the inputstream caused an exception for file with path: " + filePath, e);
            throw new BadRequestException("A bad request has been formed for the inputstream with path: "
                                              + filePath, e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error("Unable to close inputstream from path: " + filePath , e);
                }
            }
        }
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
}
