package com.zip.zipUtils.read.inputstream;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipInputStreamReader {

    public static void forEachEntry(
            ZipInputStream zipInputStream,
            ZipInputStreamCallable<ZipEntry> consumer) throws IOException {

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            try {
                consumer.accept(entry);
            } finally {
                zipInputStream.closeEntry();
            }
        }
    }
}
