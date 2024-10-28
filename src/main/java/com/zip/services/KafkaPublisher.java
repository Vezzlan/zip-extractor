package com.zip.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.client.FakeFileClient;
import com.zip.model.KafkaCommand;
import com.zip.model.User;
import com.zip.model.ZipEntryHolder;
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
public class KafkaPublisher {

    private final ObjectMapper objectMapper;

    private final ZipService zipService;

    private final FakeFileClient fileClient;

    @Autowired
    public KafkaPublisher(ObjectMapper objectMapper, ZipService zipService, FakeFileClient fileClient) {
        this.objectMapper = objectMapper;
        this.zipService = zipService;
        this.fileClient = fileClient;
    }

    public List<KafkaCommand> sendCommand(File file) {
        final var zipEntryMap = zipService.mapZipEntries(file);

        if (isJsonMissing(zipEntryMap)) {
            return Collections.emptyList();
        }

        final var kafkaCommands = ZipFileHandler.withZipFile(file, zipFile ->
                convertToKafkaCommands(zipFile, zipEntryMap));

        sendCommandsToKafka(kafkaCommands);

        return kafkaCommands;
    }

    private boolean isJsonMissing(Map<String, ZipEntryHolder> zipEntriesMap) {
        return zipEntriesMap.entrySet().stream()
                .anyMatch(entry -> entry.getValue().json() == null);
    }

    private List<KafkaCommand> convertToKafkaCommands(ZipFile zipFile, Map<String, ZipEntryHolder> zipEntryMap) {
        return zipEntryMap.values().stream()
                .map(zipEntryHolder -> toKafkaCommand(zipFile, zipEntryHolder))
                .toList();
    }

    private KafkaCommand toKafkaCommand(ZipFile zipFile, ZipEntryHolder zipEntryHolder) {
        final var user = parseJsonToUser(zipFile, zipEntryHolder.json());
        final var fileId = fileClient.getFileId(zipEntryHolder.python());
        final var newId = UUID.randomUUID().toString();
        return new KafkaCommand(newId, fileId, user);
    }

    private User parseJsonToUser(ZipFile zipFile, ZipEntry jsonEntry) {
        try (InputStream inputStream = zipFile.getInputStream(jsonEntry)) {
            final var jsonStr = new String(inputStream.readAllBytes());
            final var metadata = parseJson(jsonStr);
            return new User(metadata.id(), UUID.randomUUID().toString(), metadata.name(), metadata.description());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private User parseJson(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendCommandsToKafka(List<KafkaCommand> kafkaCommands) {
        kafkaCommands.stream()
                .map(dto -> new User(dto.id(), dto.fileId(), dto.user().name(), dto.user().description()))
                .forEach(KafkaPublisher::sendToKafka);
    }

    private static void sendToKafka(User sendCommand) {
        //Send to Kafka
    }

}