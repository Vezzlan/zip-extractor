package com.zip.exceptions;

public class FileClientException extends Exception {

    public FileClientException(String message) {
        super(message);
    }

    public FileClientException(Throwable cause) {
        super(cause);
    }

    public FileClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
