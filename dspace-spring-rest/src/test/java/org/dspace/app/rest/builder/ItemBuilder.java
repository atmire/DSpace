/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.builder;

import org.dspace.content.*;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.dspace.eperson.Group;

/**
 * Builder to construct Item objects
 */
public class ItemBuilder extends AbstractBuilder<Item> {

    private WorkspaceItem workspaceItem;

    public ItemBuilder createItem(final Context context, final Collection col1) {
        this.context = context;

        try {
            workspaceItem = workspaceItemService.create(context, col1, false);
        } catch (Exception e) {
            return handleException(e);
        }

        return this;
    }

    public ItemBuilder withTitle(final String title) {
        return setMetadataSingleValue(workspaceItem.getItem(), MetadataSchema.DC_SCHEMA, "title", null, title);
    }

    public ItemBuilder withIssueDate(final String issueDate) {
        return addMetadataValue(workspaceItem.getItem(), MetadataSchema.DC_SCHEMA, "date", "issued", new DCDate(issueDate).toString());
    }

    public ItemBuilder withAuthor(final String authorName) {
        return addMetadataValue(workspaceItem.getItem(), MetadataSchema.DC_SCHEMA, "date", "issued", authorName);
    }

    public ItemBuilder withSubject(final String subject) {
        return addMetadataValue(workspaceItem.getItem(), MetadataSchema.DC_SCHEMA, "subject", null, subject);
    }

    public ItemBuilder makePrivate() {
        workspaceItem.getItem().setDiscoverable(false);
        return this;
    }

    public ItemBuilder withEmbargoPeriod(String embargoPeriod) {
        return setEmbargo(embargoPeriod, workspaceItem.getItem());
    }

    public ItemBuilder withReaderGroup(Group group) {
        return setOnlyReadPermission(workspaceItem.getItem(), group, null);
    }

    @Override
    public Item build() {
        try {
            Item item = installItemService.installItem(context, workspaceItem);
            itemService.update(context, item);
            context.dispatchEvents();

            indexingService.commit();
            return item;
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Override
    protected DSpaceObjectService<Item> getDsoService() {
        return itemService;
    }

}
