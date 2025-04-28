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
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Objects.isNull;

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
        return ZipFileHandler.useFile(file, this::convertToKafkaCommands);
    }

    private List<KafkaCommand> convertToKafkaCommands(ZipFile zipFile) {
        final var filePairsMap = zipContentProcessor.mapZipEntriesToFilePairs(zipFile);

        if (hasMissingFiles(filePairsMap)) {
            return Collections.emptyList();
        }
        return filePairsMap.values().stream()
                .map(zipEntryPair -> createKafkaCommand(zipFile, zipEntryPair))
                .toList();
    }

    private boolean hasMissingFiles(Map<String, ZipEntryPair> zipEntriesMap) {
        return zipEntriesMap.values().stream()
                .anyMatch(pair -> isNull(pair.json()) || isNull(pair.python()));
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
