package org.dspace.app.rest.test;

public class ExitException extends SecurityException
{
    private final int status;
    public ExitException(int status)
    {
        super("Process exited with code: " + status + ", you can ignore this exception");
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}