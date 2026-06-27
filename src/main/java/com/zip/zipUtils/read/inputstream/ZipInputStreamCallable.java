package com.zip.zipUtils.read.inputstream;

import java.io.IOException;

@FunctionalInterface
public interface ZipInputStreamCallable<T> {
    void accept(T value) throws IOException;
}
