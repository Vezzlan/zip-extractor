package com.zip.services;

import com.zip.client.FakeFileClient;
import com.zip.services.zip.ZipExtractor;
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
        ZipExtractor zipExtractor = new ZipExtractor();
        FakeFileClient fakeFileClient = new FakeFileClient();
        KafkaPublisher kafkaPublisher = mock(KafkaPublisher.class);
        executeService = new ExecuteService(kafkaPublisher, fakeFileClient, zipExtractor);
    }

    @Test
    void whenReadingZipEntries_convertEntriesToUuidsAndOneError() {
        final var result = executeService.convertEntriesToIds(getZip());

        var hasError = result.stream().anyMatch(string -> string.contains("Error"));

        assertTrue(hasError);
        assertEquals(6, result.size());
    }

    private File getZip() {
        try {
            return new ClassPathResource("invalid.zip").getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}