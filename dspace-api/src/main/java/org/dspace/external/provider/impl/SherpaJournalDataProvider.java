package org.dspace.external.provider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.dspace.app.sherpa.SHERPAJournal;
import org.dspace.app.sherpa.SHERPAResponse;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.model.MockMetadataValue;
import org.dspace.external.provider.ExternalDataProvider;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SherpaJournalDataProvider implements ExternalDataProvider {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SherpaJournalDataProvider.class);

    private String url;
    private String sourceIdentifier;
    private String apiKey;

    private CloseableHttpClient client = null;

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void init() throws IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        // httpclient 4.3+ doesn't appear to have any sensible defaults any more. Setting conservative defaults as
        // not to hammer the SHERPA service too much.
        client = builder
            .disableAutomaticRetries()
            .setMaxConnTotal(5)
            .build();
    }

    public ExternalDataObject getExternalDataObject(String id) {

        HttpGet method = null;
        SHERPAResponse sherpaResponse = null;
        int timeout = 5000;
        URIBuilder uriBuilder = null;
        try {
            uriBuilder = new URIBuilder(url);
            uriBuilder.addParameter("issn", id);
            uriBuilder.addParameter("versions", "all");
            if (StringUtils.isNotBlank(apiKey)) {
                uriBuilder.addParameter("ak", apiKey);
            }

            method = new HttpGet(uriBuilder.build());
            method.setConfig(RequestConfig.custom()
                                          .setConnectionRequestTimeout(timeout)
                                          .setConnectTimeout(timeout)
                                          .setSocketTimeout(timeout)
                                          .build());
            // Execute the method.

            HttpResponse response = client.execute(method);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                sherpaResponse = new SHERPAResponse("SHERPA/RoMEO return not OK status: "
                                                        + statusCode);
            }

            HttpEntity responseBody = response.getEntity();

            if (null != responseBody) {
                sherpaResponse = new SHERPAResponse(responseBody.getContent());
            } else {
                sherpaResponse = new SHERPAResponse("SHERPA/RoMEO returned no response");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (sherpaResponse == null) {
            sherpaResponse = new SHERPAResponse(
                "Error processing the SHERPA/RoMEO answer");
        }
        if (CollectionUtils.isNotEmpty(sherpaResponse.getJournals())) {
            SHERPAJournal sherpaJournal = sherpaResponse.getJournals().get(0);

            ExternalDataObject externalDataObject = new ExternalDataObject();
            externalDataObject.setSource(sourceIdentifier);
            externalDataObject
                .addMetadata(new MockMetadataValue("dc", "title", null, null, sherpaJournal.getTitle(), null, 0));
            externalDataObject
                .addMetadata(new MockMetadataValue("dc", "identifier", "issn", null, sherpaJournal.getIssn(), null, 0));
            externalDataObject.setDisplayValue(sherpaJournal.getTitle());
            return externalDataObject;
        }
        return null;
    }

    public List<ExternalDataObject> searchExternalDataObjects(String query, int start, int limit) {
        // query args to add to SHERPA/RoMEO request URL
        List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
        args.add(new BasicNameValuePair("jtitle", query));
        args.add(new BasicNameValuePair("qtype", "contains"));
        args.add(new BasicNameValuePair("ak", apiKey));
        HttpClient hc = new DefaultHttpClient();
        String srUrl = url + "?" + URLEncodedUtils.format(args, "UTF8");
        HttpGet get = new HttpGet(srUrl);
        try {
            HttpResponse response = hc.execute(get);
            if (response.getStatusLine().getStatusCode() == 200) {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                SherpaJournalHandler handler = new SherpaJournalHandler(sourceIdentifier);

                // XXX FIXME: should turn off validation here explicitly, but
                //  it seems to be off by default.
                xr.setFeature("http://xml.org/sax/features/namespaces", true);
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);
                xr.parse(new InputSource(response.getEntity().getContent()));
                return Arrays.asList(handler.result);
            }
        } catch (IOException e) {
            log.error("SHERPA/RoMEO query failed: ", e);
            return null;
        } catch (ParserConfigurationException e) {
            log.warn("Failed parsing SHERPA/RoMEO result: ", e);
            return null;
        } catch (SAXException e) {
            log.warn("Failed parsing SHERPA/RoMEO result: ", e);
            return null;
        } finally {
            get.releaseConnection();
        }
        return null;
    }

    public boolean supports(String source) {
        return StringUtils.equalsIgnoreCase(sourceIdentifier, source);
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    // SAX handler to grab SHERPA/RoMEO (and eventually other details) from result
    private static class SherpaJournalHandler
        extends DefaultHandler {
        private ExternalDataObject result[] = null;
        int rindex = 0; // result index
        int total = 0;
        private String sourceIdentifier;

        protected String textValue = null;

        public SherpaJournalHandler(String sourceIdentifier) {
            super();
            this.sourceIdentifier = sourceIdentifier;
        }

        // NOTE:  text value MAY be presented in multiple calls, even if
        // it all one word, so be ready to splice it together.
        // BEWARE:  subclass's startElement method should call super()
        // to null out 'value'.  (Don't you miss the method combination
        // options of a real object system like CLOS?)
        @Override
        public void characters(char[] ch, int start, int length) {
            String newValue = new String(ch, start, length);
            if (newValue.length() > 0) {
                if (textValue == null) {
                    textValue = newValue;
                } else {
                    textValue += newValue;
                }
            }
        }

        // if this was the FIRST "numhits" element, it's size of results:
        @Override
        public void endElement(String namespaceURI, String localName,
                               String qName) {
            if (localName.equals("numhits")) {
                String stotal = textValue.trim();
                if (stotal.length() > 0) {
                    total = Integer.parseInt(stotal);
                    result = new ExternalDataObject[total];
                    if (total > 0) {
                        result[0] = new ExternalDataObject();
                        log.debug("Got " + total + " records in results.");
                    }
                }
            } else if (localName.equals("journal")) {
                // after start of result element, get next hit ready
                if (++rindex < result.length) {
                    ExternalDataObject externalDataObject = new ExternalDataObject();
                    externalDataObject.setSource(sourceIdentifier);
                    result[rindex] = externalDataObject;

                }
            } else if (localName.equals("jtitle") && textValue != null) {
                result[rindex].addMetadata(new MockMetadataValue("dc", "title", null, null, textValue.trim(), null, 0));
                result[rindex].setDisplayValue(textValue.trim());
            } else if ("issn" != null && localName.equals("issn") && textValue != null) {
                result[rindex]
                    .addMetadata(new MockMetadataValue("dc", "identifier", "issn", null, textValue.trim(), null, 0));
            } else if (localName.equals("message") && textValue != null) {
                // error message
                log.warn("SHERPA/RoMEO response error message: " + textValue.trim());
            }
        }

        // subclass overriding this MUST call it with super()
        @Override
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) {
            textValue = null;
        }

        @Override
        public void error(SAXParseException exception)
            throws SAXException {
            throw new SAXException(exception);
        }

        @Override
        public void fatalError(SAXParseException exception)
            throws SAXException {
            throw new SAXException(exception);
        }
    }
}
