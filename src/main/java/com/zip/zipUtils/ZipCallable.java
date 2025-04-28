package com.zip.zipUtils;

import java.io.IOException;

@FunctionalInterface
public interface ZipCallable<T> {
    void call(T t) throws IOException;
}
