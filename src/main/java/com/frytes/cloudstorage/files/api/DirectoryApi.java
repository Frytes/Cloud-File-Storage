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

import java.util.List;

@Tag(name = "Директории", description = "API для работы с директориями (папками) пользователя")
public interface DirectoryApi {

    @Operation(summary = "Получить содержимое директории", description = "Возвращает список файлов и папок. Для получения корневой директории передайте пустую строку.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список содержимого успешно получен"),
            @ApiResponse(responseCode = "400", description = "Недопустимый формат пути (например, попытка выйти за пределы директории)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    List<FileResponse> getAllDirectory(
            @Parameter(description = "Путь к папке (со слэшем на конце). Оставьте пустым для корня.", example = "documents/work/") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );

    @Operation(summary = "Создать новую директорию", description = "Создает пустую папку (виртуальный объект) по указанному пути.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Директория успешно создана"),
            @ApiResponse(responseCode = "400", description = "Недопустимый формат пути", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Директория с таким именем уже существует", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Ошибка создания директории в хранилище", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<FileResponse> createDirectory(
            @Parameter(description = "Полный путь новой папки (включая её имя и слэш на конце)", example = "photos/vacation_2024/") String path,
            @Parameter(hidden = true) CustomUserDetails user
    );
}