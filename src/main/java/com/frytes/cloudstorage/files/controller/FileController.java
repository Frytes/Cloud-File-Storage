package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.files.dto.FileDto;
import com.frytes.cloudstorage.files.service.ArchiveService;
import com.frytes.cloudstorage.files.service.FileService;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final ArchiveService archiveService;

    @GetMapping
    public FileDto getFileInfo(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return fileService.getFileInfo(user.getId(), path);
    }

    @GetMapping("/search")
    public List<FileDto> searchFiles(
            @RequestParam("query") String query,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return fileService.searchUserFiles(user.getId(), query);
    }

    @GetMapping("/download")
    public ResponseEntity<Object> downloadFile(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        if (path.endsWith("/")) {
            Long totalSize = fileService.calculateFolderSize(user.getId(), path);
            String ticket = archiveService.sendArchivingTask(user.getId(), path, totalSize);

            return ResponseEntity.accepted().body(Map.of(
                    "ticket", ticket,
                    "message", "Начата архивация папки",
                    "status", "IN_PROGRESS"
            ));
        }

        var inputStream = fileService.downloadFile(user.getId(), path);
        String fileName = PathUtils.getFileNameFromPath(path);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadFiles( // Переименовали для ясности (было uploadFile)
                             @RequestParam("path") String path,
                             @RequestParam("object") List<MultipartFile> files,
                             @AuthenticationPrincipal CustomUserDetails user
    ) {

        fileService.uploadFiles(user.getId(), path, files);
    }

    @PostMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    public void moveFile(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        fileService.moveObject(user.getId(), from, to);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        fileService.deleteObject(user.getId(), path);
    }
}