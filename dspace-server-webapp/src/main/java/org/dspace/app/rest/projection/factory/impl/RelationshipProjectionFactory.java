/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.projection.factory.impl;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.projection.RelationshipProjection;
import org.dspace.app.rest.projection.factory.DSpaceProjectionFactory;
import org.dspace.content.RelationshipType;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RelationshipProjectionFactory implements DSpaceProjectionFactory {

    private static final Logger log = Logger.getLogger(RelationshipProjectionFactory.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    public List<Projection> instantiateProjections() {
        Context context = new Context();
        List<Projection> projections = new LinkedList<>();
        try {
            for (RelationshipType relationshipType : relationshipTypeService.findAll(context)) {
                RelationshipProjection leftRelationshipProjection = applicationContext
                    .getBean(RelationshipProjection.class);
                RelationshipProjection rightRelationshipProjection = applicationContext
                    .getBean(RelationshipProjection.class);
                leftRelationshipProjection.setName(relationshipType.getLeftwardType());
                rightRelationshipProjection.setName(relationshipType.getRightwardType());
                projections.add(leftRelationshipProjection);
                projections.add(rightRelationshipProjection);
            }
        } catch (SQLException e) {
            log.error("there was an issue retrieving the RelationshipTypes", e);
        }
        context.close();
        return projections;
    }

}
