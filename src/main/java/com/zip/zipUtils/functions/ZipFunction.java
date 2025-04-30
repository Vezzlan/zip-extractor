package com.zip.zipUtils.functions;

import java.io.IOException;
import java.util.zip.ZipFile;

@FunctionalInterface
public interface ZipFunction<T> {
    T apply(ZipFile zipFile) throws IOException;
}
