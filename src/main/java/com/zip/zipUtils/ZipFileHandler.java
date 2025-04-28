package com.zip.zipUtils;

import java.io.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipFileHandler {

    public static <T> T useFile(File file, ZipFunction<T> zipFunction) {
        try (ZipFile zipFile = new ZipFile(file, ZipFile.OPEN_READ)) {
            return zipFunction.apply(zipFile);
        } catch (IOException e) {
            throw new RuntimeException("Error processing zip file", e);
        }
    }

    public static void writeTo(OutputStream outputStream, ZipCallable<ZipOutputStream> zipCallable) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipCallable.call(zipOutputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error writing zip file", e);
        }
    }
}
