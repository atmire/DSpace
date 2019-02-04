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
import java.util.UUID;

import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.RelationshipConverter;
import org.dspace.app.rest.model.RelationshipRest;
import org.dspace.app.rest.model.hateoas.DSpaceResource;
import org.dspace.app.rest.model.hateoas.RelationshipResource;
import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipType;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Component;

/**
 * This is the repository that is responsible to manage Relationship Rest objects
 */
@Component(RelationshipRest.CATEGORY + "." + RelationshipRest.NAME)
public class RelationshipRestRepository extends DSpaceRestRepository<RelationshipRest, Integer> {

    @Autowired
    private ItemService itemService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    @Autowired
    private RelationshipConverter relationshipConverter;

    public RelationshipRest findOne(Context context, Integer integer) {
        try {
            return relationshipConverter.fromModel(relationshipService.find(context, integer));
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Page<RelationshipRest> findAll(Context context, Pageable pageable) {
        List<Relationship> relationships = null;
        try {
            relationships = relationshipService.findAll(context);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Page<RelationshipRest> page = utils.getPage(relationships, pageable).map(relationshipConverter);
        return page;
    }

    public Class<RelationshipRest> getDomainClass() {
        return RelationshipRest.class;
    }

    public DSpaceResource<RelationshipRest> wrapResource(RelationshipRest model, String... rels) {
        return new RelationshipResource(model, utils, rels);
    }

    @SearchRestMethod(name = "byLabel")
    public Page<RelationshipRest> findByLabel(@Parameter(value = "label", required = true) String label,
                                              @Parameter(value = "dso", required = false) UUID dsoId,
                                              Pageable pageable) throws SQLException {

        Context context = obtainContext();

        List<RelationshipType> relationshipTypeList = relationshipTypeService.findByLeftOrRightLabel(context, label);
        List<Relationship> relationships = new LinkedList<>();
        if (dsoId != null) {

            Item item = itemService.find(context, dsoId);

            if (item == null) {
                throw new ResourceNotFoundException("The request DSO with id: " + dsoId + " was not found");
            }
            for (RelationshipType relationshipType : relationshipTypeList) {
                relationships.addAll(relationshipService.findByItemAndRelationshipType(context,
                                                                                       item, relationshipType));
            }
        } else {
            for (RelationshipType relationshipType : relationshipTypeList) {
                relationships.addAll(relationshipService.findByRelationshipType(context, relationshipType));
            }
        }

        Page<RelationshipRest> page = utils.getPage(relationships, pageable).map(relationshipConverter);
        return page;

    }
}
