package com.zip.zipUtils;

import java.io.IOException;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface ZipFileReader<T> {
    T read(ZipFile zipFile) throws IOException;
}
