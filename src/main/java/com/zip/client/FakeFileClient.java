package com.zip.client;

import com.zip.exceptions.FileClientException;

import java.util.UUID;
import java.util.zip.ZipEntry;

public class FakeFileClient {

    public String getFileId(ZipEntry pythonEntry) {
        return UUID.randomUUID().toString();
    }

    public UUID getFileId(String fileName) throws FileClientException {
        if (fileName.equals("text.txt")) {
            throw new FileClientException("Invalid file sent: " + fileName);
        }
        return UUID.randomUUID();
    }
}
