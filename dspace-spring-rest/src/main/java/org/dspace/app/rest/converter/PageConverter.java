/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.PageRest;
import org.dspace.pages.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This the converter that will take care of the conversion to and from database models of the Page object and the
 * PageRest object. This class allows us to convert from the DSpace API data model to the REST data model and vice versa
 */
@Component
public class PageConverter extends DSpaceConverter<Page, PageRest> {

    @Autowired
    private BitstreamConverter bitstreamConverter;

    @Override
    public Page toModel(PageRest obj) {
        Page page = new Page();
        page.setTitle(obj.getTitle());
        page.setLanguage(obj.getLanguage());
        page.setName(obj.getName());
        page.setId(obj.getId());
        return page;
    }

    @Override
    public PageRest fromModel(Page obj) {
        PageRest pageRest = new PageRest();
        pageRest.setLanguage(obj.getLanguage());
        pageRest.setName(obj.getName());
        pageRest.setTitle(obj.getTitle());
        pageRest.setId(obj.getID());
        pageRest.setBitstreamRest(bitstreamConverter.fromModel(obj.getBitstream()));
        return pageRest;
    }
    protected PageRest newInstance() {
        return new PageRest();
    }

    protected Class<Page> getModelClass() {
        return Page.class;
    }
}
