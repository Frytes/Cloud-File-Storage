package com.frytes.cloudstorage.files.dto;


import lombok.Builder;

@Builder
public record FileDto(
        String name,
        Long size,
        FileType type,
        String path,
        String lastModified
) {}