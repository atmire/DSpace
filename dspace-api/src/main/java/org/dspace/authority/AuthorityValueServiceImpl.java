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
import org.apache.log4j.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.*;
import org.dspace.authority.factory.*;
import org.dspace.authority.indexer.*;
import org.dspace.authority.service.*;
import org.dspace.content.*;
import org.dspace.content.authority.*;
import org.dspace.content.service.*;
import org.dspace.core.*;
import org.dspace.core.LogManager;
import org.springframework.beans.factory.annotation.*;

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

    @Autowired
    protected AuthorityValueFactory authorityValueFactory;

    @Autowired(required = true)
    protected ItemService itemService;

    @Autowired(required = true)
    protected AuthorityIndexingService indexingService;

    @Autowired(required = true)
    protected AuthorityService authorityService;

    protected AuthorityValueServiceImpl()
    {

    }

    //TODO: rename this method
    @Override
    public AuthorityValue generate(Context context, String authorityKey, String content, String field) {
        AuthorityValue nextValue = generateRaw(authorityKey, content, field);
        nextValue.updateLastModifiedDate();
        nextValue.setCreationDate(new Date());
        nextValue.setField(field);

        return nextValue;
    }

    protected AuthorityValue generateRaw(String authorityKey, String content, String field) {
        AuthorityValue nextValue;

        AuthorityKeyRepresentation authorityKeyRepresentation = new AuthorityKeyRepresentation(authorityKey);
        if (StringUtils.isNotBlank(authorityKeyRepresentation.getInternalIdentifier())) {
            nextValue = authorityValueFactory.createAuthorityValue(authorityKeyRepresentation.getAuthorityType(), authorityKeyRepresentation.getInternalIdentifier(), content);
        } else {
            nextValue = authorityValueFactory.createAuthorityValue(field, content);
        }
        return nextValue;
    }

    @Override
    public AuthorityValue update(AuthorityValue value) {
        //TODO: Rewrite this one.
        AuthorityValue updated = generateRaw(value.generateString(), value.getValue(), value.getField());
        if (updated != null) {
            updated.setCreationDate(value.getCreationDate());
            updated.setField(value.getField());
            if (updated.hasTheSameInformationAs(value)) {
                updated.setLastModified(value.getLastModified());
            }else {
                updated.updateLastModifiedDate();
                authorityService.indexContent(updated);
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
    public AuthorityValue findByID(Context context, String authorityID) {
        //Ensure that if we use the full identifier to match on
        String queryString = "id:\"" + authorityID + "\"";
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
    public List<AuthorityValue> retrieveExternalResults(String field, String text, int max)
    {
        return authorityValueFactory.retrieveExternalResults(field, text, max);

    }

    @Override
    public AuthorityValue fromSolr(SolrDocument solrDocument) {
        String type = (String) solrDocument.getFieldValue("authority_type");
        return authorityValueFactory.loadAuthorityValue(type, solrDocument);
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

    /**
     * This method looks at the authority of a metadata.
     * If the authority can be found in solr, that value is reused.
     * Otherwise a new authority value will be generated that will be indexed in solr.
     * If the authority starts with AuthorityValueGenerator.GENERATE, a specific type of AuthorityValue will be generated.
     * Depending on the type this may involve querying an external REST service
     *
     * @param metadataField Is one of the fields defined in dspace.cfg to be indexed.
     * @param value         Is one of the values of the given metadataField in one of the items being indexed.
     */
    public AuthorityValue prepareNextValue(Context context, Item item, String metadataField, MetadataValue value)  {

        AuthorityValue nextValue = null;

        String content = value.getValue();
        String authorityKey = value.getAuthority();
        //We only want to update our item IF our ID is not present or if we need to generate one.
        boolean requiresItemUpdate = StringUtils.isBlank(authorityKey) || AuthorityKeyRepresentation.isAuthorityKeyRepresentation(authorityKey);

        if (StringUtils.isNotBlank(authorityKey) && !AuthorityKeyRepresentation.isAuthorityKeyRepresentation(authorityKey)) {
            // !uid.startsWith(AuthorityValueGenerator.GENERATE) is not strictly necessary here but it prevents exceptions in solr
            nextValue = findByID(context, authorityKey);
        }
        if (nextValue == null) {
            nextValue = generate(context, authorityKey, content, metadataField.replaceAll("\\.", "_"));
        }
        if (nextValue != null && requiresItemUpdate) {
            try {
                nextValue.updateItem(context, item, value);
                itemService.update(context, item);
            } catch (Exception e) {
                log.error("Error creating a metadata value's authority", e);
            }
        }

        return nextValue;
    }
}
