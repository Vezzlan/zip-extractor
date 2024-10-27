import com.fasterxml.jackson.databind.ObjectMapper;
import com.zip.kafka.KafkaPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.core.io.ClassPathResource;
import com.zip.services.ZipService;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ZipServiceTest {

    private ZipService zipService;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        KafkaPublisher kafkaPublisher = new KafkaPublisher(objectMapper);
        zipService = new ZipService(kafkaPublisher);
    }

    @Test
    public void testImportNewSolution() {
        var result = zipService.importFilesFromZip(getZip("valid.zip"));

        Assertions.assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertNotNull(result.getFirst().uuid()),
                () -> assertNotNull(result.getFirst().commonId())
        );
    }

    @Test
    public void whenJsonFileIsMissing_thenShouldReturnEmptyList() {
        final var result = zipService.importFilesFromZip(getZip("invalid.zip"));
        assert result.isEmpty();
    }

    private File getZip(String zipFile) {
        try {
            return new ClassPathResource(zipFile).getFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}