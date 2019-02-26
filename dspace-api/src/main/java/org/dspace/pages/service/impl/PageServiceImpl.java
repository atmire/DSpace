/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.dspace.pages.Page;
import org.dspace.pages.dao.PageDao;
import org.dspace.pages.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the implementation class of the PageService
 */
public class PageServiceImpl implements PageService {

    private static final Logger log = Logger.getLogger(PageService.class);

    @Autowired
    private PageDao pageDao;

    @Autowired
    private BitstreamService bitstreamService;

    public Page create(Context context, String name, String language) throws SQLException {
        Page page = new Page();
        page.setName(name);
        page.setLanguage(language);
        return pageDao.create(context, page);
    }

    @Override
    public Page findByUuid(Context context, UUID uuid) throws SQLException {
        return pageDao.findByUuid(context, uuid);
    }

    @Override
    public List<Page> findByName(Context context, String name) throws SQLException {
        return pageDao.findByName(context, name);
    }

    @Override
    public Page findByNameAndLanguage(Context context, String name, String language) throws SQLException {
        return pageDao.findByNameAndLanguage(context, name, language);
    }

    @Override
    public void attachFile(Context context, InputStream inputStream, Page page)
        throws IOException, SQLException, AuthorizeException {
        Bitstream bitstream = bitstreamService.create(context, inputStream);
        page.setBitstream(bitstream);
        update(context, page);
    }

    @Override
    public List<Page> findAll(Context context) throws SQLException {
        return pageDao.findAll(context, Page.class);
    }

    @Override
    public Page create(Context context) throws SQLException, AuthorizeException {
        Page page = new Page();
        return pageDao.create(context, page);
    }

    @Override
    public Page find(Context context, int id) throws SQLException {
        //TODO Why?
        return null;
    }

    @Override
    public void update(Context context, Page page) throws SQLException, AuthorizeException {
        update(context, Collections.singletonList(page));
    }

    @Override
    public void update(Context context, List<Page> pages) throws SQLException, AuthorizeException {
        if (CollectionUtils.isNotEmpty(pages)) {
            for (Page page : pages) {
                pageDao.save(context, page);
            }
        }
    }

    @Override
    public void delete(Context context, Page page) throws SQLException, AuthorizeException {
        try {
            Bitstream bitstream = page.getBitstream();
            if (bitstream != null) {
                bitstreamService.delete(context, bitstream);
            }
        } catch (IOException e) {
            log.error("The attached bitstream was unable to be deleted for Page with uuid: " + page.getID(), e);
        }
        pageDao.delete(context, page);
    }
}
