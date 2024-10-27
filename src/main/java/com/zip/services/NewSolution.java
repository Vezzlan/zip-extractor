package com.zip.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.kafka.KafkaPublisher;
import com.zip.model.CreatedResourceIds;
import com.zip.model.User;
import com.zip.model.KafkaCommand;
import com.zip.model.ZipEntryHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.zip.zipUtils.ZipFileHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.stream.Collectors.*;

@Component
public class NewSolution {

    private final ObjectMapper objectMapper;

    private static final String JSON = "json";

    private static final String PYTHON = "python";

    @Autowired
    public NewSolution(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CreatedResourceIds> importFilesFromZip(File file) {
        final var zipEntryMap = ZipFileHandler.withZipFile(file, this::mapZipEntries);

        if (isJsonMissing(zipEntryMap)) {
            return Collections.emptyList();
        }

        final var kafkaCommands = ZipFileHandler.withZipFile(file, zipFile ->
                convertToKafkaCommands(zipFile, zipEntryMap));

        sendToKafka(kafkaCommands);

        return convertToCreatedResources(kafkaCommands);
    }

    private Map<String, ZipEntryHolder> mapZipEntries(ZipFile zipFile) {
        return zipFile.stream()
                .filter(entry -> !entry.getName().startsWith("__MACOSX"))
                .filter(this::isJsonOrPython)
                .collect(groupingBy(
                        this::getFileName,
                        collectingAndThen(
                                toMap(this::getFileExtension, Function.identity()),
                                mapOfZipEntries -> new ZipEntryHolder(mapOfZipEntries.get(JSON), mapOfZipEntries.get(PYTHON))

                        ))
                );
    }

    private boolean isJsonOrPython(ZipEntry zipEntry) {
        return zipEntry.getName().endsWith(".json") || zipEntry.getName().endsWith(".py");
    }

    private String getFileName(ZipEntry zipEntry) {
        return  zipEntry.getName().substring(0, zipEntry.getName().indexOf("."));
    }

    private String getFileExtension(ZipEntry entry) {
        return entry.getName().endsWith(".json") ? JSON : PYTHON;
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
        ZipEntry jsonEntry = zipEntryHolder.json();
        ZipEntry pyEntry = zipEntryHolder.python();

        User metadata = parseJsonToUser(zipFile, jsonEntry);
        final var newFileId = fileClientMock(pyEntry);
        final var newId = UUID.randomUUID().toString();

        return new KafkaCommand(metadata, newFileId, newId);
    }

    private User parseJsonToUser(ZipFile zipFile, ZipEntry jsonEntry) {
        try (InputStream inputStream = zipFile.getInputStream(jsonEntry)) {
            final var jsonStr = new String(inputStream.readAllBytes());
            final var metadata = parseJson(jsonStr);
            return new User(
                    metadata.id(),
                    UUID.randomUUID().toString(),
                    "kalle",
                    metadata.description()); //Copy with new values
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
                .forEach(KafkaPublisher::sendCommand);
    }

    private List<CreatedResourceIds> convertToCreatedResources(final List<KafkaCommand> kafkaCommands) {
        return kafkaCommands.stream()
                .map(command -> new CreatedResourceIds(command.id(), command.fileId()))
                .toList();
    }

}
