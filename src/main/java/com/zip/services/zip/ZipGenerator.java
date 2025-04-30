package com.zip.services.zip;

import com.zip.client.FakeFileClient;
import com.zip.model.User;
import com.zip.zipUtils.ZipWriter;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ZipGenerator {

    private final FakeFileClient fileClient;

    public ZipGenerator(FakeFileClient fileClient) {
        this.fileClient = fileClient;
    }

    public void generate(OutputStream outputStream, User user) throws IOException {
        final var pythonResource = fileClient.downloadPythonFile(user.fileId());
        final var pythonFileName = String.format("%s.py", user.name());
        final var jsonFileName = String.format("%s.json", user.name());

        ZipWriter.use(outputStream, zipWriter -> {
            zipWriter.addEntry(pythonFileName, pythonResource);
            zipWriter.addEntry(jsonFileName, user);
        });
    }

}
