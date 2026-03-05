package com.frytes.cloudstorage.files.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDto {
    private String name;
    private Long size;
    private String type;
    private String path;
    private String lastModified;
}