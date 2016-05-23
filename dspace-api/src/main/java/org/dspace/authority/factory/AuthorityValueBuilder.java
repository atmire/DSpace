/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.factory;

import org.apache.commons.collections.ListUtils;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityValue;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;
import java.util.List;

/**
 * Abstract class that is used to construct authority values,
 * if a new authority value is wanted a new builder is needed to construct the objects
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 *
 */
public abstract class AuthorityValueBuilder<T extends AuthorityValue>
{
    protected List<String> metadataFields;

    /**
     * Build an authority value with the provided identifier and content
     * @param identifier
     * the authority identifier
     * @param content
     * the authority value
     * @return
     */
    public abstract T buildAuthorityValue(String identifier, String content);

    /**
     * Build an authority value with the provided solr document, this method will set the general fields,
     * authority value specifid fields must be handled by overriding this method.
     *
     * @param document The solr document from which we will construct our authority value
     */
    public T buildAuthorityValue(SolrDocument document)
    {
        T authorityValue = buildAuthorityValue();
        authorityValue.setSolrId(String.valueOf(document.getFieldValue("id")));
        authorityValue.setField(String.valueOf(document.getFieldValue("field")));
        authorityValue.setValue(String.valueOf(document.getFieldValue("value")));
        authorityValue.setDeleted((Boolean) document.getFieldValue("deleted"));
        authorityValue.setCreationDate((Date) document.getFieldValue("creation_date"));
        authorityValue.setLastModified(((Date) document.getFieldValue("last_modified_date")));
        return authorityValue;
    }

    /**
     * Create an empty authorityValue object representation
     * @return a new authorityValue object
     */
    public abstract T buildAuthorityValue();

    /**
     * Get the list of metadata fields that this builder supports
     * @return
     */
    public List<String> getMetadataFields() {
        return metadataFields;
    }


    /**
     * Set the list of metadata fields that this builder supports
     * @param metadataFields the list of metadata fields
     */
    @Required
    public void setMetadataFields(List<String> metadataFields) {
        this.metadataFields = metadataFields;
    }

    /**
     * Query an external source for authority values, returns an empty list of values by default (if not external source is present).
     * @param text the query to be sent to the external source
     * @param max the maximum number of results returned.
     * @return a list of authority values returned from the third party source
     */
    public List<AuthorityValue> buildAuthorityValueFromExternalSource(String text, int max)
    {
        return ListUtils.EMPTY_LIST;
    }
}
