package com.zip.zipUtils;

import java.io.IOException;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface ZipFunction<T> {
    T apply(ZipFile zipFile) throws IOException;
}
