/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

public class RelationshipValidationException extends Exception {
    /**
     * Create an empty Exception
     */
    public RelationshipValidationException() {
        super();
    }

    /**
     * Create an exception with only a message
     *
     * @param message message string
     */
    public RelationshipValidationException(String message) {
        super(message);
    }
}
