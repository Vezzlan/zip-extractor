package com.zip.zipUtils;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

@FunctionalInterface
public interface ZipContentWriter {
    void writeEntries(ZipOutputStream zipOutputStream) throws IOException;
}
