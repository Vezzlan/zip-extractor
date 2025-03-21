package com.zip.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.client.FakeFileClient;
import com.zip.model.KafkaCommand;
import com.zip.services.zip.ZipContentProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KafkaCommandServiceTest {

    private KafkaCommandService kafkaCommandService;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        ZipContentProcessor zipContentProcessor = new ZipContentProcessor();
        FakeFileClient fileClient = new FakeFileClient();
        kafkaCommandService = new KafkaCommandService(objectMapper, zipContentProcessor, fileClient);
    }

    @Test
    void whenValidZip_thenCommandsShouldBeUpdatedAndSent() {
        var results = kafkaCommandService.createKafkaCommands(getZip("valid.zip"));

        final var firstKafkaCommand = resultWithId(results, "1");
        final var secondKafkaCommand = resultWithId(results, "2");
        final var thirdKafkaCommand = resultWithId(results, "3");

        assertAll(
                () -> assertEquals(3, results.size()),
                () -> assertEquals("calle", firstKafkaCommand.user().name()),
                () -> assertEquals("first file!", firstKafkaCommand.user().description()),
                () -> assertEquals("arne", secondKafkaCommand.user().name()),
                () -> assertEquals("second file!", secondKafkaCommand.user().description()),
                () -> assertEquals("william", thirdKafkaCommand.user().name()),
                () -> assertEquals("third file!", thirdKafkaCommand.user().description())
        );
    }

    @Test
    void whenJsonFileIsMissingInZip_shouldReturnEmptyListOfKafkaCommands() {
        var results = kafkaCommandService.createKafkaCommands(getZip("invalid.zip"));
        assertEquals(0, results.size());
    }

    private KafkaCommand resultWithId(List<KafkaCommand> results, String id) {
        return results.stream()
                .filter(command -> id.equals(command.user().id()))
                .findAny()
                .orElseThrow();
    }

    private File getZip(String zipFile) {
        try {
            return new ClassPathResource(zipFile).getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}