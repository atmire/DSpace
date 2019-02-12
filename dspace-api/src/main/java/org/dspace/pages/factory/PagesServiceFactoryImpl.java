/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages.factory;

import org.dspace.pages.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is the implementation for the PagesServiceFactory
 */
public class PagesServiceFactoryImpl extends PagesServiceFactory {

    @Autowired
    private PageService pageService;

    @Override
    public PageService getPageService() {
        return pageService;
    }
}
