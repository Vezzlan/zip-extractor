package com.zip.services;

import com.zip.client.FakeFileClient;
import com.zip.exceptions.Failure;
import com.zip.exceptions.Success;
import com.zip.exceptions.Try;
import com.zip.model.KafkaCommand;
import com.zip.model.ResourceIds;
import com.zip.model.User;
import com.zip.services.zip.ZipContentMapper;
import com.zip.zipUtils.ZipReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;

@Component
public class ExecuteService {

    private final KafkaCommandService kafkaCommandService;

    private final FakeFileClient fileClient;

    private final ZipContentMapper zipContentMapper;

    @Autowired
    public ExecuteService(KafkaCommandService kafkaCommandService, FakeFileClient fileClient, ZipContentMapper zipContentMapper) {
        this.kafkaCommandService = kafkaCommandService;
        this.fileClient = fileClient;
        this.zipContentMapper = zipContentMapper;
    }

    public List<ResourceIds> executeFlow(File file) {
        final var kafkaCommands = kafkaCommandService.createKafkaCommands(file);

        sendCommandsToKafka(kafkaCommands);

        return kafkaCommands.stream()
                .map(command -> new ResourceIds(command.id(), command.fileId()))
                .toList();
    }

    private void sendCommandsToKafka(List<KafkaCommand> kafkaCommands) {
        kafkaCommands.stream()
                .map(dto -> new User(dto.id(), dto.fileId(), dto.user().name(), dto.user().description()))
                .forEach(this::sendToKafka);
    }

    private void sendToKafka(User sendCommand) {
        System.out.println(sendCommand);
    }

    public List<String> convertEntriesToIds(File file) {
        final var entries = ZipReader.openAndApply(file, zipContentMapper::listZipEntries);
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
                .map(tryOf -> tryOf.map(UUID::toString))
                .toList();
    }

}
