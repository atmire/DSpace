package org.dspace.external.provider.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.authority.orcid.xml.XMLtoBio;
import org.dspace.authority.rest.RESTConnector;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.model.MockMetadataValue;
import org.dspace.external.provider.ExternalDataProvider;
import org.json.JSONObject;
import org.orcid.jaxb.model.record_v2.Person;

public class OrcidV2AuthorDataProvider implements ExternalDataProvider {

    private static Logger log = LogManager.getLogger(OrcidV2AuthorDataProvider.class);

    public RESTConnector restConnector;
    private String OAUTHUrl;
    private String clientId;

    private String clientSecret;

    private String accessToken;

    private String sourceIdentifier;

    @Override
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    /**
     * Initialize the accessToken that is required for all subsequent calls to ORCID.
     *
     * @throws java.io.IOException passed through from HTTPclient.
     */
    public void init() throws IOException {
        if (StringUtils.isNotBlank(accessToken) && StringUtils.isNotBlank(clientSecret)) {
            String authenticationParameters = "?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&scope=/read-public&grant_type=client_credentials";
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

    /**
     * Makes an instance of the Orcidv2 class based on the provided parameters.
     * This constructor is called through the spring bean initialization
     */
    private OrcidV2AuthorDataProvider(String url) {
        this.restConnector = new RESTConnector(url);
    }

    @Override
    public ExternalDataObject getExternalDataObject(String id) {
        Person person = getBio(id);
        ExternalDataObject externalDataObject = convertToExternalDataObject(person);
        return externalDataObject;
    }

    private ExternalDataObject convertToExternalDataObject(Person person) {
        ExternalDataObject externalDataObject = new ExternalDataObject(sourceIdentifier, new LinkedList<>(), "");
        String lastName = "";
        String firstName = "";
        if (person.getName().getFamilyName() != null) {
            lastName = person.getName().getFamilyName().getValue();
            externalDataObject.addMetadata(new MockMetadataValue("person", "familyName", null, null,
                                                                 lastName, null, 0));
        }
        if (person.getName().getGivenNames() != null) {
            firstName = person.getName().getGivenNames().getValue();
            externalDataObject.addMetadata(new MockMetadataValue("person", "givenName", null, null,
                                                                 firstName, null, 0));

        }
        externalDataObject.addMetadata(new MockMetadataValue("dc", "identifier", "orcid", null, person.getName().getPath(), null, 0));
        externalDataObject.addMetadata(new MockMetadataValue("dc", "identifier", "uri", null, "https://orcid.org/" + person.getName().getPath(), null, 0));
        if (!StringUtils.isBlank(lastName) && !StringUtils.isBlank(firstName)) {
            externalDataObject.setDisplayValue(lastName + ", " + firstName);
        } else if (StringUtils.isBlank(firstName)) {
            externalDataObject.setDisplayValue(lastName);
        } else if (StringUtils.isBlank(lastName)) {
            externalDataObject.setDisplayValue(firstName);
        }
        return externalDataObject;
    }

    /**
     * Retrieve a Person object based on a given orcid identifier
     * @param id orcid identifier
     * @return Person
     */
    public Person getBio(String id) {
        log.debug("getBio called with ID=" + id);
        if (!isValid(id)) {
            return null;
        }
        InputStream bioDocument = restConnector.get(id + ((id.endsWith("/person")) ? "" : "/person"), accessToken);
        XMLtoBio converter = new XMLtoBio();
        Person person = converter.convertSinglePerson(bioDocument);
        return person;
    }

    /**
     * Check to see if the provided text has the correct ORCID syntax.
     * Since only searching on ORCID id is allowed, this way, we filter out any queries that would return a
     * blank result anyway
     */
    private boolean isValid(String text) {
        return StringUtils.isNotBlank(text) && text.matches(Orcidv2AuthorityValue.ORCID_ID_SYNTAX);
    }

    @Override
    public List<ExternalDataObject> searchExternalDataObjects(String query, int start, int limit) {
        if (limit > 100) {
            throw new IllegalArgumentException("The maximum number of results to retrieve cannot exceed 100.");
        }

        String searchPath = "search?q=" + URLEncoder.encode(query) + "&start=" + start + "&rows=" + limit;
        log.debug("queryBio searchPath=" + searchPath + " accessToken=" + accessToken);
        InputStream bioDocument = restConnector.get(searchPath, accessToken);
        XMLtoBio converter = new XMLtoBio();
        List<Person> bios = converter.convert(bioDocument);
        if (bios == null) {
            return null;
        } else {
            return bios.stream().map(bio -> convertToExternalDataObject(bio)).collect(Collectors.toList());
        }
    }

    @Override
    public boolean supports(String source) {
        return StringUtils.equalsIgnoreCase(sourceIdentifier, source);
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }
}
