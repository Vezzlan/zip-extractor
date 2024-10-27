package com.zip.zipUtils;

import java.io.IOException;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface ZipFileProcessor<T> {
    T process(ZipFile zipFile) throws IOException;
}
