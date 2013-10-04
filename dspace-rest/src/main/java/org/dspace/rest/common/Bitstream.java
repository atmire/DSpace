package org.dspace.rest.common;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: peterdietz
 * Date: 9/21/13
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class Bitstream {
    Logger log = Logger.getLogger(Bitstream.class);

    String bundleName;
    String name;
    String description;
    String format;
    String sizeBytes;

    String retrieveLink;

    final String type = "bitstream";
    Integer bitstreamID;

    Context context;

    public Bitstream() {

    }

    public Bitstream(Integer bitstreamID) {
        new Bitstream(bitstreamID, "");
    }

    public Bitstream(Integer bitstreamID, String expand) {
        try {
            if(context == null || !context.isValid()) {
                context = new Context();
            }

            org.dspace.content.Bitstream bitstream = org.dspace.content.Bitstream.find(context, bitstreamID);
            setup(bitstream, expand);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public Bitstream(org.dspace.content.Bitstream bitstream, String expand) {
        setup(bitstream, expand);
    }

    public void setup(org.dspace.content.Bitstream bitstream, String expand) {
        try {
            bitstreamID = bitstream.getID();
            bundleName = bitstream.getBundles()[0].getName();
            name = bitstream.getName();
            description = bitstream.getDescription();
            format = bitstream.getFormatDescription();
            sizeBytes = bitstream.getSize() + "";
            retrieveLink = "/bitstreams/" + bitstreamID + "/retrieve";
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public InputStream retrieve(org.dspace.content.Bitstream bitstream) throws SQLException, IOException, AuthorizeException {
        return bitstream.retrieve();
    }

    public String getBundleName() {
        return bundleName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFormat() {
        return format;
    }

    public String getSizeBytes() {
        return sizeBytes;
    }

    public String getType() {
        return type;
    }

    public Integer getBitstreamID() {
        return bitstreamID;
    }

    public String getRetrieveLink() {
        return retrieveLink;
    }


}
