package com.zip.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.model.ZipEntryHolder;
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
        kafkaPublisher = new KafkaPublisher(objectMapper);
    }

    @Test
    void when_thenSendCommand() {
        Map<String, ZipEntryHolder> expected = new HashMap<>();
        expected.put("file1", new ZipEntryHolder(new ZipEntry("file1.json"), new ZipEntry("file1.py")));
        expected.put("file2", new ZipEntryHolder(new ZipEntry("file2.json"), new ZipEntry("file2.py")));
        expected.put("file3", new ZipEntryHolder(new ZipEntry("file3.json"), new ZipEntry("file3.py")));


        var result = kafkaPublisher.sendCommand(getZip("valid.zip"), expected);

        assertAll(
                () -> assertEquals(3, result.size())
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