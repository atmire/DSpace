/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.mock.MockMetadataValue;

/**
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class AuthorityValue {

    /**
     * The text value of this authority
     */
    private String value;

    /**
     * When this authority record has been created
     */
    private Date creationDate;

    /**
     * represents the last time that DSpace got updated information from its external source
     */
    private Date lastModified;

    private List<MockMetadataValue> metadata;
    private String source;
    private String category;
    private String externalSourceIdentifier;


    /**
     * Generic getter for the metadata
     * @return the metadata value of this AuthorityValue
     */
    public List<MockMetadataValue> getMetadata() {
        return metadata;
    }

    /**
     * Generic setter for the metadata
     * @param metadata   The metadata to be set on this AuthorityValue
     */
    public void setMetadata(List<MockMetadataValue> metadata) {
        this.metadata = metadata;
    }

    /**
     * Generic getter for the source
     * @return the source value of this AuthorityValue
     */
    public String getSource() {
        return source;
    }

    /**
     * Generic setter for the source
     * @param source   The source to be set on this AuthorityValue
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Generic getter for the category
     * @return the category value of this AuthorityValue
     */
    public String getCategory() {
        return category;
    }

    /**
     * Generic setter for the category
     * @param category   The category to be set on this AuthorityValue
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Generic getter for the externalSourceIdentifier
     * @return the externalSourceIdentifier value of this AuthorityValue
     */
    public String getExternalSourceIdentifier() {
        return externalSourceIdentifier;
    }

    /**
     * Generic setter for the externalSourceIdentifier
     * @param externalSourceIdentifier   The externalSourceIdentifier to be set on this AuthorityValue
     */
    public void setExternalSourceIdentifier(String externalSourceIdentifier) {
        this.externalSourceIdentifier = externalSourceIdentifier;
    }

    public AuthorityValue() {
    }

    /**
     * This method will generate a unique key based on the Category, source and externalSourceIdentifier. This is done
     * to ensure that we'll always have a unique identifier for different objects but the same unique identifier for
     * the same object
     * @return The id for the authority value
     */
    public String getId() {
        String nonDigestedIdentifier;
        nonDigestedIdentifier = AuthorityValue.class.toString() + "field: " + category + "source: " + source
            + "externalSourceIdentifier: " + externalSourceIdentifier;
        // We return an md5 digest of the toString, this will ensure a unique identifier for the same value each time
        return DigestUtils.sha1Hex(nonDigestedIdentifier);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    protected void updateLastModifiedDate() {
        this.lastModified = new Date();
    }

    /**
     * log4j logger
     */
    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(AuthorityValue.class);

    @Override
    public String toString() {
        return "AuthorityValue{" +
            "id='" + getId() + '\'' +
            ", value='" + value + '\'' +
            ", source='" + source + '\'' +
            ", category='" + category + '\'' +
            ", externalSourceIdentifier='" + externalSourceIdentifier + '\'' +
            ", creationDate=" + creationDate +
            ", lastModified=" + lastModified +
            '}';
    }

    /**
     * The regular equals() only checks if both AuthorityValues describe the same authority.
     * This method checks if the AuthorityValues have different information
     * E.g. it is used to decide when lastModified should be updated.
     *
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

        if (CollectionUtils.isEqualCollection(metadata, that.metadata)) {
            return false;
        }
        if (!StringUtils.equals(source, that.source)) {
            return false;
        }
        if (!StringUtils.equals(category, that.category)) {
            return false;
        }
        if (!StringUtils.equals(externalSourceIdentifier, that.externalSourceIdentifier)) {
            return false;
        }
        if (!StringUtils.equals(getId(), that.getId())) {
            return false;
        }
        if (!StringUtils.equals(value, that.value)) {
            return false;
        }

        return true;
    }
}
