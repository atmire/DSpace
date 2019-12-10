/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.projection.factory.impl;

import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.projection.ListProjection;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.projection.factory.DSpaceProjectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListProjectionFactory implements DSpaceProjectionFactory {

    @Autowired
    private ListProjection listProjection;

    public List<Projection> instantiateProjections() {
        List<Projection> projections = new LinkedList<>();
        projections.add(listProjection);
        return projections;
    }
}
