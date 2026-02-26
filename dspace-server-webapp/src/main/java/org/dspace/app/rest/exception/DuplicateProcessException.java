/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.exception;

import org.dspace.core.I18nUtil;

/**
 * <p>Extend {@link UnprocessableEntityException} to provide a specific error message
 * in the REST response. The error message is added to the response in
 * {@link DSpaceApiExceptionControllerAdvice#handleCustomUnprocessableEntityException},
 * hence it should not contain sensitive or security-compromising info.</p>
 *
 * @author Bram Maegerman (bram.maegerman@atmire.com)
 */
public class DuplicateProcessException extends UnprocessableEntityException implements TranslatableException {

    public static final String MESSAGE_KEY = "org.dspace.app.rest.exception.DuplicateProcessException.message";

    public DuplicateProcessException() {
        super(I18nUtil.getMessage(MESSAGE_KEY));
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }
}
