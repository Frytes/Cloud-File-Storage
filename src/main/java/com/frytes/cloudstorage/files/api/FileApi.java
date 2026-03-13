package com.frytes.cloudstorage.files.api;

import com.frytes.cloudstorage.files.dto.response.FileResponse;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "Файлы и Объекты", description = "API для управления файлами: загрузка, скачивание, перемещение и удаление")
public interface FileApi {

    @Operation(summary = "Получить метаданные файла/папки")
    FileResponse getFileInfo(
            @Parameter(description = "Путь к объекту", example = "documents/report.txt") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Поиск файлов", description = "Ищет файлы и папки пользователя по вхождению подстроки в имя объекта (регистронезависимо).")
    List<FileResponse> searchFiles(
            @Parameter(description = "Текст для поиска", example = "report") String query,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Скачать файл или папку", description = "Если файл — бинарный поток. Если папка < 100 МБ — ZIP-архив. Если папка > 100 МБ — 202 статус и ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл или ZIP-архив готов к скачиванию"),
            @ApiResponse(responseCode = "202", description = "Архивация запущена в фоне")
    })
    ResponseEntity<Object> downloadFile(
            @Parameter(description = "Путь к скачиваемому объекту", example = "photos/vacation/") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Проверить статус фоновой сборки архива")
    ResponseEntity<Map<String, String>> checkDownloadStatus(
            @Parameter(description = "Идентификатор задачи", example = "550e8400-e29b-41d4-a716-446655440000") String ticket,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Загрузить файлы")
    @ApiResponse(responseCode = "201", description = "Файлы успешно загружены в хранилище")
    void uploadFiles(
            @Parameter(description = "Целевая директория (пустая строка для корня)", example = "documents/work/") String path,
            @Parameter(description = "Список файлов") List<MultipartFile> files,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Переместить или переименовать файл/папку")
    @ApiResponse(responseCode = "200", description = "Объект успешно перемещен")
    void moveFile(
            @Parameter(description = "Текущий путь к объекту", example = "old.txt") String from,
            @Parameter(description = "Новый путь к объекту", example = "new.txt") String to,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Удалить файл или папку")
    @ApiResponse(responseCode = "204", description = "Объект успешно удален")
    void deleteFile(
            @Parameter(description = "Путь к удаляемому объекту", example = "report.txt") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );
}