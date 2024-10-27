package com.zip.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.model.KafkaCommand;
import com.zip.model.User;
import com.zip.model.ZipEntryHolder;
import com.zip.services.ZipService;
import com.zip.zipUtils.ZipFileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class KafkaPublisher {

    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<KafkaCommand> sendCommand(File file, Map<String, ZipEntryHolder> zipEntryMap) {
        final var kafkaCommands = ZipFileHandler.withZipFile(file, zipFile ->
                convertToKafkaCommands(zipFile, zipEntryMap));

        sendToKafka(kafkaCommands);

        return kafkaCommands;
    }

    private List<KafkaCommand> convertToKafkaCommands(ZipFile zipFile, Map<String, ZipEntryHolder> zipEntryMap) {
        return zipEntryMap.values().stream()
                .map(zipEntryHolder -> toKafkaCommand(zipFile, zipEntryHolder))
                .toList();
    }

    private KafkaCommand toKafkaCommand(ZipFile zipFile, ZipEntryHolder zipEntryHolder) {
        ZipEntry jsonEntry = zipEntryHolder.json();
        ZipEntry pythonEntry = zipEntryHolder.python();

        final var user = parseJsonToUser(zipFile, jsonEntry);
        final var newFileIdFromWWW = fileClientMock(pythonEntry);
        final var newId = UUID.randomUUID().toString();

        return new KafkaCommand(user, newFileIdFromWWW, newId);
    }

    private User parseJsonToUser(ZipFile zipFile, ZipEntry jsonEntry) {
        try (InputStream inputStream = zipFile.getInputStream(jsonEntry)) {
            final var jsonStr = new String(inputStream.readAllBytes());
            final var metadata = parseJson(jsonStr);
            return new User(metadata.id(), UUID.randomUUID().toString(), "kalle", metadata.description());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String fileClientMock(ZipEntry pyCode) {
        //Mock client with PyCode.
        return UUID.randomUUID().toString();
    }

    private User parseJson(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToKafka(List<KafkaCommand> kafkaCommands) {
        kafkaCommands.stream()
                .map(dto -> new User(dto.id(), dto.fileId(), dto.user().name(), dto.user().description()))
                .forEach(KafkaPublisher::sendToKafka);
    }

    private static void sendToKafka(User sendCommand) {
        //Send to Kafka
    }

}
