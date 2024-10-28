package com.zip.services;

import com.zip.client.FakeFileClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ExecuteServiceTest {

    private ExecuteService executeService;

    @BeforeEach
    void setup() {
        ZipService zipService = new ZipService();
        FakeFileClient fakeFileClient = new FakeFileClient();
        KafkaPublisher kafkaPublisher = mock(KafkaPublisher.class);
        executeService = new ExecuteService(kafkaPublisher, fakeFileClient, zipService);
    }

    @Test
    void whenReadingZipEntries_convertEntriesToUuidsAndOneError() {
        final var result = executeService.convertEntriesToIds(getZip("invalid.zip"));

        var hasError = result.stream().anyMatch(string -> string.contains("Error"));

        assertTrue(hasError);
        assertEquals(6, result.size());
    }

    private File getZip(String zipFile) {
        try {
            return new ClassPathResource(zipFile).getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}