package com.zip.services;

import com.zip.model.ZipEntryHolder;
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

    private static final String JSON = "json";

    private static final String PYTHON = "python";

    public Map<String, ZipEntryHolder> mapZipEntries(File file) {
        return ZipFileHandler.withZipFile(file, this::mapZipEntries);
    }

    public List<String> getFileNames(File file) {
        return ZipFileHandler.withZipFile(file, zipFile ->
                zipFile.stream()
                        .map(ZipEntry::getName)
                        .filter(name -> !name.startsWith("__MACOSX"))
                        .toList()
        );
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

}
