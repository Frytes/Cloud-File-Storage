package com.frytes.cloudstorage.common.exception;

public class DirectoryReadException extends RuntimeException {
    public DirectoryReadException(String message, Throwable cause) {
        super(message, cause);
    }
}