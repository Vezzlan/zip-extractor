package com.zip.services.zip;

import com.zip.zipUtils.read.file.ZipReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ZipContentMapperTest {

    private ZipContentMapper zipContentMapper;

    @BeforeEach
    void setup() {
        zipContentMapper = new ZipContentMapper();
    }

    @Test
    public void zipInputStream_whenZipContainsPairOfJsonAndPythonFiles_AllEntriesShouldBeMapped() throws IOException {
        final var file = getZip("valid.zip");

        try (InputStream inputStream = Files.newInputStream(file.toPath());
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            final var result = zipContentMapper.mapZipEntriesToFilePairs2(zipInputStream);
            assertAll(
                    () -> assertNotNull(result.get("file1").json()),
                    () -> assertNotNull(result.get("file1").python()),
                    () -> assertNotNull(result.get("file2").json()),
                    () -> assertNotNull(result.get("file2").python()),
                    () -> assertNotNull(result.get("file3").json()),
                    () -> assertNotNull(result.get("file3").python())
            );
        }
    }

    @Test
    public void zipInputStream_whenJsonFileIsMissing_mapShouldHaveEmptyJsonEntry() throws IOException {
        final var file = getZip("missing_json.zip");

        try (InputStream inputStream = Files.newInputStream(file.toPath());
             ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            final var result = zipContentMapper.mapZipEntriesToFilePairs2(zipInputStream);
            assertAll(
                    () -> assertNotNull(result.get("file1").json()),
                    () -> assertNotNull(result.get("file1").python()),
                    () -> assertNull(result.get("file2").json()),
                    () -> assertNotNull(result.get("file2").python()),
                    () -> assertNotNull(result.get("file3").json()),
                    () -> assertNotNull(result.get("file3").python())
            );
        }
    }

    @Test
    public void whenZipContainsPairOfJsonAndPythonFiles_AllEntriesShouldBeMapped() {
        final var file = getZip("valid.zip");
        final var result = ZipReader.openAndApply(file, zipFile -> zipContentMapper.mapZipEntriesToFilePairs(zipFile));

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
        final var file = getZip("missing_json.zip");
        final var result = ZipReader.openAndApply(file, zipFile -> zipContentMapper.mapZipEntriesToFilePairs(zipFile));

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