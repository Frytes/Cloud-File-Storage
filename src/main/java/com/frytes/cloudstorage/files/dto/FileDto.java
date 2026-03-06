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
    public static final String TYPE_DIRECTORY = "DIRECTORY";
    public static final String TYPE_FILE = "FILE";

    private String name;
    private Long size;
    private String type;
    private String path;
    private String lastModified;
}