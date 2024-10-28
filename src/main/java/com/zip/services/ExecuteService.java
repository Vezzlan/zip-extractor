package com.zip.services;

import com.zip.client.FakeFileClient;
import com.zip.exceptions.Failure;
import com.zip.exceptions.Success;
import com.zip.exceptions.Try;
import com.zip.kafka.KafkaPublisher;
import com.zip.model.CreatedResourceIds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Component
public class ExecuteService {

    private final KafkaPublisher kafkaPublisher;

    private final FakeFileClient fileClient;

    private final ZipService zipService;

    @Autowired
    public ExecuteService(KafkaPublisher kafkaPublisher, FakeFileClient fileClient, ZipService zipService) {
        this.kafkaPublisher = kafkaPublisher;
        this.fileClient = fileClient;
        this.zipService = zipService;
    }

    public List<CreatedResourceIds> executeFlow(File file) {
        final var kafkaCommands = kafkaPublisher.sendCommand(file);
        return kafkaCommands.stream()
                .map(command -> new CreatedResourceIds(command.id(), command.fileId()))
                .toList();
    }

    public List<String> convertEntriesToIds(File file) {
        final var entries = zipService.getZipEntries(file);
        return getIdFromFileClient(entries).stream()
                .map(name -> switch (name) {
                    case Success(String result) -> result;
                    case Failure(Throwable throwable) -> "Error: " + throwable.getMessage();
                })
                .toList();
    }

    private List<Try<String>> getIdFromFileClient(List<String> fileNames) {
        return fileNames.stream()
                .map(code -> Try.of(() -> fileClient.getFileId(code)))
                .map(uuid -> uuid.map(UUID::toString))
                .toList();
    }

}
