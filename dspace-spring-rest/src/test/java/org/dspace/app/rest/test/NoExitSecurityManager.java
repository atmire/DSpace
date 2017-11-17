package org.dspace.app.rest.test;

import java.security.Permission;

public class NoExitSecurityManager extends SecurityManager
{
    @Override
    public void checkPermission(Permission perm)
    {
        // allow anything.
    }
    @Override
    public void checkPermission(Permission perm, Object context)
    {
        // allow anything.
    }
    @Override
    public void checkExit(int status)
    {
        super.checkExit(status);
        if(status >= 0) {
            throw new ExitException(status);
        }
    }
}