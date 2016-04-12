/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.orcid;

import org.dspace.authority.orcid.model.Bio;
import org.dspace.authority.orcid.model.Work;
import org.dspace.authority.orcid.xml.XMLtoBio;
import org.dspace.authority.orcid.xml.XMLtoWork;
import org.dspace.authority.rest.RESTConnector;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.net.URLEncoder;
import java.util.List;

/**
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class OrcidConnector
{

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(OrcidConnector.class);

    protected RESTConnector restConnector;


    private OrcidConnector(String url)
    {
        this.restConnector = new RESTConnector(url);
    }

    public Bio getBio(String id) {
        Document bioDocument = restConnector.get(id + "/orcid-bio");
        XMLtoBio converter = new XMLtoBio();
        Bio bio = converter.convert(bioDocument).get(0);
        bio.setOrcid(id);
        return bio;
    }

    public List<Work> getWorks(String id) {
        Document document = restConnector.get(id + "/orcid-works");
        XMLtoWork converter = new XMLtoWork();
        return converter.convert(document);
    }

    public List<Bio> queryBio(String name, int start, int rows) {
        Document bioDocument = restConnector.get("search/orcid-bio?q=" + URLEncoder.encode("\"" + name + "\"") + "&start=" + start + "&rows=" + rows);
        XMLtoBio converter = new XMLtoBio();
        return converter.convert(bioDocument);
    }

    /**
     * com.atmire.org.dspace.authority.rest.RestSource#queryAuthorities -> add field, so the source can decide whether to query /users or something else.
     * -> implement subclasses
     * -> implement usages
     */
    public List<Bio> queryBio(String text, int max) {
        return queryBio(text, 0, max);
    }

    public Bio queryAuthorityID(String id) {
        return getBio(id);
    }
}
