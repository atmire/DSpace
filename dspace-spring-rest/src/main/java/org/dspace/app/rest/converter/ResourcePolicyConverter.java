/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.rest.model.ResourcePolicyRest;
import org.dspace.app.rest.utils.ScopeResolver;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.core.Context;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Converter to translate ResourcePolicy into human readable value
 * configuration.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class ResourcePolicyConverter extends DSpaceConverter<ResourcePolicy, ResourcePolicyRest> {

    private static final Logger log = Logger.getLogger(ResourcePolicyConverter.class);

    @Autowired
    ResourcePolicyService resourcePolicyService;

    @Autowired
    GroupService groupService;

    @Autowired
    EPersonService ePersonService;

    @Autowired
    GroupConverter groupConverter;

    @Autowired
    EPersonConverter ePersonConverter;

    @Autowired
    GenericDSpaceObjectConverter genericDSpaceObjectConverter;

    @Override
    public ResourcePolicyRest fromModel(ResourcePolicy obj) {

        ResourcePolicyRest model = new ResourcePolicyRest();

        model.setId(obj.getID());

        model.setName(obj.getRpName());
        model.setDescription(obj.getRpDescription());
        model.setRpType(obj.getRpType());
        model.setResource(genericDSpaceObjectConverter.fromModel(obj.getdSpaceObject()));
        model.setAction(resourcePolicyService.getActionText(obj));

        model.setStartDate(obj.getStartDate());
        model.setEndDate(obj.getEndDate());

        if (obj.getGroup() != null) {
            try {
                model.setGroup(groupConverter.fromModel(groupService.find(new Context(), obj.getGroup().getID())));
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }

        if (obj.getEPerson() != null) {
            try {
                model.setEperson(ePersonConverter.fromModel(ePersonService.find(new Context(), obj.getEPerson().getID())));
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
        return model;
    }

    @Override
    public ResourcePolicy toModel(ResourcePolicyRest obj) {
        // TODO Auto-generated method stub
        return null;
    }

}
