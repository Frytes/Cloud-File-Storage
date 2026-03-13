package com.frytes.cloudstorage.files.api;

import com.frytes.cloudstorage.files.dto.response.FileResponse;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Директории", description = "API для работы с директориями (папками) пользователя")
public interface DirectoryApi {

    @Operation(summary = "Получить содержимое директории", description = "Возвращает список файлов и папок. Для получения корневой директории передайте пустую строку.")
    List<FileResponse> getAllDirectory(
            @Parameter(description = "Путь к папке (со слэшем на конце). Оставьте пустым для корня.", example = "documents/work/") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Создать новую директорию", description = "Создает пустую папку (виртуальный объект) по указанному пути.")
    ResponseEntity<FileResponse> createDirectory(
            @Parameter(description = "Полный путь новой папки (включая её имя и слэш на конце)", example = "photos/vacation_2024/") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );
}