package com.zip.zipUtils.read;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public final class ZipReader {

    public static <T> T openAndApply(File file, ZipFunction<T> zipFunction) {
        try (ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ)) {
            return zipFunction.apply(zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Error processing zip file", e);
        }
    }
}
