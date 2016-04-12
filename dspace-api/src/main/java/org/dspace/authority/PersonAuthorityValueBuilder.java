package org.dspace.authority;

import org.apache.commons.lang.ObjectUtils;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.factory.AuthorityValueBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/16
 * Time: 16:24
 */
public class PersonAuthorityValueBuilder<T extends PersonAuthorityValue> extends AuthorityValueBuilder<T> {

    @Override
    public T buildAuthorityValue(String identifier, String content)
    {
        final T authorityValue = buildAuthorityValue();
        authorityValue.setValue(content);
        return authorityValue;
    }

    @Override
    public T buildAuthorityValue(SolrDocument document)
    {
        T authorityValue = super.buildAuthorityValue(document);
        authorityValue.setFirstName(ObjectUtils.toString(document.getFieldValue("first_name")));
        authorityValue.setLastName(ObjectUtils.toString(document.getFieldValue("last_name")));
        Collection<Object> document_name_variant = document.getFieldValues("name_variant");
        if (document_name_variant != null) {
            for (Object name_variants : document_name_variant) {
                authorityValue.addNameVariant(String.valueOf(name_variants));
            }
        }
        if (document.getFieldValue("institution") != null) {
            authorityValue.setInstitution(String.valueOf(document.getFieldValue("institution")));
        }

        Collection<Object> emails = document.getFieldValues("email");
        if (emails != null) {
            for (Object email : emails) {
                authorityValue.addEmail(String.valueOf(email));
            }
        }
        return authorityValue;
    }


    @Override
    public T buildAuthorityValue() {
        return (T) new PersonAuthorityValue();
    }
}
