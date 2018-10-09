/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.RestResourceController;

/**
 * The Access Condition REST Resource. It is intent to be an human or REST
 * client understandable representation of the DSpace ResourcePolicy.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class ResourcePolicyRest extends BaseObjectRest<Integer> {

    public static final String NAME = "resourcePolicy";
    public static final String CATEGORY = RestAddressableModel.AUTHORIZATION;

    private String name;

    @JsonProperty("policyType")
    private String rpType;

    private String description;

    @JsonIgnore
    private GroupRest group;

    @JsonIgnore
    private EPersonRest eperson;

    @JsonIgnore
    private DSpaceObjectRest resource;

    private String action;

    private Date startDate;

    private Date endDate;

    public GroupRest getGroup() {
        return group;
    }

    public void setGroup(GroupRest groupUuid) {
        this.group = groupUuid;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }


    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Class getController() {
        return RestResourceController.class;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRpType() {
        return rpType;
    }

    public void setRpType(String rpType) {
        this.rpType = rpType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EPersonRest getEperson() {
        return eperson;
    }

    public void setEperson(EPersonRest eperson) {
        this.eperson = eperson;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public DSpaceObjectRest getResource() {
        return resource;
    }

    public void setResource(DSpaceObjectRest resource) {
        this.resource = resource;
    }
}
