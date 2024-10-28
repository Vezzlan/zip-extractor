package com.zip.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.client.FakeFileClient;
import com.zip.model.KafkaCommand;
import com.zip.services.ZipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KafkaPublisherTest {

    private KafkaPublisher kafkaPublisher;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        ZipService zipService = new ZipService();
        FakeFileClient fileClient = new FakeFileClient();
        kafkaPublisher = new KafkaPublisher(objectMapper, zipService, fileClient);
    }

    @Test
    void whenValidZip_thenCommandsShouldBeUpdatedAndSent() {
        var results = kafkaPublisher.sendCommand(getZip("valid.zip"));

        final var first = resultWithId(results, "1");
        final var second = resultWithId(results, "2");

        assertAll(
                () -> assertEquals(3, results.size()),
                () -> assertEquals("calle", first.user().name()),
                () -> assertEquals("first file!", first.user().description()),
                () -> assertEquals("arne", second.user().name()),
                () -> assertEquals("second file!", second.user().description())
        );
    }

    @Test
    void whenJsonFileIsMissingInZip_shouldReturnEmptyListOfKafkaCommands() {
        var results = kafkaPublisher.sendCommand(getZip("invalid.zip"));
        assertEquals(0, results.size());
    }

    private KafkaCommand resultWithId(List<KafkaCommand> results, String id) {
        return results.stream().filter(command -> id.equals(command.user().id())).findAny().orElseThrow();
    }

    private File getZip(String zipFile) {
        try {
            return new ClassPathResource(zipFile).getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}