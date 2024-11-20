package com.zip.services;

import com.zip.client.FakeFileClient;
import com.zip.exceptions.Failure;
import com.zip.exceptions.Success;
import com.zip.exceptions.Try;
import com.zip.model.ResourceIds;
import com.zip.services.zip.ZipExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Component
public class ExecuteService {

    private final KafkaPublisher kafkaPublisher;

    private final FakeFileClient fileClient;

    private final ZipExtractor zipExtractor;

    @Autowired
    public ExecuteService(KafkaPublisher kafkaPublisher, FakeFileClient fileClient, ZipExtractor zipExtractor) {
        this.kafkaPublisher = kafkaPublisher;
        this.fileClient = fileClient;
        this.zipExtractor = zipExtractor;
    }

    public List<ResourceIds> executeFlow(File file) {
        final var kafkaCommands = kafkaPublisher.sendCommand(file);
        return kafkaCommands.stream()
                .map(command -> new ResourceIds(command.id(), command.fileId()))
                .toList();
    }

    public List<String> convertEntriesToIds(File file) {
        final var entries = zipExtractor.listEntriesFromZip(file);
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
