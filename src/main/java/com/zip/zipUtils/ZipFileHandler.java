package com.zip.zipUtils;

import java.io.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipFileHandler {

    public static <T> T processZipFile(File file, ZipFileProcessor<T> processor) {
        try (ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ)) {
            return processor.process(zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Error processing zip file", e);
        }
    }

    public static void writeToZipOutputStream(OutputStream outputStream, ZipContentWriter writer) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream))) {
            writer.writeEntries(zipOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error writing zip file", e);
        }
    }
}
