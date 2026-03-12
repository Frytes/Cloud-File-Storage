package com.frytes.cloudstorage.files.model;

import java.time.LocalDateTime;

public record StorageItem(
        String path,
        boolean isDir,
        Long size,
        LocalDateTime lastModified) {
}