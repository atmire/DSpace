/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import java.util.*;
import org.apache.commons.lang.*;
import org.apache.solr.common.*;
import org.dspace.authority.factory.*;

/**
 * AuthorityValueBuilder handles the creation of PersonAuthorityValues.
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public class PersonAuthorityValueBuilder<T extends PersonAuthorityValue> extends AuthorityValueBuilder<T> {

    /**
     * Build an authority value with the provided identifier and content
     * @param identifier
     * the authority identifier
     * @param content
     * the authority value
     * @return
     */
    @Override
    public T buildAuthorityValue(String identifier, String content)
    {
        final T authorityValue = buildAuthorityValue();
        authorityValue.setValue(content);
        return authorityValue;
    }

    /**
     * Build an authority value with the provided solr document
     * @param document
     * The solr document of the authority value
     * @return
     * The created authority value
     */
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

    /**
     * Build an empty authority value
     * @return
     * A new authority value
     */
    @Override
    public T buildAuthorityValue() {
        return (T) new PersonAuthorityValue();
    }
}
