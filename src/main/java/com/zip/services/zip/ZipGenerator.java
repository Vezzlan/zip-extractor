package com.zip.services.zip;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.client.FakeFileClient;
import com.zip.model.User;
import com.zip.zipUtils.ZipFileHandler;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipGenerator {

    private final ObjectMapper objectMapper;

    private final FakeFileClient fileClient;

    public ZipGenerator(FakeFileClient fileClient) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        this.fileClient = fileClient;
    }

    public void generate(OutputStream outputStream, User user) {
        final var pythonResource = fileClient.downloadPythonFile(user.fileId());
        final var pythonFileName = String.format("%s.py", user.name());
        final var jsonFileName = String.format("%s.json", user.name());

        ZipFileHandler.writeToOutputStream(outputStream, zipOutputStream -> {
            addZipEntry(zipOutputStream, pythonFileName, pythonResource);
            addZipEntry(zipOutputStream, jsonFileName, user);
        });
    }

    private void addZipEntry(ZipOutputStream zipOutputStream, String fileName, Resource pythonResource) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        try (InputStream resource = pythonResource.getInputStream()) {
            resource.transferTo(zipOutputStream);
        }
        zipOutputStream.closeEntry();
    }

    private void addZipEntry(ZipOutputStream zipOutputStream, String fileName, User user) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        convertToJson(zipOutputStream, user);
        zipOutputStream.closeEntry();
    }

    private void addZipEntryAsBytes(ZipOutputStream zipOutputStream, String fileName, User user) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(fileName));
        zipOutputStream.write(objectMapper.writeValueAsBytes(user));
        zipOutputStream.closeEntry();
    }

    private void convertToJson(ZipOutputStream zipOutputStream, User user) throws IOException {
        objectMapper.writeValue(zipOutputStream, user);
    }

}
