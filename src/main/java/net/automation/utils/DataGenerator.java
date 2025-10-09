package net.automation.utils;

import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Random;
import java.util.UUID;

import static net.automation.utils.FileFactory.FILE_WRITERS;
import static org.assertj.core.api.Fail.fail;

public class DataGenerator {
    @Setter
    private static String testDataPath = System.getProperty("user.dir") + File.separator + "testdata";
    private static final Random random = new Random();

    public static synchronized long getUniqueNumber() {
        // Make sure there will be always a unique number returned
        ThreadHelper.sleep(1);
        return System.currentTimeMillis();
    }

    public static synchronized String generateNextId() {
        // Make sure the next number will be always unique
        ThreadHelper.sleep(2);

        LocalTime now = LocalTime.now();
        int hours = now.getHour();
        int minutes = now.getMinute();
        int seconds = now.getSecond();
        int milliseconds = now.getNano() / 1_000_000;

        String formattedHours = String.format("%02d", hours);
        String formattedMinutes = String.format("%02d", minutes);
        String formattedSeconds = String.format("%02d", seconds);
        String formattedMilliseconds = String.format("%03d", milliseconds);

        return formattedHours + formattedMinutes + formattedSeconds + formattedMilliseconds;
    }

    public static int getRandomNumber() {
        return random.nextInt(98099992);
    }

    public static String getUniqueString() {
        return UUID.randomUUID().toString();
    }

    public static String getUniqueId() {
        return "aut" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String getUniqueStringOnlyAlphanumeric() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static synchronized String createUniqueFile() {
        return createUniqueFile(FileType.TEXT);
    }

    public static synchronized String createUniqueFile(FileType fileType) {
        String uniqueFileName = "test_" + getUniqueNumber();
        String filePath = testDataPath + File.separator + uniqueFileName + fileType.getExtension();

        File file = new File(filePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            boolean directoryCreated = file.getParentFile().mkdirs();
            if (!directoryCreated) {
                fail("Cannot create file parent directory");
            }
        }

        FileFactory.FileContentWriter writer = FILE_WRITERS.get(fileType);
        if (writer != null) {
            try {
                writer.write(file, uniqueFileName);
            } catch (IOException e) {
                fail("Cannot create new file. Details: " + e.getMessage());
            }
        }

        return filePath;
    }

    public static void deleteTestFiles() {
        try {
            File directory = new File(testDataPath);
            FileUtils.deleteDirectory(directory);
        } catch (IOException e) {
            fail("Cannot delete test data dir. Details: " + e.getMessage());
        }
    }

    public static String getUniqueStringWithPrefix(final String prefix) {
        return "aut-" + prefix + getUniqueNumber();
    }
}
