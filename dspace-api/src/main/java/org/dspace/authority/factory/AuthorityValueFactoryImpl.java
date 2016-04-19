package org.dspace.authority.factory;

import java.util.*;
import org.apache.commons.collections.*;
import org.apache.log4j.*;
import org.apache.solr.common.*;
import org.dspace.authority.*;
import org.springframework.beans.factory.annotation.*;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 8/04/16
 * Time: 12:06
 */
public class AuthorityValueFactoryImpl extends AuthorityValueFactory {

    private static final Logger log = Logger.getLogger(AuthorityValueFactoryImpl.class);

    protected Map<String, AuthorityValueBuilder> authorityValueBuilderDefaults;

    protected Map<String, AuthorityValueBuilder> authorityValueBuilders;


    @Override
    public AuthorityValue createAuthorityValue(String field, String content)
    {
        //Retrieve the default authority value builder for this metadata field
        final AuthorityValueBuilder authorityValueBuilder = authorityValueBuilderDefaults.get(field);
        if(authorityValueBuilder == null)
        {
            throw new IllegalArgumentException("No default authority type for: " + field + ", check orcid-authority-services.xml");
        }
        return authorityValueBuilder.buildAuthorityValue(field, content);
    }

    @Override
    public AuthorityValue createAuthorityValue(String type, String identifier, String content)
    {
        //Retrieve the builder for this type
        final AuthorityValueBuilder authorityValueBuilder = authorityValueBuilders.get(type);
        if(authorityValueBuilder == null)
        {
            throw new IllegalArgumentException("Invalid authority type: " + type + ", check orcid-authority-services.xml");
        }
        return authorityValueBuilder.buildAuthorityValue(identifier, content);
    }

    @Override
    public AuthorityValue loadAuthorityValue(String type, SolrDocument solrDocument) {
        final AuthorityValueBuilder valueBuilder = authorityValueBuilders.get(type);
        if(valueBuilder == null)
        {
            throw new IllegalArgumentException("Invalid authority type: " + type + ", check orcid-authority-services.xml");
        }
        return valueBuilder.buildAuthorityValue(solrDocument);
    }

    @Override
    public List<AuthorityValue> retrieveExternalResults(String field, String text, int max) {
        for(String type : authorityValueBuilders.keySet())
        {
            final AuthorityValueBuilder valueBuilder = authorityValueBuilders.get(type);
            if(valueBuilder.getMetadataFields().contains(field))
            {
                return valueBuilder.buildAuthorityValueFromExternalSource(text, max);
            }
        }
        return ListUtils.EMPTY_LIST;
    }


    public Map<String, AuthorityValueBuilder> getAuthorityValueBuilderDefaults() {
        return authorityValueBuilderDefaults;
    }

    @Required
    public void setAuthorityValueBuilderDefaults(Map<String, AuthorityValueBuilder> authorityValueBuilderDefaults) {
        this.authorityValueBuilderDefaults = authorityValueBuilderDefaults;
    }

    public Map<String, AuthorityValueBuilder> getAuthorityValueBuilders() {
        return authorityValueBuilders;
    }

    @Required
    public void setAuthorityValueBuilders(Map<String, AuthorityValueBuilder> authorityValueBuilders) {
        this.authorityValueBuilders = authorityValueBuilders;
    }
}
