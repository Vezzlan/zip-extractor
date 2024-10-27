package com.zip.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.model.ZipEntryHolder;
import com.zip.services.ZipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.*;

class KafkaPublisherTest {

    private KafkaPublisher kafkaPublisher;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        ZipService zipService = new ZipService();
        kafkaPublisher = new KafkaPublisher(objectMapper, zipService);
    }

    @Test
    void when_thenSendCommand() {
        Map<String, ZipEntryHolder> expected = new HashMap<>();
        expected.put("file1", new ZipEntryHolder(new ZipEntry("file1.json"), new ZipEntry("file1.py")));
        expected.put("file2", new ZipEntryHolder(new ZipEntry("file2.json"), new ZipEntry("file2.py")));
        expected.put("file3", new ZipEntryHolder(new ZipEntry("file3.json"), new ZipEntry("file3.py")));

        //Mocka det ovan eller? För att kunna testa rätt saker?
        var result = kafkaPublisher.sendCommand(getZip("valid.zip"));

        final var first = result.stream().filter(command -> "1".equals(command.user().id())).findAny().orElseThrow();
        final var second = result.stream().filter(command -> "2".equals(command.user().id())).findAny().orElseThrow();

        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("first file!", first.user().description()),
                () -> assertEquals("second file!", second.user().description())
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