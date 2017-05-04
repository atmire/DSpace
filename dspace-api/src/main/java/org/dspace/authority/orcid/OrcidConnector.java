/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.authority.orcid;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.dspace.authority.orcid.xml.XMLtoBio;
import org.dspace.authority.orcid.xml.XMLtoWork;
import org.dspace.authority.rest.RESTConnector;
import org.json.JSONObject;
import org.orcid.jaxb.model.record_v2.Person;
import org.orcid.jaxb.model.record_v2.Work;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.List;

/**
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class OrcidConnector {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(OrcidConnector.class);

    public RESTConnector restConnector;
    private String OAUTHUrl;
    private String clientId;

    private String clientSecret;

    private String accessToken;

    public void init() throws IOException {
        if (StringUtils.isNotBlank(accessToken) && StringUtils.isNotBlank(clientSecret)) {
            String authenticationParameters = "?client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=/read-public&grant_type=client_credentials";
            HttpPost httpPost = new HttpPost(OAUTHUrl + authenticationParameters);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse getResponse = httpClient.execute(httpPost);

            InputStream is = getResponse.getEntity().getContent();
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            JSONObject responseObject = null;
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null && responseObject == null) {
                if (inputStr.startsWith("{") && inputStr.endsWith("}") && inputStr.contains("access_token")) {
                    try {
                        responseObject = new JSONObject(inputStr);
                    } catch (Exception e) {
                        //Not as valid as I'd hoped, move along
                        responseObject = null;
                    }
                }
            }

            if (responseObject != null && responseObject.has("access_token")) {
                accessToken = (String) responseObject.get("access_token");
            }
        }

    }

    private OrcidConnector(String url, String OAUTHUrl, String clientId, String clientSecret) {
        this.restConnector = new RESTConnector(url);
        this.OAUTHUrl = OAUTHUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private OrcidConnector(String url, String OAUTHUrl) {
        this.restConnector = new RESTConnector(url);
        this.OAUTHUrl = OAUTHUrl;
    }

    public Person getBio(String id) {
        InputStream bioDocument = restConnector.get(id + ((id.endsWith("/person")) ? "" : "/person"), accessToken);
        XMLtoBio converter = new XMLtoBio();
        Person person = converter.convertSinglePerson(bioDocument);
        return person;
    }

    public List<Work> getWorks(String id) {
        InputStream document = restConnector.get(id + "/works", accessToken);
        XMLtoWork converter = new XMLtoWork();
        List<Work> workSummaries = converter.convert(document);
        return workSummaries;
    }

    public Work getWork(String path) {
        InputStream document = restConnector.get(path, accessToken);
        XMLtoWork converter = new XMLtoWork();
        Work work = converter.toWork(document);
        return work;
    }

    public List<Person> queryBio(String name, int start, int rows) {
        if (rows > 100) {
            throw new IllegalArgumentException("The maximum number of results to retrieve from solr cannot exceed 100.");
        }
        final String[] nameParts = StringUtils.split(name);
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++) {
            String namePart = nameParts[i];
            query.append("given-names:").append(namePart).append("*").append(" OR family-name:").append(name).append("*");
            query.append(" OR orcid: ").append(name);
            if (i < nameParts.length - 1) {
                query.append(" OR ");
            }
        }


        String searchPath = "search?q=" + URLEncoder.encode(query.toString()) + "&start=" + start + "&rows=" + rows;
        InputStream bioDocument = restConnector.get(searchPath, accessToken);
        XMLtoBio converter = new XMLtoBio();
        List<Person> bios = converter.convert(bioDocument);
        return bios;
    }

    /**
     * com.atmire.org.dspace.authority.rest.RestSource#queryAuthorities -> add field, so the source can decide whether to query /users or something else.
     * -> implement subclasses
     * -> implement usages
     */
    public List<Person> queryBio(String text, int max) {
        return queryBio(text, 0, max);
    }

    public Person queryAuthorityID(String id) {
        return getBio(id);
    }
}
