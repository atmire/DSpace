/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.storage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A debugging alternative to an {@link InputStream} to know when we're opening/closing it
 *
 * @author Marie Verdonck (Atmire) on 26/10/21
 */
public class LoggingInputstream extends InputStream {
    private static final Logger log = LogManager.getLogger(LoggingInputstream.class);

    private UUID uuid;
    private InputStream inputStream;

    public LoggingInputstream(InputStream inputStream) {
        this.uuid = UUID.randomUUID();
        this.inputStream = inputStream;
        this.log.info(String.format("CREATING the %s with UUID %s", this.getClass().toString(), this.uuid.toString()));
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < 10; i++) {
            this.log.info(stackTraceElements[i].toString());
        }
    }

    @Override
    public int read() {
        this.log.info(String.format("READING the %s with UUID %s", this.getClass().toString(), this.uuid.toString()));
        return 0;
    }

    @Override
    public void close() {
        this.log.info(String.format("CLOSING the %s with UUID %s", this.getClass().toString(), this.uuid.toString()));
    }

    @Override
    public int read(byte[] b) {
        this.log.debug(String.format("READING (read(byte[])) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return 0;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        this.log.debug(String.format("READING (read(byte[], int, int)) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return 0;
    }

    @Override
    public byte[] readAllBytes() {
        this.log.debug(String.format("READING (readAllBytes()) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return new byte[0];
    }

    @Override
    public byte[] readNBytes(int len) {
        this.log.debug(String.format("READING (readNBytes(int)) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return new byte[0];
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) {
        this.log.debug(String.format("READING (readNBytes(byte[], int, int)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return 0;
    }

    @Override
    public long skip(long n) {
        this.log.debug(String.format("SKIPPING (skip(long)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return 0;
    }

    @Override
    public int available() {
        this.log.debug(String.format("AVAILABLE (available()) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return 0;
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.log.debug(String.format("MARK (mark(int)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
    }

    @Override
    public synchronized void reset() {
        this.log.debug(String.format("RESET (reset()) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
    }

    @Override
    public boolean markSupported() {
        this.log.debug(String.format("MARK SUPPORTED (markSupported()) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return false;
    }

    @Override
    public long transferTo(OutputStream out) {
        this.log.debug(String.format("TRANSFER TO (transferTo(OutputStream)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return 0;
    }
}
