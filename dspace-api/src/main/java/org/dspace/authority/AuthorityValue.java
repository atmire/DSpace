/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.*;

/**
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public abstract class AuthorityValue {

    /**
     * The solr id field
     */
    private String solrId;
    /**
     * The metadata field that this authority value is for
     */
    private String field;

    /**
     * The text value of this authority
     */
    private String value;

    /**
     * When this authority record has been created
     */
    private Date creationDate;

    /**
     * If this authority has been removed
     */
    private boolean deleted;

    /**
     * represents the last time that DSpace got updated information from its external source
     */
    private Date lastModified;

    private Set<String> notDuplicateOfAuthorityIds = new HashSet<String>();

    private boolean verified;

    public AuthorityValue() {
    }

    public abstract String getId();

    public String getSolrId() {
        return solrId;
    }

    public void setSolrId(String solrId) {
        this.solrId = solrId;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public abstract AuthorityCategory getCategory();

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = stringToDate(creationDate);
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = stringToDate(lastModified);
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Set<String> getNotDuplicateOfAuthorityIds() {
        if(notDuplicateOfAuthorityIds == null) {
            notDuplicateOfAuthorityIds = new HashSet<>();
        }
        return notDuplicateOfAuthorityIds;
    }

    public void addNotDuplicateOfAuthorityId(final String id) {
        getNotDuplicateOfAuthorityIds().add(id);
    }

    public void updateLastModifiedDate() {
        this.lastModified = new Date();
    }

    public void update() {
        updateLastModifiedDate();
    }

    public void delete() {
        setDeleted(true);
        updateLastModifiedDate();
    }

    /**
     * Generate a solr record from this instance
     *
     * @return SolrInputDocument
     */
    public SolrInputDocument getSolrInputDocument() {

        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", getId());
        doc.addField("field", getField());
        doc.addField("value", getValue());
        doc.addField("deleted", isDeleted());
        doc.addField("creation_date", getCreationDate());
        doc.addField("last_modified_date", getLastModified());
        doc.addField("authority_type", getAuthorityType());
        doc.addField("authority_category", getCategory().toString());
        doc.addField("verified", isVerified());

        for (String id : getNotDuplicateOfAuthorityIds()) {
            doc.addField("not_duplicate_of_authority_id", id);
        }

        return doc;
    }

    /**
     * Information that can be used the choice ui
     * @return map
     */
    public Map<String, String> choiceSelectMap() {
        return new HashMap<String, String>();
    }


    public List<DateTimeFormatter> getDateFormatters() {
        List<DateTimeFormatter> list = new ArrayList<DateTimeFormatter>();
        list.add(ISODateTimeFormat.dateTime());
        list.add(ISODateTimeFormat.dateTimeNoMillis());
        return list;
    }

    public Date stringToDate(String date) {
        Date result = null;
        if (StringUtils.isNotBlank(date)) {
            List<DateTimeFormatter> dateFormatters = getDateFormatters();
            boolean converted = false;
            int formatter = 0;
            while(!converted) {
                try {
                    DateTimeFormatter dateTimeFormatter = dateFormatters.get(formatter);
                    DateTime dateTime = dateTimeFormatter.parseDateTime(date);
                    result = dateTime.toDate();
                    converted = true;
                } catch (IllegalArgumentException e) {
                    formatter++;
                    if (formatter > dateFormatters.size()) {
                        converted = true;
                    }
                    log.error("Could not find a valid date format for: \""+date+"\"", e);
                }
            }
        }
        return result;
    }

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(AuthorityValue.class);

    @Override
    public String toString() {
        return "AuthorityValue{" +
                "id='" + getId() + '\'' +
                ", authority_category='" + getCategory() + '\'' +
                ", value='" + value + '\'' +
                ", field='" + field + '\'' +
                ", creationDate=" + creationDate +
                ", deleted=" + deleted +
                ", lastModified=" + lastModified +
                ", verified=" + verified +
                '}';
    }

    public abstract String generateString();


    public String getAuthorityType() {
        return "internal";
    }

    /**
     * The regular equals() only checks if both AuthorityValues describe the same authority.
     * This method checks if the AuthorityValues have different information
     * E.g. it is used to decide when lastModified should be updated.
     * @param o object
     * @return true or false
     */
    public boolean hasTheSameInformationAs(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthorityValue that = (AuthorityValue) o;

        if (deleted != that.deleted) {
            return false;
        }
        if (field != null ? !field.equals(that.field) : that.field != null) {
            return false;
        }
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        if (verified != that.verified) {
            return false;
        }

        return true;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isVerified() {
        return verified;
    }

    /**
     * Replace an item's DCValue with this authority
     */
    public void updateItem(Item currentItem, Metadatum value, int confidence) {
        Metadatum newValue = value.copy();
        newValue.value = getValue();
        newValue.authority = getId();
        newValue.confidence = confidence;
        currentItem.replaceMetadataValue(value, newValue);
    }
}
