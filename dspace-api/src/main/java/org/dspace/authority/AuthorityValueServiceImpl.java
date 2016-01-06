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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.content.authority.SolrAuthority;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * This service contains all methods for using authority values
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class AuthorityValueServiceImpl implements AuthorityValueService{

    private final Logger log = Logger.getLogger(AuthorityValueServiceImpl.class);

    @Autowired(required = true)
    protected AuthorityTypes authorityTypes;

    protected AuthorityValueServiceImpl()
    {

    }

    @Override
    public AuthorityValue generate(Context context, String authorityKey, String content, String field) {
        AuthorityValue nextValue = null;

        nextValue = generateRaw(authorityKey, content, field);


        if (nextValue != null) {
            //Only generate a new UUID if there isn't one offered OR if the identifier needs to be generated
            if (StringUtils.isBlank(authorityKey)) {
                // An existing metadata without authority is being indexed
                // If there is an exact match in the index, reuse it before adding a new one.
                List<AuthorityValue> byValue = findByExactValue(context, field, content);
                if (byValue != null && !byValue.isEmpty()) {
                    authorityKey = byValue.get(0).getId();
                } else {
                    authorityKey = UUID.randomUUID().toString();
                }
            } else if (StringUtils.startsWith(authorityKey, GENERATE)) {
                authorityKey = UUID.randomUUID().toString();
            }

            nextValue.setId(authorityKey);
            nextValue.updateLastModifiedDate();
            nextValue.setCreationDate(new Date());
            nextValue.setField(field);
        }

        return nextValue;
    }

    protected AuthorityValue generateRaw(String authorityKey, String content, String field) {
        AuthorityValue nextValue;
        if (authorityKey != null && authorityKey.startsWith(GENERATE)) {
            String[] split = StringUtils.split(authorityKey, SPLIT);
            String type = null, info = null;
            if (split.length > 0) {
                type = split[1];
                if (split.length > 1) {
                    info = split[2];
                }
            }
            AuthorityValue authorityType = authorityTypes.getEmptyAuthorityValue(type);
            nextValue = authorityType.newInstance(info);
        } else {
            Map<String, AuthorityValue> fieldDefaults = authorityTypes.getFieldDefaults();
            nextValue = fieldDefaults.get(field).newInstance(null);
            if (nextValue == null) {
                nextValue = new AuthorityValue();
            }
            nextValue.setValue(content);
        }
        return nextValue;
    }

    @Override
    public AuthorityValue update(AuthorityValue value) {
        AuthorityValue updated = generateRaw(value.generateString(), value.getValue(), value.getField());
        if (updated != null) {
            updated.setId(value.getId());
            updated.setCreationDate(value.getCreationDate());
            updated.setField(value.getField());
            if (updated.hasTheSameInformationAs(value)) {
                updated.setLastModified(value.getLastModified());
            }else {
                updated.updateLastModifiedDate();
            }
        }
        return updated;
    }

    /**
     * Item.ANY does not work here.
     * @param context Context
     * @param authorityID authority id
     * @return AuthorityValue
     */
    @Override
    public AuthorityValue findByUID(Context context, String authorityID) {
        //Ensure that if we use the full identifier to match on
        String queryString = "id:\"" + authorityID + "\"";
        List<AuthorityValue> findings = find(context, queryString);
        return findings.size() > 0 ? findings.get(0) : null;
    }

    @Override
    public AuthorityValue findByOrcidID(Context context, String orcid_id) {
        String queryString = "orcid_id:" + orcid_id;
        List<AuthorityValue> findings = find(context, queryString);
        return findings.size() > 0 ? findings.get(0) : null;
    }

    @Override
    public List<AuthorityValue> findByExactValue(Context context, String field, String value) {
        String queryString = "value_keyword:\"" + value + "\" AND field:" + field;
        return find(context, queryString);
    }

    @Override
    public List<AuthorityValue> findAll(Context context) {
        String queryString = "*:*";
        int rows = 1000;
        int start = 0;
        boolean hasMore = true;
        List<AuthorityValue> allAuthorityValues = new ArrayList<>();

        while(hasMore){
            List<AuthorityValue> authorityValuesPart = find(context, queryString, start, rows);

            if (authorityValuesPart.size()<rows) {
                hasMore = false;
            }

            allAuthorityValues.addAll(authorityValuesPart);
            start+=rows;
        }

        return allAuthorityValues;
    }

    @Override
    public AuthorityValue fromSolr(SolrDocument solrDocument) {
        String type = (String) solrDocument.getFieldValue("authority_type");
        AuthorityValue value = authorityTypes.getEmptyAuthorityValue(type);
        value.setValues(solrDocument);
        return value;
    }

    @Override
    public AuthorityValue getAuthorityValueType(String metadataString) {
        AuthorityValue fromAuthority = null;
        for (AuthorityValue type : authorityTypes.getTypes()) {
            if (StringUtils.startsWithIgnoreCase(metadataString,type.getAuthorityType())) {
                fromAuthority = type;
            }
        }
        return fromAuthority;
    }

    protected List<AuthorityValue> find(Context context, String queryString){
        return find(context,queryString,0,10);
    }

    protected List<AuthorityValue> find(Context context, String queryString, int start, int rows) {
        List<AuthorityValue> findings = new ArrayList<AuthorityValue>();
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(filtered(queryString));
            solrQuery.setStart(start);
            solrQuery.setRows(rows);
            log.debug("AuthorityValueFinder makes the query: " + queryString);
            QueryResponse queryResponse = SolrAuthority.getSearchService().search(solrQuery);
            if (queryResponse != null && queryResponse.getResults() != null && 0 < queryResponse.getResults().getNumFound()) {
                for (SolrDocument document : queryResponse.getResults()) {
                    AuthorityValue authorityValue = fromSolr(document);
                    findings.add(authorityValue);
                    log.debug("AuthorityValueFinder found: " + authorityValue.getValue());
                }
            }
        } catch (Exception e) {
            log.error(LogManager.getHeader(context, "Error while retrieving AuthorityValue from solr", "query: " + queryString),e);
        }

        return findings;
    }

    protected String filtered(String queryString) throws InstantiationException, IllegalAccessException {
        String instanceFilter = "-deleted:true";
        if (StringUtils.isNotBlank(instanceFilter)) {
            queryString += " AND " + instanceFilter;
        }
        return queryString;
    }

    protected String fieldParameter(String schema, String element, String qualifier) {
        return schema + "_" + element + ((qualifier != null) ? "_" + qualifier : "");
    }
}
