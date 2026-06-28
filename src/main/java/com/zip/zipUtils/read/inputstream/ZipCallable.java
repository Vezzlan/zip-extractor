package com.zip.zipUtils.read.inputstream;

import java.io.IOException;

@FunctionalInterface
public interface ZipCallable<T> {
    void accept(T value) throws IOException;
}
