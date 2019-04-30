/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.builder;

import org.dspace.core.Context;
import org.dspace.pages.Page;
import org.dspace.pages.service.PageService;

/**
 * Builder to construct Page objects
 */
public class PageResourceBuilder extends AbstractBuilder<Page, PageService> {
    private Page page;

    protected PageResourceBuilder(Context context) {
        super(context);
    }

    protected void cleanup() throws Exception {
        delete(page);
    }

    public Page build() {
        return page;
    }

    public void delete(Page dso) throws Exception {
        try (Context c = new Context()) {
            c.turnOffAuthorisationSystem();
            Page attachedDso = c.reloadEntity(dso);
            if (attachedDso != null) {
                getService().delete(c, attachedDso);
            }
            c.complete();
        }

        indexingService.commit();
    }

    protected PageService getService() {
        return pageService;
    }

    public static PageResourceBuilder createPageResource(final Context context, final String name,
                                                         final String language) {
        PageResourceBuilder pageResourcebuilder = new PageResourceBuilder(context);
        return pageResourcebuilder.create(context, name, language);
    }

    private PageResourceBuilder create(final Context context, final String name, final String language) {
        this.context = context;
        try {
            page = pageService.create(context, name, language, siteService.findSite(context));
        } catch (Exception e) {
            return handleException(e);
        }
        return this;
    }

    public PageResourceBuilder withTitle(final String title) {
        this.page.setTitle(title);
        return this;
    }
}
