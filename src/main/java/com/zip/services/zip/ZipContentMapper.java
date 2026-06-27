package com.zip.services.zip;

import com.zip.model.ZipEntryPair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static java.util.stream.Collectors.*;

@Component
public class ZipContentMapper {

    private static final String JSON = "json";

    private static final String PYTHON = "py";

    private static final String MAC_OS_RESOURCE = "__MACOSX";

    private static final class ZipEntryPairBuilder {
        ZipEntry json;
        ZipEntry python;
    }
    /**
     *
     * Using ZipInputStream and read directly from stream instead of saving file to disc.
     *
     */
    public Map<String, ZipEntryPair> mapZipEntriesToFilePairs2(ZipInputStream zipInputStream) throws IOException {
        Map<String, ZipEntryPairBuilder> grouped = new HashMap<>();

        forEachEntry(zipInputStream, zipEntry -> {
            if (!isMacOsResource(zipEntry) && isJsonOrPython(zipEntry)) {
                String fileName = getFileName(zipEntry);

                ZipEntryPairBuilder builder =
                        grouped.computeIfAbsent(fileName, k -> new ZipEntryPairBuilder());

                switch (getFileType(zipEntry)) {
                    case JSON -> builder.json = new ZipEntry(zipEntry.getName());
                    case PYTHON -> builder.python = new ZipEntry(zipEntry.getName());
                }
            }
        });

        return grouped.entrySet().stream()
                .collect(toMap(
                        Map.Entry::getKey,
                        e -> new ZipEntryPair(e.getValue().json, e.getValue().python)
                ));
    }

    private static void forEachEntry(
            ZipInputStream zipInputStream,
            ThrowingConsumer<ZipEntry> consumer) throws IOException {

        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            try {
                consumer.accept(entry);
            } finally {
                zipInputStream.closeEntry();
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T value) throws IOException;
    }

    /**
     *
     * Read from file on disc - pro side is that you can use stream() function to modify your result
     *
     */
    public Map<String, ZipEntryPair> mapZipEntriesToFilePairs(ZipFile zipFile) {
        return zipFile.stream()
                .filter(zipEntry -> !isMacOsResource(zipEntry))
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
                .filter(entry -> !isMacOsResource(entry))
                .map(ZipEntry::getName)
                .toList();
    }

    private boolean isMacOsResource(ZipEntry zipEntry) {
        return zipEntry.getName().startsWith(MAC_OS_RESOURCE);
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
