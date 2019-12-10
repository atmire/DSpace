/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.projection;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.hateoas.HALResource;
import org.dspace.app.rest.model.hateoas.ItemResource;
import org.dspace.app.rest.model.hateoas.RelationshipResource;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipType;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.core.Context;
import org.dspace.services.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class RelationshipProjection extends AbstractProjection {

    private static final Logger log = Logger.getLogger(RelationshipProjection.class);

    @Autowired
    private ItemService itemService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    @Autowired
    private ConverterService converter;

    @Autowired
    private HalLinkService halLinkService;

    @Autowired
    private Utils utils;

    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public <T extends HALResource> T transformResource(T halResource) {
        if (!(halResource instanceof ItemResource)) {
            return halResource;
        }
        ItemResource itemResource = ((ItemResource) halResource);
        try {
            Context context = ContextUtil.obtainContext(requestService.getCurrentRequest().getHttpServletRequest());
            Item item = itemService.find(context, UUID.fromString(itemResource.getContent().getId()));
            List<RelationshipType> relationshipTypes = relationshipTypeService
                .findByLeftwardOrRightwardTypeName(context, name);
            List<RelationshipResource> relationshipResources = new LinkedList<>();
            for (RelationshipType relationshipType : relationshipTypes) {
                List<Relationship> relationships = relationshipService
                    .findByItemAndRelationshipType(context, item, relationshipType);
                relationshipResources.addAll(relationships.stream().map(relationship -> {
                    RelationshipResource relationshipResource = new RelationshipResource(
                        converter.toRest(relationship, Projection.DEFAULT), utils);
                    halLinkService.addLinks(relationshipResource);
                    return relationshipResource;
                }).collect(Collectors.toList()));

            }
            halResource.embedResource("relationships", relationshipResources);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return super.transformResource(halResource);
    }
}
