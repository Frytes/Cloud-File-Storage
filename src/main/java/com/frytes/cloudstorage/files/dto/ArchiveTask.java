package com.frytes.cloudstorage.files.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record ArchiveTask(
        String username,
        String ticketId,
        Long userId,
        String path,
        Long totalSize
) implements Serializable {}