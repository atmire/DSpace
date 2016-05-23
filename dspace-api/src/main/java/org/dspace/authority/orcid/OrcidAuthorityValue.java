/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.orcid;

import java.util.*;
import org.apache.commons.codec.digest.*;
import org.apache.commons.lang.*;
import org.apache.log4j.*;
import org.apache.solr.common.*;
import org.dspace.authority.*;

/**
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class OrcidAuthorityValue extends PersonAuthorityValue {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(OrcidAuthorityValue.class);
    public static final String TYPE = "orcid";


    private String orcid_id;
    private Map<String, List<String>> otherMetadata = new HashMap<String, List<String>>();


    /**
     * Creates an instance of OrcidAuthorityValue with only uninitialized fields.
     * This is meant to be filled in with values from an existing record.
     * To create a brand new OrcidAuthorityValue, use create()
     */
    protected OrcidAuthorityValue() {
    }
    @Override
    public String getId()
    {
        //We consider an ORCID value to be unique based on the identifier retrieved from ORCID.
        final String nonDigestedIdentifier = OrcidAuthorityValue.class.toString() + " field, " + getField() + ", OrcidIdentifier: " + getOrcid_id();
        // We return an md5 digest of the toString, this will ensure a unique identifier for the same value each time
        return DigestUtils.md5Hex(nonDigestedIdentifier);
    }

    public String getOrcid_id() {
        return orcid_id;
    }

    public void setOrcid_id(String orcid_id) {
        this.orcid_id = orcid_id;
    }

    public Map<String, List<String>> getOtherMetadata() {
        return otherMetadata;
    }

    public void addOtherMetadata(String label, String data) {
        List<String> strings = otherMetadata.get(label);
        if (strings == null) {
            strings = new ArrayList<>();
        }
        strings.add(data);
        otherMetadata.put(label, strings);
    }

    @Override
    public SolrInputDocument getSolrInputDocument() {
        SolrInputDocument doc = super.getSolrInputDocument();
        if (StringUtils.isNotBlank(getOrcid_id())) {
            doc.addField("orcid_id", getOrcid_id());
        }

        for (String t : otherMetadata.keySet()) {
            List<String> data = otherMetadata.get(t);
            for (String data_entry : data) {
                doc.addField("label_" + t, data_entry);
            }
        }
        return doc;
    }

    boolean isNewMetadata(String label, String data) {
        List<String> strings = getOtherMetadata().get(label);
        boolean update;
        if (strings == null) {
            update = StringUtils.isNotBlank(data);
        } else {
            update = !strings.contains(data);
        }
        return update;
    }

    @Override
    public Map<String, String> choiceSelectMap() {

        Map<String, String> map = super.choiceSelectMap();

        map.put("orcid", getOrcid_id());

        return map;
    }

    @Override
    public String getAuthorityType() {
        return TYPE;
    }

    @Override
    public String generateString() {
        return new AuthorityKeyRepresentation(getAuthorityType(), getOrcid_id()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OrcidAuthorityValue that = (OrcidAuthorityValue) o;

        if (orcid_id != null ? !orcid_id.equals(that.orcid_id) : that.orcid_id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return orcid_id != null ? orcid_id.hashCode() : 0;
    }

    @Override
    public boolean hasTheSameInformationAs(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.hasTheSameInformationAs(o)) {
            return false;
        }

        OrcidAuthorityValue that = (OrcidAuthorityValue) o;

        if (orcid_id != null ? !orcid_id.equals(that.orcid_id) : that.orcid_id != null) {
            return false;
        }

        for (String key : otherMetadata.keySet()) {
            if(otherMetadata.get(key) != null){
                List<String> metadata = otherMetadata.get(key);
                List<String> otherMetadata = that.otherMetadata.get(key);
                if (otherMetadata == null) {
                    return false;
                } else {
                    HashSet<String> metadataSet = new HashSet<String>(metadata);
                    HashSet<String> otherMetadataSet = new HashSet<String>(otherMetadata);
                    if (!metadataSet.equals(otherMetadataSet)) {
                        return false;
                    }
                }
            }else{
                if(that.otherMetadata.get(key) != null){
                    return false;
                }
            }
        }

        return true;
    }
}
