package com.zip.services.zip;

import com.zip.model.ZipEntryPair;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.stream.Collectors.*;

@Component
public class ZipContentProcessor {

    private static final String JSON = "json";

    private static final String PYTHON = "python";

    public Map<String, ZipEntryPair> mapZipEntriesToFilePairs(ZipFile zipFile) {
        return zipFile.stream()
                .filter(isMacOsResource().negate())
                .filter(this::isJsonOrPython)
                .collect(groupingBy(
                        this::getFileName,
                        collectingAndThen(
                                toMap(
                                        this::getFileType,
                                        Function.identity()
                                ),
                                map -> new ZipEntryPair(map.get(JSON), map.get(PYTHON))
                        ))
                );
    }

    public List<String> listZipEntries(ZipFile zipFile) {
        return zipFile.stream()
                .filter(isMacOsResource().negate())
                .map(ZipEntry::getName)
                .toList();
    }

    private Predicate<ZipEntry> isMacOsResource() {
        return (zipEntry) -> zipEntry.getName().startsWith("__MACOSX");
    }

    private boolean isJsonOrPython(ZipEntry zipEntry) {
        return zipEntry.getName().endsWith(".json") || zipEntry.getName().endsWith(".py");
    }

    private String getFileName(ZipEntry zipEntry) {
        return zipEntry.getName().substring(0, zipEntry.getName().indexOf("."));
    }

    private String getFileType(ZipEntry entry) {
        return entry.getName().endsWith(".json") ? JSON : PYTHON;
    }

}
