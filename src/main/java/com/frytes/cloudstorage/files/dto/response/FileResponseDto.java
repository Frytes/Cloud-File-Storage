package com.frytes.cloudstorage.files.dto.response;


import com.frytes.cloudstorage.files.dto.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Информация о файле или директории в облачном хранилище")
public record FileResponseDto(
        @Schema(description = "Имя объекта", example = "report.txt")
        String name,

        @Schema(description = "Размер файла в байтах (для директорий всегда 0)", example = "1048576")
        Long size,

        @Schema(description = "Тип объекта: FILE или DIRECTORY", example = "FILE")
        FileType type,

        @Schema(description = "Относительный путь к объекту", example = "documents/work/report.txt")
        String path,

        @Schema(description = "Дата и время последнего изменения в формате ISO", example = "2024-03-12T15:30:00")
        String lastModified
) {}