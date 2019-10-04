package org.dspace.external.provider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.mock.MockMetadataValue;
import org.dspace.external.provider.ExternalDataProvider;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SherpaPublisherDataProvider implements ExternalDataProvider {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SherpaPublisherDataProvider.class);

    private String sourceIdentifier;
    private String url;
    private String apiKey;

    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public Optional<ExternalDataObject> getExternalDataObject(String id) {
        List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
        args.add(new BasicNameValuePair("id", id));
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
                SherpaPublisherHandler handler = new SherpaPublisherHandler(sourceIdentifier);

                // XXX FIXME: should turn off validation here explicitly, but
                //  it seems to be off by default.
                xr.setFeature("http://xml.org/sax/features/namespaces", true);
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);
                xr.parse(new InputSource(response.getEntity().getContent()));
                List<ExternalDataObject> results = Arrays.asList(handler.result);
                if (results.size() == 1) {
                    return Optional.of(results.get(0));
                } else {
                    log.error("Something went wrong in the lookup for sherpa publishers with id:" + id);
                }
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

    public List<ExternalDataObject> searchExternalDataObjects(String query, int start, int limit) {
        List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
        args.add(new BasicNameValuePair("pub", query));
        args.add(new BasicNameValuePair("qtype", "all"));
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
                SherpaPublisherHandler handler = new SherpaPublisherHandler(sourceIdentifier);

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
    private static class SherpaPublisherHandler
        extends DefaultHandler {
        private ExternalDataObject result[] = null;
        int rindex = 0; // result index
        int total = 0;
        private String sourceIdentifier;

        protected String textValue = null;
        protected String currentId = null;

        public SherpaPublisherHandler(String sourceIdentifier) {
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
                        ExternalDataObject externalDataObject = new ExternalDataObject();
                        externalDataObject.setSource(sourceIdentifier);
                        result[0] = externalDataObject;
                        log.debug("Got " + total + " records in results.");
                    }
                }
            } else if (localName.equals("publisher")) {
                // after start of result element, get next hit ready
                if (++rindex < result.length) {
                    ExternalDataObject externalDataObject = new ExternalDataObject();
                    externalDataObject.setSource(sourceIdentifier);
                    result[rindex] = externalDataObject;

                }
            } else if (localName.equals("name") && textValue != null) {
                result[rindex].addMetadata(new MockMetadataValue("dc", "title", null, null, textValue.trim()));
                result[rindex].setDisplayValue(textValue.trim());
                result[rindex].setValue(textValue.trim());
                if (StringUtils.isNotBlank(currentId)) {
                    result[rindex].setId(currentId);
                    result[rindex].addMetadata(new MockMetadataValue("dc", "identifier", "sherpaPublisher", null, currentId));
                }
            } else if (localName.equals("homeurl") && textValue != null) {
                result[rindex]
                    .addMetadata(new MockMetadataValue("dc", "identifier", "other", null, textValue.trim()));
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
            if (StringUtils.equalsIgnoreCase(localName, "publisher")) {
                currentId = atts.getValue("id");
            }
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
