package com.zip.services;

import com.zip.kafka.KafkaPublisher;
import com.zip.model.CreatedResourceIds;
import com.zip.model.KafkaCommand;
import com.zip.model.ZipEntryHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.zip.zipUtils.ZipFileHandler;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.stream.Collectors.*;

@Component
public class ZipService {

    private final KafkaPublisher kafkaPublisher;

    private static final String JSON = "json";

    private static final String PYTHON = "python";

    @Autowired
    public ZipService(KafkaPublisher kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
    }

    public List<CreatedResourceIds> importFilesFromZip(File file) {
        final var zipEntryMap = ZipFileHandler.withZipFile(file, this::mapZipEntries);

        if (isJsonMissing(zipEntryMap)) {
            return Collections.emptyList();
        }

        final var kafkaCommands = kafkaPublisher.sendCommand(file, zipEntryMap);
        return convertToCreatedResources(kafkaCommands);
    }

    private Map<String, ZipEntryHolder> mapZipEntries(ZipFile zipFile) {
        return zipFile.stream()
                .filter(entry -> !entry.getName().startsWith("__MACOSX"))
                .filter(this::isJsonOrPython)
                .collect(groupingBy(
                        this::getFileName,
                        collectingAndThen(
                                toMap(
                                        this::getFileType,
                                        Function.identity()
                                ),
                                map -> new ZipEntryHolder(map.get(JSON), map.get(PYTHON))
                        ))
                );
    }

    private boolean isJsonOrPython(ZipEntry zipEntry) {
        return zipEntry.getName().endsWith(".json") || zipEntry.getName().endsWith(".py");
    }

    private String getFileName(ZipEntry zipEntry) {
        return  zipEntry.getName().substring(0, zipEntry.getName().indexOf("."));
    }

    private String getFileType(ZipEntry entry) {
        return entry.getName().endsWith(".json") ? JSON : PYTHON;
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
