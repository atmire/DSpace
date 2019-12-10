/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.link;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

import org.dspace.app.rest.RestResourceController;
import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.hateoas.ItemResource;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.content.EntityType;
import org.dspace.content.RelationshipType;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
public class ItemHalLinkFactory extends HalLinkFactory<ItemResource, RestResourceController> {

    @Autowired
    private EntityTypeService entityTypeService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    @Autowired
    private ConfigurationService configurationService;

    protected void addLinks(ItemResource halResource, Pageable pageable, LinkedList<Link> list) throws Exception {

        Context context = ContextUtil.obtainContext(requestService.getCurrentRequest().getHttpServletRequest());
        SortedMap<String, List<MetadataValueRest>> metadataMap = halResource.getContent().getMetadata().getMap();
        List<MetadataValueRest> metadataValueRestList = metadataMap.get("relationship.type");
        if (metadataValueRestList.size() > 0) {
            String entityTypeString = metadataValueRestList.get(0).getValue();
            EntityType entityType = entityTypeService.findByEntityType(context, entityTypeString);
            List<RelationshipType> relationshipTypes = relationshipTypeService.findByEntityType(context, entityType);
            for (RelationshipType relationshipType : relationshipTypes) {
                String baseUrl = configurationService.getProperty("dspace.baseUrl");
                String uuid = halResource.getContent().getUuid();
                String type;

                if (relationshipType.getLeftType() == entityType) {
                    type = relationshipType.getLeftwardType();
                } else {
                    type = relationshipType.getRightwardType();
                }
                list.add(buildLink(type, baseUrl + "/api/core/relationships/search/byLabel?label=" +
                    type + "&dso=" + uuid));
            }
        }

    }

    protected Class<RestResourceController> getControllerClass() {
        return RestResourceController.class;
    }


    protected Class<ItemResource> getResourceClass() {
        return ItemResource.class;
    }
}
