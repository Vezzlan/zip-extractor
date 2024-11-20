package com.zip.zipUtils;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@FunctionalInterface
public interface ZipFileWriter {
    void write(ZipOutputStream zipOutputStream) throws IOException;
}
