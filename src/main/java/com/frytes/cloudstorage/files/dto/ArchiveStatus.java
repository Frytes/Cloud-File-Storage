package com.frytes.cloudstorage.files.dto;

import java.util.Map;

public enum ArchiveStatus {
    IN_PROGRESS, READY, ERROR;

    public static final String STATUS_KEY = "status";

    public Map<String, String> toResponse(String url) {
        if (this == READY) {
            return Map.of(STATUS_KEY, name(), "url", url);
        }
        return Map.of(STATUS_KEY, name());
    }
}