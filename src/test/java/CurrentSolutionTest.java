import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

public class CurrentSolutionTest {

    private NewSolution newSolution;

    private CurrentSolution currentSolution;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        newSolution = new NewSolution(objectMapper);
        currentSolution = new CurrentSolution(objectMapper);
    }

    @Test
    public void testImportFunction() {
        var result = currentSolution.importFunction(getZip("valid.zip"));

        System.out.println("endresult: " + result);
    }

    @Test
    public void testImportNewSolution() {
        var result = newSolution.importFilesFromZip(getZip("valid2.zip"));

        System.out.println("endresult new solution: " + result);
    }

    @Test
    public void testImportNewSolution_ShouldReturnEmptyList() {
        final var result = newSolution.importFilesFromZip(getZip("invalid.zip"));
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