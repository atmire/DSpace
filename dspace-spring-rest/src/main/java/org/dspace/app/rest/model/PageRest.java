/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.RestResourceController;

/**
 * The Page REST object
 */
public class PageRest extends BaseObjectRest<UUID> {
    /**
     * The NAME to be used for this REST object
     */
    public static final String NAME = "page";
    /**
     * The CATEGORY to be used for this REST object
     */
    public static final String CATEGORY = RestAddressableModel.CONFIGURATION;

    /**
     * Tha name property of the PageRest object
     */
    private String name;
    /**
     * The title property of the PageRest object
     */
    private String title;
    /**
     * The language property of the PageRest object
     */
    private String language;

    private long sizeBytes;

    /**
     * The BitstreamRest object that is connected to this PageRest object
     */
//    private BitstreamRest content;

    private BitstreamFormatRest bitstreamFormat;
    /**
     * Generic getter for the BitstreamRest property of this PageRest object.
     * This annotation will make sure that the Bitstream is added as an embed and link for the PageResource object.
     * @return  The bitstreamRest property for this PageRest object
     */
//    @LinkRest(linkClass = BitstreamRest.class, name = "content")
//    @JsonIgnore
//    public BitstreamRest getBitstreamRest() {
//        return content;
//    }

    @LinkRest(linkClass = BitstreamFormatRest.class, name = "format")
    @JsonIgnore
    public BitstreamFormatRest getBitstreamFormatRest() {
        return bitstreamFormat;
    }

    public void setBitstreamFormat(BitstreamFormatRest bitstreamFormat) {
        this.bitstreamFormat = bitstreamFormat;
    }
    /**
     * Generic setter for the bitstreamRest property
     * @param bitstreamRest The bitstreamRest object that the property should be set to
     */
//    public void setBitstreamRest(BitstreamRest bitstreamRest) {
//        this.content = bitstreamRest;
//    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
    /**
     * Generic getter for the name property
     * @return  The name property of this PageRest object
     */
    public String getName() {
        return name;
    }

    /**
     * Generic setter for the name property
     * @param name  The name String that this property should be set to
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Generic getter for the title property
     * @return  The title property of this PageRest object
     */
    public String getTitle() {
        return title;
    }

    /**
     * Generic setter for the title property
     * @param title The title String property that this title property should be set to
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Generic getter for the language property
     * @return  The language property of this PageRest object
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Generic setter for the language property
     * @param language  The language String property that this language property should be set to
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Generic getter for the CATEGORY property of this PageRest object
     * @return  The CATEGORY property of this PageRest object
     */
    public String getCategory() {
        return CATEGORY;
    }

    /**
     * Generic getter for the ControllerClass of this PageRest object
     * @return  The class that will be used as a control for this PageRest object
     */
    public Class getController() {
        return RestResourceController.class;
    }

    /**
     * Generic getter for the type property of this PageRest object
     * @return  The NAME property of this PageRest object
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getType() {
        return NAME;
    }
}
