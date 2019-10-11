/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority.converter;

import org.apache.solr.common.SolrDocument;
import org.dspace.content.authority.AuthorityValue;
import org.dspace.external.model.ExternalDataObject;

/**
 * This serves as an interface to define methods that allow us to create AuthorityValue objects from various sources
 */
public interface AuthorityConverter {

    /**
     * This method will create an AuthorityValue from a given SolrDocument
     * @param solrDocument  The SolrDocument object that will be transformed into an AuthorityValue
     * @return              The corresponding AuthorityValue object
     */
    AuthorityValue createFromSolr(SolrDocument solrDocument);

    /**
     * This method will create an AuthorityValue object from an ExternalDataObject together with a given category
     * @param category      The category to be used for the AuthorityValue object
     * @param dataObject    The ExternalDataObject to be parsed to an AuthorityValue object
     * @return              The corresponding AuthorityValue object
     */
    AuthorityValue createFromExternalData(String category, ExternalDataObject dataObject);

}
