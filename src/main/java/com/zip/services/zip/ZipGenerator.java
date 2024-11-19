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

    public void generate(OutputStream zipOutputStream, User user) {
        final var pythonResource = fileClient.downloadPythonFile(user.fileId());
        final var pythonFileName = String.format("%s.py", user.name());
        final var jsonFileName = String.format("%s.json", user.name());

        ZipFileHandler.writeToZipOutputStream(zipOutputStream, zipWriter -> {
            addZipEntry(zipWriter, pythonFileName, pythonResource);
            addZipEntry(zipWriter, jsonFileName, user);
        });
    }

    private void addZipEntry(ZipOutputStream zipWriter, String fileName, Resource pythonResource) throws IOException {
        zipWriter.putNextEntry(new ZipEntry(fileName));
        try (InputStream resource = pythonResource.getInputStream()) {
            resource.transferTo(zipWriter);
        }
        zipWriter.closeEntry();
    }

    private void addZipEntry(ZipOutputStream zipWriter, String fileName, User user) throws IOException {
        zipWriter.putNextEntry(new ZipEntry(fileName));
        convertToJson(zipWriter, user);
        zipWriter.closeEntry();
    }

    private void addZipEntryAsBytes(ZipOutputStream zipWriter, String fileName, User user) throws IOException {
        zipWriter.putNextEntry(new ZipEntry(fileName));
        zipWriter.write(objectMapper.writeValueAsBytes(user));
        zipWriter.closeEntry();
    }

    private void convertToJson(ZipOutputStream zipWriter, User user) throws IOException {
        objectMapper.writeValue(zipWriter, user);
    }

}
