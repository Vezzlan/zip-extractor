package com.zip.client;

import com.zip.exceptions.AirportInfoException;

import java.util.UUID;
import java.util.zip.ZipEntry;

public class FakeFileClient {

    public String getFileId(ZipEntry pythonEntry) {
        return UUID.randomUUID().toString();
    }

    public UUID getFileId(String fileName) throws AirportInfoException {
        if (fileName.equals("text.txt")) {
            throw new AirportInfoException("Invalid file sent: " + fileName);
        }
        return UUID.randomUUID();
    }
}
