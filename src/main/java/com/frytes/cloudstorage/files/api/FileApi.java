package com.frytes.cloudstorage.files.api;

import com.frytes.cloudstorage.common.dto.ErrorResponse;
import com.frytes.cloudstorage.files.dto.response.FileResponse;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Метаданные успешно получены"),
            @ApiResponse(responseCode = "400", description = "Недопустимый путь", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Файл или папка не найдена", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    FileResponse getFileInfo(
            @Parameter(description = "Путь к объекту", example = "documents/report.txt") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Поиск файлов", description = "Ищет файлы и папки пользователя по вхождению подстроки в имя объекта (регистронезависимо).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результаты поиска успешно получены"),
            @ApiResponse(responseCode = "400", description = "Пустой поисковой запрос", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    List<FileResponse> searchFiles(
            @Parameter(description = "Текст для поиска", example = "report") String query,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Скачать файл или папку", description = "Если файл — бинарный поток. Если папка < 100 МБ — ZIP-архив. Если папка > 100 МБ — 202 статус и ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл или ZIP-архив готов к скачиванию (стрим)"),
            @ApiResponse(responseCode = "202", description = "Большая папка: архивация запущена в фоне, возвращен ticket"),
            @ApiResponse(responseCode = "400", description = "Недопустимый путь", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Объект не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка при скачивании или формировании архива", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Object> downloadFile(
            @Parameter(description = "Путь к скачиваемому объекту", example = "photos/vacation/") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Проверить статус фоновой сборки архива")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Архив готов (READY) или произошла ошибка (ERROR)"),
            @ApiResponse(responseCode = "202", description = "Сборка архива еще в процессе (IN_PROGRESS)"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Тикет не найден или истек", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Map<String, String>> checkDownloadStatus(
            @Parameter(description = "Идентификатор задачи", example = "550e8400-e29b-41d4-a716-446655440000") String ticket,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Загрузить файлы", description = "Загружает один или несколько файлов по указанному пути")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Файлы успешно загружены в хранилище"),
            @ApiResponse(responseCode = "400", description = "Недопустимый путь назначения", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Файл с таким именем уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "413", description = "Размер файла превышает лимит", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Ошибка сохранения в хранилище", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void uploadFiles(
            @Parameter(description = "Целевая директория (пустая строка для корня)", example = "documents/work/") String path,
            @Parameter(description = "Список файлов") List<MultipartFile> files,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Переместить или переименовать файл/папку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объект успешно перемещен/переименован"),
            @ApiResponse(responseCode = "400", description = "Недопустимый путь источника или назначения", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Исходный объект не найден", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Объект по целевому пути уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void moveFile(
            @Parameter(description = "Текущий путь к объекту", example = "old.txt") String from,
            @Parameter(description = "Новый путь к объекту", example = "new.txt") String to,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Удалить файл или папку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Объект успешно удален"),
            @ApiResponse(responseCode = "400", description = "Недопустимый путь (например, попытка удалить корень)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Ошибка удаления в хранилище", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    void deleteFile(
            @Parameter(description = "Путь к удаляемому объекту", example = "report.txt") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );
}