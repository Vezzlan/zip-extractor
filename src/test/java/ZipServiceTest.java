import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import com.zip.services.ZipService;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ZipServiceTest {

    private ZipService zipService;

    @BeforeEach
    void setup() {
        zipService = new ZipService();
    }

    @Test
    public void whenZipContainsPairOfJsonAndPythonFiles_AllEntriesShouldBeMapped() {
        var result = zipService.importFilesFromZip(getZip("valid.zip"));
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
        final var result = zipService.importFilesFromZip(getZip("invalid.zip"));

        assertAll(
                () -> assertNotNull(result.get("file1").json()),
                () -> assertNotNull(result.get("file1").python()),
                () -> assertNull(result.get("file3").json()),
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