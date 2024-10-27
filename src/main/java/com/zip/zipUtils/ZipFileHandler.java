package com.zip.zipUtils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class ZipFileHandler {

    public static <T> T withZipFile(File file, ZipFileProcessor<T> processor) {
        try (ZipFile zipFile = new ZipFile(file)) {
            return processor.process(zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Error processing zip file", e);
        }
    }
}
