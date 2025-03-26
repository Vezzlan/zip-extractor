package com.zip.services.zip;

import com.zip.zipUtils.ZipFileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ZipContentProcessorTest {

    private ZipContentProcessor zipContentProcessor;

    @BeforeEach
    void setup() {
        zipContentProcessor = new ZipContentProcessor();
    }

    @Test
    public void whenZipContainsPairOfJsonAndPythonFiles_AllEntriesShouldBeMapped() {
        final var file = getZip("valid.zip");
        final var result = ZipFileHandler.openZipFile(file, zipFile -> zipContentProcessor.mapZipEntries(zipFile));

        assertAll(
                () -> assertNotNull(result.get("file1").json()),
                () -> assertNotNull(result.get("file1").python()),
                () -> assertNotNull(result.get("file2").json()),
                () -> assertNotNull(result.get("file2").python()),
                () -> assertNotNull(result.get("file3").json()),
                () -> assertNotNull(result.get("file3").python())
        );
    }

    @Test
    public void whenJsonFileIsMissing_mapShouldHaveEmptyJsonEntry() {
        final var file = getZip("invalid.zip");
        final var result = ZipFileHandler.openZipFile(file, zipFile -> zipContentProcessor.mapZipEntries(zipFile));

        assertAll(
                () -> assertNotNull(result.get("file1").json()),
                () -> assertNotNull(result.get("file1").python()),
                () -> assertNull(result.get("file2").json()),
                () -> assertNotNull(result.get("file2").python()),
                () -> assertNotNull(result.get("file3").json()),
                () -> assertNotNull(result.get("file3").python())
        );
    }

    private File getZip(String zipFile) {
        try {
            return new ClassPathResource(zipFile).getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}