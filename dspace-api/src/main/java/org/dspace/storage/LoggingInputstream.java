/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.storage;

import java.io.IOException;
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
        this.logNrLinesStackTrace(10);
    }

    private void logNrLinesStackTrace(int nrOfLines) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i <= nrOfLines; i++) {
            stringBuilder.append(stackTraceElements[i].toString() + "\n");
        }
        this.log.info(stringBuilder.toString());
    }

    @Override
    public int read() throws IOException {
        this.log.info(String.format("READING the %s with UUID %s", this.getClass().toString(), this.uuid.toString()));
        return this.inputStream.read();
    }

    @Override
    public void close() throws IOException {
        this.log.info(String.format("CLOSING the %s with UUID %s", this.getClass().toString(), this.uuid.toString()));
        this.logNrLinesStackTrace(10);
        this.inputStream.close();
    }

    @Override
    public int read(byte[] b) throws IOException {
        this.log.debug(String.format("READING (read(byte[])) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return this.inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.log.debug(String.format("READING (read(byte[], int, int)) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return this.inputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        this.log.debug(String.format("READING (readAllBytes()) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return this.inputStream.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        this.log.debug(String.format("READING (readNBytes(int)) the %s with UUID %s", this.getClass().toString(),
            this.uuid.toString()));
        return this.inputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        this.log.debug(String.format("READING (readNBytes(byte[], int, int)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return this.inputStream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        this.log.debug(String.format("SKIPPING (skip(long)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return this.inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        this.log.debug(String.format("AVAILABLE (available()) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return this.inputStream.available();
    }

    @Override
    public synchronized void mark(int readlimit) {
        this.log.debug(String.format("MARK (mark(int)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        this.inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.log.debug(String.format("RESET (reset()) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        this.inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        this.log.debug(String.format("MARK SUPPORTED (markSupported()) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return this.inputStream.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        this.log.debug(String.format("TRANSFER TO (transferTo(OutputStream)) the %s with UUID %s",
            this.getClass().toString(), this.uuid.toString()));
        return this.inputStream.transferTo(out);
    }
}
