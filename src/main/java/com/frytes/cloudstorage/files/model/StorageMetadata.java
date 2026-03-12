package com.frytes.cloudstorage.files.model;

import java.time.LocalDateTime;

public record StorageMetadata(
        Long size,
        LocalDateTime lastModified) {
}