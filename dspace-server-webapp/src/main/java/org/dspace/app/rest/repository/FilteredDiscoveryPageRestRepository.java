/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.converter.FilteredDiscoveryPageConverter;
import org.dspace.app.rest.model.FilteredDiscoveryPageRest;
import org.dspace.app.rest.model.hateoas.DSpaceResource;
import org.dspace.app.rest.model.hateoas.FilteredDiscoveryPageResource;
import org.dspace.content.EntityType;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.virtual.EntityTypeToFilterQueryService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * This is the repository that is responsible to manage FilteredDiscoveryPage Rest objects
 */
@Component(FilteredDiscoveryPageRest.CATEGORY + "." + FilteredDiscoveryPageRest.NAME)
public class FilteredDiscoveryPageRestRepository extends DSpaceRestRepository<FilteredDiscoveryPageRest, String> {

    @Autowired
    private EntityTypeService entityTypeService;

    @Autowired
    private FilteredDiscoveryPageConverter filteredDiscoveryPageConverter;

    @Autowired
    private EntityTypeToFilterQueryService entityTypeToFilterQueryService;

    public FilteredDiscoveryPageRest findOne(Context context, String string, String projection) {
        try {
            EntityType entityType = entityTypeService.findByEntityType(context, string);
            return filteredDiscoveryPageConverter.fromModel(utils.applyProjection(entityType, projection));
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Page<FilteredDiscoveryPageRest> findAll(Context context, Pageable pageable, String projection) {
        List<EntityType> entityTypeList = null;
        try {
            entityTypeList = entityTypeService.findAll(context);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        List<EntityType> resultingList = new LinkedList<>();
        for (EntityType entityType : entityTypeList) {
            if (entityTypeToFilterQueryService.hasKey(entityType.getLabel())) {
                resultingList.add(entityType);
            }
        }
        Page<FilteredDiscoveryPageRest> page = utils.getPage(resultingList, pageable)
            .map(object -> utils.applyProjection(object, projection))
            .map(filteredDiscoveryPageConverter);
        return page;    }

    public Class<FilteredDiscoveryPageRest> getDomainClass() {
        return FilteredDiscoveryPageRest.class;
    }

    public DSpaceResource<FilteredDiscoveryPageRest> wrapResource(FilteredDiscoveryPageRest model, String... rels) {
        return new FilteredDiscoveryPageResource(model, utils, rels);
    }
}
