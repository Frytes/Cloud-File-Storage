package com.frytes.cloudstorage.files.dto.response;


import com.frytes.cloudstorage.files.dto.FileType;
import lombok.Builder;

@Builder
public record FileDto(
        String name,
        Long size,
        FileType type,
        String path,
        String lastModified
) {}