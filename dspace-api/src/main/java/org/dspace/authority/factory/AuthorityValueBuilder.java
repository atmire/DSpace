package org.dspace.authority.factory;

import org.apache.commons.collections.ListUtils;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityValue;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;
import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/16
 * Time: 15:42
 */
public abstract class AuthorityValueBuilder<T extends AuthorityValue>
{
    protected List<String> metadataFields;

    public abstract T buildAuthorityValue(String identifier, String content);

    /**
     * Build an authority value with the provided solr document
     */
    public T buildAuthorityValue(SolrDocument document)
    {
        T authorityValue = buildAuthorityValue();
        authorityValue.setField(String.valueOf(document.getFieldValue("field")));
        authorityValue.setValue(String.valueOf(document.getFieldValue("value")));
        authorityValue.setDeleted((Boolean) document.getFieldValue("deleted"));
        authorityValue.setCreationDate((Date) document.getFieldValue("creation_date"));
        authorityValue.setLastModified(((Date) document.getFieldValue("last_modified_date")));
        return authorityValue;
    }

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
     * @param metadataFields
     */
    @Required
    public void setMetadataFields(List<String> metadataFields) {
        this.metadataFields = metadataFields;
    }

    public List<AuthorityValue> buildAuthorityValueFromExternalSource(String text, int max)
    {
        return ListUtils.EMPTY_LIST;
    }
}
