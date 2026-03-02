/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.exception;

import java.text.MessageFormat;

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

    public DuplicateProcessException(int processID) {
        super(formatMessage(I18nUtil.getMessage(MESSAGE_KEY), processID));
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY;
    }

    /**
     * @param formatStr string with placeholders, ideally obtained using {@link I18nUtil}
     * @param processID The processId of the existing process that is getting duplicated
     * @return message with the processId filled in
     */
    private static String formatMessage(String formatStr, int processID) {
        MessageFormat fmt = new MessageFormat(formatStr);
        String[] values = {
                String.valueOf(processID) // {0} in formatStr
        };
        return fmt.format(values);
    }
}
