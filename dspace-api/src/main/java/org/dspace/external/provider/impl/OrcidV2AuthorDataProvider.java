package org.dspace.external.provider.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.external.provider.orcid.xml.XMLtoBio;
import org.dspace.authority.rest.RESTConnector;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.ExternalDataProvider;
import org.dspace.mock.MockMetadataValue;
import org.json.JSONObject;
import org.orcid.jaxb.model.record_v2.Person;
import org.springframework.beans.factory.annotation.Required;

public class OrcidV2AuthorDataProvider implements ExternalDataProvider {

    private static Logger log = LogManager.getLogger(OrcidV2AuthorDataProvider.class);

    public RESTConnector OrcidRestConnector;
    private String OAUTHUrl;
    private String clientId;

    private String clientSecret;

    private String accessToken;

    private String sourceIdentifier;
    private String orcidUrl;

    public static final String ORCID_ID_SYNTAX = "\\d{4}-\\d{4}-\\d{4}-(\\d{3}X|\\d{4})";

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
            is.close();
            streamReader.close();

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
        this.OrcidRestConnector = new RESTConnector(url);
    }

    @Override
    public Optional<ExternalDataObject> getExternalDataObject(String id) {
        Person person = getBio(id);
        ExternalDataObject externalDataObject = convertToExternalDataObject(person);
        return Optional.of(externalDataObject);
    }

    protected ExternalDataObject convertToExternalDataObject(Person person) {
        ExternalDataObject externalDataObject = new ExternalDataObject(sourceIdentifier);
        String lastName = "";
        String firstName = "";
        if (person.getName().getFamilyName() != null) {
            lastName = person.getName().getFamilyName().getValue();
            externalDataObject.addMetadata(new MockMetadataValue("person", "familyName", null, null,
                                                                 lastName));
        }
        if (person.getName().getGivenNames() != null) {
            firstName = person.getName().getGivenNames().getValue();
            externalDataObject.addMetadata(new MockMetadataValue("person", "givenName", null, null,
                                                                 firstName));

        }
        externalDataObject.setId(person.getName().getPath());
        externalDataObject
            .addMetadata(new MockMetadataValue("dc", "identifier", "orcid", null, person.getName().getPath()));
        externalDataObject
            .addMetadata(new MockMetadataValue("dc", "identifier", "uri", null, orcidUrl + person.getName().getPath()));
        if (!StringUtils.isBlank(lastName) && !StringUtils.isBlank(firstName)) {
            externalDataObject.setDisplayValue(lastName + ", " + firstName);
            externalDataObject.setValue(lastName + ", " + firstName);
        } else if (StringUtils.isBlank(firstName)) {
            externalDataObject.setDisplayValue(lastName);
            externalDataObject.setValue(lastName);
        } else if (StringUtils.isBlank(lastName)) {
            externalDataObject.setDisplayValue(firstName);
            externalDataObject.setValue(firstName);
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
        InputStream bioDocument = OrcidRestConnector.get(id + ((id.endsWith("/person")) ? "" : "/person"), accessToken);
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
        return StringUtils.isNotBlank(text) && text.matches(ORCID_ID_SYNTAX);
    }

    @Override
    public List<ExternalDataObject> searchExternalDataObjects(String query, int start, int limit) {
        if (limit > 100) {
            throw new IllegalArgumentException("The maximum number of results to retrieve cannot exceed 100.");
        }

        String searchPath = "search?q=" + URLEncoder.encode(query) + "&start=" + start + "&rows=" + limit;
        log.debug("queryBio searchPath=" + searchPath + " accessToken=" + accessToken);
        InputStream bioDocument = OrcidRestConnector.get(searchPath, accessToken);
        XMLtoBio converter = new XMLtoBio();
        List<Person> bios = converter.convert(bioDocument);
        if (bios == null) {
            return Collections.emptyList();
        } else {
            return bios.stream().map(bio -> convertToExternalDataObject(bio)).collect(Collectors.toList());
        }
    }

    @Override
    public boolean supports(String source) {
        return StringUtils.equalsIgnoreCase(sourceIdentifier, source);
    }


    @Required
    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    public String getOrcidUrl() {
        return orcidUrl;
    }

    @Required
    public void setOrcidUrl(String orcidUrl) {
        this.orcidUrl = orcidUrl;
    }

    public void setOAUTHUrl(String OAUTHUrl) {
        this.OAUTHUrl = OAUTHUrl;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
