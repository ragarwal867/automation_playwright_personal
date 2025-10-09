package net.automation.clients.mail;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.automation.utils.ExceptionHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Fail.fail;

@Accessors(chain = true)
@Getter
public class EmailPart {
    @Setter
    private String filename;
    @Setter
    private String contentType;
    @Setter
    private byte[] content;

    private String contentTransferEncoding;
    private String contentEncoding;

    public EmailPart setContent(String stringContent, boolean base64, boolean compress) {
        content = stringContent.getBytes();

        if (compress) {
            content = compressGzip(content);
            contentEncoding = "gzip";
        }

        if (base64) {
            this.contentTransferEncoding = "base64";
        } else {
            this.contentTransferEncoding = "7bit";
        }

        return this;
    }

    public String getContentAsString(boolean decompress) {
        byte[] contentByte = decompress
                ? decompressGzip(this.content)
                : this.content;

        String stringContent = new String(contentByte, StandardCharsets.UTF_8);
        return stringContent;
    }

    private byte[] decompressGzip(byte[] compressedData) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, len);
            }

            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            fail("Cannot decompress Gzip. Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
            return null;
        }
    }

    private byte[] compressGzip(byte[] data) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            fail("Cannot compress to gzip. Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
            return null;
        }
    }
}
