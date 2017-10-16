/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.builder;

import org.apache.log4j.Logger;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.discovery.IndexingService;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutablePeriod;
import org.joda.time.format.PeriodFormat;

import java.sql.SQLException;
import java.util.Date;

/**
 * Abstract builder to construct DSpace Objects
 */
public abstract class AbstractBuilder<T extends DSpaceObject> {

    protected static final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    protected static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    protected static final ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    protected static final InstallItemService installItemService = ContentServiceFactory.getInstance().getInstallItemService();
    protected static final WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();
    protected static final EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();
    protected static final GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();
    protected static final BundleService bundleService = ContentServiceFactory.getInstance().getBundleService();
    protected static final BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    protected static final AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
    protected static final ResourcePolicyService resourcePolicyService = AuthorizeServiceFactory.getInstance().getResourcePolicyService();
    protected static final IndexingService indexingService = DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName(IndexingService.class.getName(),IndexingService.class);

    protected Context context;

    /** log4j category */
    private static final Logger log = Logger.getLogger(AbstractBuilder.class);

    protected <B> B handleException(final Exception e) {
        log.error(e);
        return null;
    }

    protected abstract DSpaceObjectService<T> getDsoService();

    protected <B extends AbstractBuilder<T>> B addMetadataValue(final T dso, final String schema, final String element, final String qualifier, final String value) {
        try {
            getDsoService().addMetadata(context, dso, schema, element, qualifier, Item.ANY, value);
        } catch (SQLException e) {
            return handleException(e);
        }
        return (B) this;
    }

    protected <B extends AbstractBuilder<T>> B setMetadataSingleValue(final T dso, final String schema, final String element, final String qualifier, final String value) {
        try {
            getDsoService().setMetadataSingleValue(context, dso, schema, element, qualifier, Item.ANY, value);
        } catch (SQLException e) {
            return handleException(e);
        }

        return (B) this;
    }

    protected <B extends AbstractBuilder<T>> B setEmbargo(String embargoPeriod, DSpaceObject dso) {
        // add policy just for anonymous
        try {
            MutablePeriod period = PeriodFormat.getDefault().parseMutablePeriod(embargoPeriod);
            Date embargoDate = DateTime.now(DateTimeZone.UTC).plus(period).toDate();

            return setOnlyReadPermission(dso, groupService.findByName(context, Group.ANONYMOUS), embargoDate);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    protected <B extends AbstractBuilder<T>> B setOnlyReadPermission(DSpaceObject dso, Group group, Date startDate) {
        // add policy just for anonymous
        try {
            authorizeService.removeAllPolicies(context, dso);

            ResourcePolicy rp = authorizeService.createOrModifyPolicy(null, context, null, group,
                    null, startDate, Constants.READ, "Integration Test", dso);
            if (rp != null) {
                resourcePolicyService.update(context, rp);
            }
        } catch (Exception e) {
            return handleException(e);
        }
        return (B) this;
    }

    public abstract T build();
}
