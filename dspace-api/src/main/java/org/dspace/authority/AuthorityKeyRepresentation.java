/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import org.apache.commons.lang.StringUtils;

/**
 * AuthorityKeyRepresentation is used to generate a temporary authority key for an authority value,
 * until an authority is created or found for the authority value.
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public class AuthorityKeyRepresentation
{
    private static final String SPLIT = "::";

    /** The first part of the temporary authority key */
    private static final String GENERATE = "will be generated" + SPLIT;

    private String authorityType = null;
    private String internalIdentifier = null;

    public AuthorityKeyRepresentation(String authority)
    {
        if (isAuthorityKeyRepresentation(authority)) {
            String[] split = org.apache.commons.lang.StringUtils.split(authority, SPLIT);
            if (split.length > 0) {
                authorityType = split[1];
                if (split.length > 1) {
                    internalIdentifier = split[2];
                }
            }
        }
    }

    public AuthorityKeyRepresentation(String authorityType, String internalIdentifier)
    {
        this.authorityType = authorityType;
        this.internalIdentifier = internalIdentifier;
    }

    public String getAuthorityType() {
        return authorityType;
    }

    public String getInternalIdentifier() {
        return internalIdentifier;
    }

    /**
     * Check if an authority key is an authority key representation.
     * @param authorityKey
     * The authority key to check
     * @return
     * true if the authority key is an authority key representation
     */
    public static boolean isAuthorityKeyRepresentation(String authorityKey)
    {
        return StringUtils.startsWith(authorityKey, GENERATE);
    }

    @Override
    public String toString() {
        String result = GENERATE;
        if(StringUtils.isNotBlank(authorityType))
        {
            result += authorityType + SPLIT;
        }

        if(org.apache.commons.lang3.StringUtils.isBlank(internalIdentifier)){
            return result;
        }

        return result + internalIdentifier;
    }
}
