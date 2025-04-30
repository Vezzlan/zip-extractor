package com.zip.zipUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.model.User;
import com.zip.zipUtils.functional_interfaces.UseInstance;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipWriter {

    private final ZipOutputStream zipOutputStream;

    private final ObjectMapper objectMapper;

    private ZipWriter(final OutputStream outputStream) {
        zipOutputStream = new ZipOutputStream(outputStream);
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    }

    public static void use(OutputStream outputStream, UseInstance<ZipWriter, IOException> zipCallable) throws IOException {
        ZipWriter zipWriter = new ZipWriter(outputStream);
        try {
            zipCallable.accept(zipWriter);
        } finally {
            zipWriter.close();
        }
    }

    public void addEntry(String fileName, Resource pythonResource) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        try (InputStream resource = pythonResource.getInputStream()) {
            resource.transferTo(zipOutputStream);
        }
        zipOutputStream.closeEntry();
    }

    public void addEntry(String fileName, User user) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        objectMapper.writeValue(zipOutputStream, user);
        zipOutputStream.closeEntry();
    }

    private void close() throws IOException {
        zipOutputStream.close();
    }
}
