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

    private final KafkaPublisher kafkaPublisher;

    @Autowired
    public ExecuteService(KafkaPublisher kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
    }

    public List<CreatedResourceIds> executeFlow(File file) {
        final var kafkaCommands = kafkaPublisher.sendCommand(file);
        return kafkaCommands.stream()
                .map(command -> new CreatedResourceIds(command.id(), command.fileId()))
                .toList();
    }

}
