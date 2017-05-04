/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.dspace.authority.AuthorityCategory;
import org.dspace.authority.AuthoritySearchService;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.orcid.OrcidAuthorityValue;
import org.dspace.authority.rest.RestSource;
import org.dspace.authority.service.CachedAuthorityService;
import org.dspace.core.ConfigurationManager;
import org.dspace.utils.DSpace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.authority.factory.AuthorityServiceFactory;

/**
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class SolrAuthority implements ChoiceAuthority {

    private static final Logger log = Logger.getLogger(SolrAuthority.class);
    private boolean externalResults = false;

    protected static CachedAuthorityService authorityService;

    public SolrAuthority() {
        authorityService = AuthorityServiceFactory.getInstance().getCachedAuthorityService();
    }

    public Choices getMatches(String field, String text, int collection, int start, int limit, String locale, boolean bestMatch) {

        if(limit == 0)
            limit = 10;

        SolrQuery queryArgs = new SolrQuery();
        if (text == null || text.trim().equals("")) {
            queryArgs.setQuery("*:*");
        } else {
//            String searchField = "value";
            String searchField = "search_field";
            String localSearchField = "";
            try {
                //A downside of the authors is that the locale is sometimes a number, make sure that this isn't one
                Integer.parseInt(locale);
                locale = null;
            } catch (NumberFormatException e) {
                //Everything is allright
            }
            if (locale != null && !"".equals(locale)) {
                localSearchField = searchField + "_" + locale;
            }

            String query = "(" + toQuery(searchField, text, bestMatch) + ") ";
            if (!localSearchField.equals("")) {
                query += " or (" + toQuery(localSearchField, text, bestMatch) + ")";
            }

//            AuthorityTypes authorityTypes = AuthorityValue.getAuthorityTypes();
//            AuthorityValue authority = authorityTypes.getFieldDefaults().get(field);
//            if (authority instanceof JournalAuthority) {
//                query += " or (" + toQuery("external_id", text) + ")"
//                        + " or (" + toQuery("issn", text) + ")"
//                        + " or (" + toQuery("publisher", text) + ")";
//            }

            queryArgs.setQuery(query);
        }
        AuthorityCategory category = authorityService.getCategory(field);

        queryArgs.addFilterQuery("authority_category:" + category);
        queryArgs.set(CommonParams.START, start);
        //We add one to our facet limit so that we know if there are more matches
        int maxNumberOfSolrResults = limit + 1;
        if(externalResults){
            maxNumberOfSolrResults = ConfigurationManager.getIntProperty("xmlui.lookup.select.size", 12);
        }
        queryArgs.set(CommonParams.ROWS, maxNumberOfSolrResults);

        String sortField = "value";
        String localSortField = "";
        if (StringUtils.isNotBlank(locale)) {
            localSortField = sortField + "_" + locale;
            queryArgs.setSortField(localSortField, SolrQuery.ORDER.asc);
        } else {
            queryArgs.setSortField(sortField, SolrQuery.ORDER.asc);
        }
        queryArgs.addFilterQuery("-deleted:true");

        Choices result;
        try {
            int max = 0;
            boolean hasMore = false;
            QueryResponse searchResponse = getSearchService().search(queryArgs);
            SolrDocumentList authDocs = searchResponse.getResults();
            ArrayList<Choice> choices = new ArrayList<Choice>();
            if (authDocs != null) {
                max = (int) searchResponse.getResults().getNumFound();
                int maxDocs = authDocs.size();
                if (limit < maxDocs)
                    maxDocs = limit;
                List<AuthorityValue> alreadyPresent = new ArrayList<AuthorityValue>();
                for (int i = 0; i < maxDocs; i++) {
                    SolrDocument solrDocument = authDocs.get(i);
                    if (solrDocument != null) {
                        AuthorityValue val = AuthorityServiceFactory.getInstance().getCachedAuthorityService().getAuthorityValueFromSolrDoc(solrDocument, "orcid_id");

                        Map<String, String> extras = val.choiceSelectMap();
                        extras.put("insolr", val.getId());
                        if(val instanceof OrcidAuthorityValue && StringUtils.isNotBlank(((OrcidAuthorityValue) val).getOrcid_id())){
                            extras.put("orcidID", ((OrcidAuthorityValue) val).getOrcid_id());
                        }
                        choices.add(new Choice(val.getId(), val.getValue(), val.getValue(), extras));
                        alreadyPresent.add(val);
                    }
                }

                if (externalResults && StringUtils.isNotBlank(text)) {
                    int sizeFromSolr = alreadyPresent.size();
                    int maxExternalResults = limit <= 10 ? Math.max(limit - sizeFromSolr, 2) : Math.max(limit - 10 - sizeFromSolr, 2) + limit - 10;
                    addExternalResults(field, text, choices, alreadyPresent, maxExternalResults);
                }


                // hasMore = (authDocs.size() == (limit + 1));
                hasMore = true;
            }


            int confidence;
            if (choices.size() == 0)
                confidence = Choices.CF_NOTFOUND;
            else if (choices.size() == 1)
                confidence = Choices.CF_UNCERTAIN;
            else
                confidence = Choices.CF_AMBIGUOUS;

            result = new Choices(choices.toArray(new Choice[choices.size()]), start, hasMore ? max : choices.size() + start, confidence, hasMore);
        } catch (Exception e) {
            log.error("Error while retrieving authority values {field: " + field + ", prefix:" + text + "}", e);
            result = new Choices(true);
        }

        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void addExternalResults(String field, String text, ArrayList<Choice> choices, List<AuthorityValue> alreadyPresent, int max) {
        final List<AuthorityValue> authorityValues = AuthorityServiceFactory.getInstance().getCachedAuthorityService().retrieveExternalResults(authorityService.getCategory(field), text, max * 2); // max*2 because results get filtered
        if(CollectionUtils.isNotEmpty(authorityValues)){
            try {
                // filtering loop
                Iterator<AuthorityValue> iterator = authorityValues.iterator();
                while (iterator.hasNext()) {
                    AuthorityValue next = iterator.next();
                    if (alreadyPresent.contains(next)) {
                        iterator.remove();
                    }
                }

                // adding choices loop
                int added = 0;
                iterator = authorityValues.iterator();
                while (iterator.hasNext() && added < max) {
                    AuthorityValue val = iterator.next();
                    Map<String, String> extras = val.choiceSelectMap();
                    extras.put("insolr", "false");
                    if(val instanceof OrcidAuthorityValue && StringUtils.isNotBlank(((OrcidAuthorityValue) val).getOrcid_id())){
                        extras.put("orcidID", ((OrcidAuthorityValue) val).getOrcid_id());
                    }
                    choices.add(new Choice(val.generateString(), val.getValue(), val.getValue(), extras));
                    added++;
                }
            } catch (Exception e) {
                log.error("Error", e);
            }
            this.externalResults = false;
        } else {
            log.warn("external source for authority not configured");
        }
    }

    private String toQuery(String searchField, String text, boolean bestMatch) {
        if (text.matches("^\".*\"$") || bestMatch) {
            //Searching for a full string
            return searchField + ":" + text.toLowerCase()+"*";
        } else {
            String query = "";
            String[] words = StringUtils.split(text, " ");
            for (int i = 0; i < words.length; i++)
            {
                String word = words[i];
                if (StringUtils.isNotBlank(query)) {
                    query += " AND ";
                }
                if (i == words.length - 1) {
                    query += "((" + searchField + ":" + ClientUtils.escapeQueryChars(word) + "*) OR (" + searchField + ":" + ClientUtils.escapeQueryChars(word) + "))";
                } else {
                    query += "(" + searchField + ":" + ClientUtils.escapeQueryChars(word) + ")";
                }
            }
            return query;
        }
    }

    @Override
    public Choices getMatches(String field, String text, int collection, int start, int limit, String locale) {
        return getMatches(field, text, collection, start, limit, locale, true);
    }

    @Override
    public Choices getBestMatch(String field, String text, int collection, String locale) {
        Choices matches = getMatches(field, text, collection, 0, 1, locale, true);
        if (matches.values.length !=0 && !matches.values[0].value.equalsIgnoreCase(text)) {
            matches = new Choices(false);
        }
        return matches;
    }

    @Override
    public String getLabel(String field, String key, String locale) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("requesting label for key " + key + " using locale " + locale);
            }
            SolrQuery queryArgs = new SolrQuery();
            queryArgs.setQuery("id:" + key);
            queryArgs.setRows(1);
            QueryResponse searchResponse = getSearchService().search(queryArgs);
            SolrDocumentList docs = searchResponse.getResults();
            if (docs.getNumFound() == 1) {
                String label = null;
                try {
                    label = (String) docs.get(0).getFieldValue("value_" + locale);
                } catch (Exception e) {
                    //ok to fail here
                }
                if (label != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("returning label " + label + " for key " + key + " using locale " + locale + " and fieldvalue " + "value_" + locale);
                    }
                    return label;
                }
                try {
                    label = (String) docs.get(0).getFieldValue("value");
                } catch (Exception e) {
                    log.error("couldn't get field value for key " + key,e);
                }
                if (label != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("returning label " + label + " for key " + key + " using locale " + locale + " and fieldvalue " + "value");
                    }
                    return label;
                }
                try {
                    label = (String) docs.get(0).getFieldValue("value_en");
                } catch (Exception e) {
                    log.error("couldn't get field value for key " + key,e);
                }
                if (label != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("returning label " + label + " for key " + key + " using locale " + locale + " and fieldvalue " + "value_en");
                    }
                    return label;
                }
            }
        } catch (Exception e) {
            log.error("error occurred while trying to get label for key " + key,e);
        }

        return key;
    }


    public static AuthoritySearchService getSearchService() {
        DSpace dspace = new DSpace();

        org.dspace.kernel.ServiceManager manager = dspace.getServiceManager();

        return manager.getServiceByName(AuthoritySearchService.class.getName(), AuthoritySearchService.class);
    }

    public void addExternalResultsInNextMatches() {
        this.externalResults = true;
    }
}
