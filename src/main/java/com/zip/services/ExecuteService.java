package com.zip.services;

import com.zip.kafka.KafkaPublisher;
import com.zip.model.CreatedResourceIds;
import com.zip.model.KafkaCommand;
import com.zip.model.ZipEntryHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ExecuteService {

    private final ZipService zipService;

    private final KafkaPublisher kafkaPublisher;

    @Autowired
    public ExecuteService(ZipService zipService, KafkaPublisher kafkaPublisher) {
        this.zipService = zipService;
        this.kafkaPublisher = kafkaPublisher;
    }

    public List<CreatedResourceIds> executeFlow(File file) {
        final var zipEntryMap = zipService.importFilesFromZip(file);

        if (isJsonMissing(zipEntryMap)) {
            return Collections.emptyList();
        }

        final var kafkaCommands = kafkaPublisher.sendCommand(file, zipEntryMap);
        return convertToCreatedResources(kafkaCommands);
    }

    private boolean isJsonMissing(Map<String, ZipEntryHolder> zipEntriesMap) {
        return zipEntriesMap.entrySet().stream()
                .anyMatch(entry -> entry.getValue().json() == null);
    }

    private List<CreatedResourceIds> convertToCreatedResources(final List<KafkaCommand> kafkaCommands) {
        return kafkaCommands.stream()
                .map(command -> new CreatedResourceIds(command.id(), command.fileId()))
                .toList();
    }

}
