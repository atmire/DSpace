package org.dspace.rest.common;

import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: peterdietz
 * Date: 5/22/13
 * Time: 9:41 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = "community")
public class Community {
    private static Logger log = Logger.getLogger(Community.class);

    //Internal value
    private Integer communityID;

    @XmlElement(name = "type", required = true)
    final String type = "community";

    //Exandable relationships
    @XmlElement(name = "parentCommunity")
    private LiteCommunity parentCommunity;


    private List<String> expand = new ArrayList<String>();

    //Metadata
    private String name;
    private String handle;

    private String copyrightText, introductoryText, shortDescription, sidebarText;
    private Integer countItems;

    @XmlElement(name = "link", required = true)
    private String link;

    @XmlElement(name = "subcommunities", required = true)
    private List<LiteCommunity> subCommunities = new ArrayList<LiteCommunity>();

    @XmlElement(name = "collections")
    private List<LiteCollection> collections = new ArrayList<LiteCollection>();

    public Community(){}

    public Community(org.dspace.content.Community community, String expand) throws SQLException, WebApplicationException{
        setup(community, expand);
    }

    private void setup(org.dspace.content.Community community, String expand) throws SQLException{
        List<String> expandFields = new ArrayList<String>();
        if(expand != null) {
            expandFields = Arrays.asList(expand.split(","));
        }

        this.setCommunityID(community.getID());
        this.setName(community.getName());
        this.setHandle(community.getHandle());
        this.setCopyrightText(community.getMetadata(org.dspace.content.Community.COPYRIGHT_TEXT));
        this.setIntroductoryText(community.getMetadata(org.dspace.content.Community.INTRODUCTORY_TEXT));
        this.setSidebarText(community.getMetadata(org.dspace.content.Community.SIDEBAR_TEXT));
        this.setCountItems(community.countItems());

        this.link = "/communities/" + this.communityID;

        if(expandFields.contains("parentCommunityID") || expandFields.contains("all")) {
            org.dspace.content.Community parentCommunity = community.getParentCommunity();
            if(parentCommunity != null) {
                setParentCommunity(new LiteCommunity(parentCommunity));
            }
        } else {
            this.addExpand("parentCommunityID");
        }

        if(expandFields.contains("subCollections") || expandFields.contains("all")) {
            org.dspace.content.Collection[] collectionArray = community.getCollections();
            collections = new ArrayList<LiteCollection>();
            for(org.dspace.content.Collection collection : collectionArray) {
                collections.add(new LiteCollection(collection));
            }
        } else {
            this.addExpand("subCollections");
        }

        if(expandFields.contains("subCommunities") || expandFields.contains("all")) {
            org.dspace.content.Community[] communityArray = community.getSubcommunities();
            subCommunities = new ArrayList<LiteCommunity>();
            for(org.dspace.content.Community subCommunity : communityArray) {
                subCommunities.add(new LiteCommunity(subCommunity));
            }
        } else {
            this.addExpand("subCommunities");
        }

        if(!expandFields.contains("all")) {
            this.addExpand("all");
        }
    }

    public List<String> getExpand() {
        return expand;
    }

    public void setExpand(List<String> expand) {
        this.expand = expand;
    }

    public void addExpand(String expandableAttribute) {
        this.expand.add(expandableAttribute);
    }

    public Integer getCommunityID() {
        return communityID;
    }

    public void setCommunityID(Integer communityID) {
        this.communityID = communityID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public List<LiteCollection> getCollections() {
        return collections;
    }

    public void setCollections(List<LiteCollection> collections) {
        this.collections = collections;
    }

    public Integer getCountItems() {
        return countItems;
    }

    public void setCountItems(Integer countItems) {
        this.countItems = countItems;
    }

    public String getSidebarText() {
        return sidebarText;
    }

    public void setSidebarText(String sidebarText) {
        this.sidebarText = sidebarText;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getIntroductoryText() {
        return introductoryText;
    }

    public void setIntroductoryText(String introductoryText) {
        this.introductoryText = introductoryText;
    }

    public String getCopyrightText() {
        return copyrightText;
    }

    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText;
    }

    public String getType() {
        return type;
    }

    public LiteCommunity getParentCommunity() {
        return parentCommunity;
    }

    public void setParentCommunity(LiteCommunity parentCommunity) {
        this.parentCommunity = parentCommunity;
    }

    public String getLink() {
        return link;
    }
}
