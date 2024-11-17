package com.zip.client;

import com.zip.exceptions.FileClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;

@Component
public class FakeFileClient {

    @Value("classpath:file1.py")
    private Resource pythonFile;

    public String getFileId(ZipEntry pythonEntry) {
        return UUID.randomUUID().toString();
    }

    public UUID getFileId(String fileName) throws FileClientException {
        if (fileName.equals("text.txt")) {
            throw new FileClientException("Invalid file sent: " + fileName);
        }
        return UUID.randomUUID();
    }

    public Resource downloadPythonFile(String fileId) {
        try {
            return new InputStreamResource(pythonFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
