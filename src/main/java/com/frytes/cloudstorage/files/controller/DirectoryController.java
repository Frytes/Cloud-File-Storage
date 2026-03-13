package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.common.validate.ValidStoragePath;
import com.frytes.cloudstorage.files.dto.response.FileResponseDto;
import com.frytes.cloudstorage.files.service.DirectoryService;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Директории", description = "API для работы с директориями пользователя")
@Validated
@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    @Operation(summary = "Получить содержимое директории", description = "Возвращает список файлов и папок по указанному пути")
    @GetMapping
    public List<FileResponseDto> getAllDirectory(
            @RequestParam("path") String path,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    ) {
        return directoryService.getAllDirectory(user.getId(), path);
    }

    @Operation(summary = "Создать новую директорию", description = "Создает пустую папку по указанному пути")
    @PostMapping
    public ResponseEntity<FileResponseDto> createDirectory(
            @RequestParam("path") @ValidStoragePath String path,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    ) {
        FileResponseDto newDirectory = directoryService.createDirectory(user.getId(), path);
        return ResponseEntity.ok(newDirectory);
    }
}