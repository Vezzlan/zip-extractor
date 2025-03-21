package com.zip.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.client.FakeFileClient;
import com.zip.model.KafkaCommand;
import com.zip.model.User;
import com.zip.model.ZipEntryPair;
import com.zip.services.zip.ZipContentProcessor;
import com.zip.zipUtils.ZipFileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class KafkaCommandService {

    private final ObjectMapper objectMapper;

    private final ZipContentProcessor zipContentProcessor;

    private final FakeFileClient fileClient;

    @Autowired
    public KafkaCommandService(ObjectMapper objectMapper, ZipContentProcessor zipContentProcessor, FakeFileClient fileClient) {
        this.objectMapper = objectMapper;
        this.zipContentProcessor = zipContentProcessor;
        this.fileClient = fileClient;
    }

    public List<KafkaCommand> createKafkaCommands(File file) {
        final var zipEntryMap = zipContentProcessor.mapZipFileEntries(file);

        if (isJsonMissing(zipEntryMap)) {
            return Collections.emptyList();
        }

        return ZipFileHandler.openZipFile(file, zipFile -> convertToKafkaCommands(zipFile, zipEntryMap));
    }

    private boolean isJsonMissing(Map<String, ZipEntryPair> zipEntriesMap) {
        return zipEntriesMap.entrySet().stream()
                .anyMatch(entry -> entry.getValue().json() == null);
    }

    private List<KafkaCommand> convertToKafkaCommands(ZipFile zipFile, Map<String, ZipEntryPair> zipEntryMap) {
        return zipEntryMap.values().stream()
                .map(zipEntryPair -> createKafkaCommand(zipFile, zipEntryPair))
                .toList();
    }

    private KafkaCommand createKafkaCommand(ZipFile zipFile, ZipEntryPair zipEntryPair) {
        final var oldUser = parseJsonToUser(zipFile, zipEntryPair.json());
        final var user = new User(oldUser.id(), UUID.randomUUID().toString(), oldUser.name(), oldUser.description());
        final var fileId = fileClient.getFileId(zipEntryPair.python());
        final var newId = UUID.randomUUID().toString();
        return new KafkaCommand(newId, fileId, user);
    }

    private User parseJsonToUser(ZipFile zipFile, ZipEntry jsonEntry) {
        try (InputStream inputStream = zipFile.getInputStream(jsonEntry)) {
            return parseJson(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private User parseJson(InputStream inputStream) throws IOException {
        try {
            return objectMapper.readValue(inputStream, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
