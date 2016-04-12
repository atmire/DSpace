package org.dspace.authority;

import org.apache.commons.lang.StringUtils;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 11/04/16
 * Time: 12:22
 */
public class AuthorityKeyRepresentation
{
    private static final String SPLIT = "::";
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
        return result + internalIdentifier;
    }
}
