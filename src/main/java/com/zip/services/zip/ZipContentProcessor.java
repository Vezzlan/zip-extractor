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

    private static final String JSON_FILE = ".json";

    private static final String PYTHON = "python";

    private static final String PYTHON_FILE = ".py";

    public Map<String, ZipEntryPair> mapZipEntriesToFilePairs(ZipFile zipFile) {
        return zipFile.stream()
                .filter(isMacOsResource().negate())
                .filter(isFile(JSON_FILE).or(isFile(PYTHON_FILE)))
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

    private Predicate<ZipEntry> isFile(String fileExtension) {
        return (zipEntry) -> zipEntry.getName().endsWith(fileExtension);
    }

    private String getFileName(ZipEntry zipEntry) {
        return zipEntry.getName().substring(0, zipEntry.getName().indexOf("."));
    }

    private String getFileType(ZipEntry entry) {
        return entry.getName().endsWith(JSON_FILE) ? JSON : PYTHON;
    }

}
