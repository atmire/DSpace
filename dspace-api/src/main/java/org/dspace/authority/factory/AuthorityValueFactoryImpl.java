/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.authority.factory;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityCategory;
import org.dspace.authority.AuthorityValue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * factory class to create AuthorityValues
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public class AuthorityValueFactoryImpl extends AuthorityValueFactory {

    private static final Logger log = Logger.getLogger(AuthorityValueFactoryImpl.class);
    private Map<String, AuthorityCategory> authorityValueFieldMapping;
    private String builderPrefix = "com.atmire.authority.builders.";
    private String builderPostfix = "AuthorityValueBuilder";
    private String idBuilderPrefix = "Identifiable";

    @Override
    public AuthorityValue createAuthorityValueFromField(String field, String content) {
        AuthorityCategory category = authorityValueFieldMapping.get(field);
        return createAuthorityValue(category, content);
    }

    @Override
    public AuthorityValue createAuthorityValue(AuthorityCategory category, String content) {
        AuthorityValueBuilder builder = retrieveBuilder(category);
        if (builder != null) {
            return builder.buildAuthorityValue(null, content);
        }
        return null;
    }

    @Override
    public AuthorityValue createAuthorityValue(AuthorityCategory category, String identifier, String content) {
        boolean identifiable = StringUtils.isNotBlank(identifier);
        AuthorityValueBuilder builder = retrieveBuilder(category, identifiable);
        if (builder != null) {
            return builder.buildAuthorityValue(identifier, content);
        }
        return null;
    }

    private AuthorityValueBuilder retrieveBuilder(AuthorityCategory category) {
        return retrieveBuilder(category, false);
    }

    private AuthorityValueBuilder retrieveBuilder(AuthorityCategory category, boolean identifiable) {
        if (category == null) {
            return null;
        }
        String builderName = builderPrefix;
        try {

            if (identifiable) {
                builderName += idBuilderPrefix;
            }
            builderName += category.toString() + builderPostfix;

            return (AuthorityValueBuilder) Class.forName(builderName).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            log.error("The builder class for " + builderName + "does not exist", e);
        }
        return null;
    }

    @Override
    public AuthorityValue loadAuthorityValue(AuthorityCategory category, SolrDocument solrDocument) {

        return loadAuthorityValue(category, solrDocument,null);
    }

    @Override
    public AuthorityValue loadAuthorityValue(AuthorityCategory category, SolrDocument solrDocument, String identifier) {
        boolean identifiable = StringUtils.isNotBlank(identifier);
        AuthorityValueBuilder builder = retrieveBuilder(category,identifiable);
        if (builder != null) {
            return builder.buildAuthorityValue(solrDocument);
        }
        return null;
    }

    /**
     * Retrieve external results for the provided authority field.
     *
     * @param category the authority controlled field
     * @param text  the text used to find the external results
     * @param max   the maximum amount of results
     * @return a list of authority values containing the external results
     */
    @Override
    public List<AuthorityValue> retrieveExternalResults(AuthorityCategory category, String text, int max) {
        AuthorityValueBuilder builder = retrieveBuilder(category, true);
        if (builder != null) {
            return builder.buildAuthorityValueFromExternalSource(text, max);
        }
        return ListUtils.EMPTY_LIST;
    }

    public void setAuthorityValueFieldMapping(Map<String, AuthorityCategory> authorityValueFieldMapping) {
        this.authorityValueFieldMapping = authorityValueFieldMapping;
    }

    public Map<String, AuthorityCategory> getAuthorityValueFieldMapping() {
        return authorityValueFieldMapping;
    }

    public boolean isAuthorityControlledField(String field) {
        return authorityValueFieldMapping.containsKey(field);
    }

    public AuthorityCategory getCategory(String field) {
        return authorityValueFieldMapping.get(field);
    }

    public List<String> getFieldKeys(AuthorityCategory category) {
        List<String> fieldKeys = new ArrayList<>();
        for (Map.Entry<String, AuthorityCategory> entry: this.authorityValueFieldMapping.entrySet()) {
            if (entry.getValue() == category) {
                fieldKeys.add(entry.getKey());
            }
        }
        return fieldKeys;
    }

    public List<String> getFieldKeys() {
        return new ArrayList<>(authorityValueFieldMapping.keySet());
    }

}
