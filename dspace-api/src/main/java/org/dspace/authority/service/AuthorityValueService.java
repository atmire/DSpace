/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.service;

import java.util.*;
import org.apache.solr.common.*;
import org.dspace.authority.*;
import org.dspace.content.*;
import org.dspace.core.*;

/**
 * This service contains all methods for using authority values
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public interface AuthorityValueService
{

    public AuthorityValue generate(Context context, String authorityKey, String content, String field);

    public AuthorityValue update(AuthorityValue value);

    public AuthorityValue findByID(Context context, String authorityID);

    public List<AuthorityValue> findByExactValue(Context context, String field, String value);

    public List<AuthorityValue> findAll(Context context);

    public AuthorityValue fromSolr(SolrDocument solrDocument);

    public List<AuthorityValue> retrieveExternalResults(String field, String text, int max);

    public AuthorityValue prepareNextValue(Context context, Item item, String metadataField, MetadataValue value);
}
