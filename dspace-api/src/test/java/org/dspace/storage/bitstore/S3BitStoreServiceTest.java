package org.dspace.storage.bitstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.dspace.content.Bitstream;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 17 Aug 2022
 */
public class S3BitStoreServiceTest {

    @Test
    public void testPutSmallStream() throws IOException {
        AmazonS3 s3Service = Mockito.mock(AmazonS3.class);
        Bitstream bitstream = Mockito.mock(Bitstream.class);
        PutObjectResult result = Mockito.mock(PutObjectResult.class);

        Mockito.when(bitstream.getInternalId()).thenReturn("internal-id");
        Mockito.when(s3Service.putObject(Matchers.any(PutObjectRequest.class))).thenReturn(result);
        Mockito.when(result.getETag()).thenReturn("checksum");

        S3BitStoreService s3BitStoreService = new S3BitStoreService();
        s3BitStoreService.setS3Service(s3Service);
        s3BitStoreService.put(bitstream, new ByteArrayInputStream("mock data.".getBytes(StandardCharsets.UTF_8)));

        Mockito.verify(bitstream, Mockito.times(1)).setSizeBytes(10L);
        Mockito.verify(bitstream, Mockito.times(1)).setChecksum("checksum");
        Mockito.verify(bitstream, Mockito.times(1)).setChecksumAlgorithm("MD5");
    }

    @Test
    public void testPutBigStream() throws IOException {
        AmazonS3 s3Service = Mockito.mock(AmazonS3.class);
        Bitstream bitstream = Mockito.mock(Bitstream.class);

        Mockito.when(bitstream.getInternalId()).thenReturn("internal-id");

        S3BitStoreService s3BitStoreService = new S3BitStoreService() {
            protected String multipartUpload(PutObjectRequest putObjectRequest) {
                return "checksum big";
            }
        };
        s3BitStoreService.setS3Service(s3Service);
        s3BitStoreService.put(bitstream, repeat("mock data.".getBytes(StandardCharsets.UTF_8), 11000000));

        Mockito.verify(bitstream, Mockito.times(1)).setSizeBytes(110000000L);
        Mockito.verify(bitstream, Mockito.times(1)).setChecksum("checksum big");
        Mockito.verify(bitstream, Mockito.times(1)).setChecksumAlgorithm("MD5");
    }

    public static InputStream repeat(byte[] sample, int times) {
        return new InputStream() {
            private long pos = 0;
            private final long total = (long) sample.length * times;

            public int read() throws IOException {
                return pos < total ?
                    sample[(int) (pos++ % sample.length)] :
                    -1;
            }
        };
    }

}