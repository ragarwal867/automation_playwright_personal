package net.automation.utils;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Fail.fail;

@UtilityClass
public class ZipHelper {
    public static InputStream createZipInputStream(Map<String, InputStream> filesToZip) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {
            for (Map.Entry<String, InputStream> entry : filesToZip.entrySet()) {
                String fileName = entry.getKey();
                InputStream fileStream = entry.getValue();

                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileStream.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();
                fileStream.close();
            }
        } catch (Exception ex) {
            fail("Cannot create zip file. Details: " + ExceptionHelper.getDetailedExceptionInfo(ex));
        }

        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public static Map<String, String> extractZipContent(byte[] zipBytes) {
        Map<String, String> zipContentMap = new HashMap<>();

        try (InputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
             ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream)) {

            ZipEntry entry;
            byte[] buffer = new byte[1024];
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    StringBuilder fileContent = new StringBuilder();
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        fileContent.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                    }
                    zipContentMap.put(entry.getName(), fileContent.toString());
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            fail("Cannot extract zip content. Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
        }

        return zipContentMap;
    }

    public Map<String, ByteArrayOutputStream> extractZipContent(ByteArrayOutputStream stream) {
        Map<String, ByteArrayOutputStream> files = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(stream.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream fileStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fileStream.write(buffer, 0, len);
                }
                files.put(entry.getName(), fileStream);
                zis.closeEntry();
            }
        } catch (IOException e) {
            fail("Cannot extract zip content. Details: " + ExceptionHelper.getDetailedExceptionInfo(e));
        }
        return files;
    }
}
