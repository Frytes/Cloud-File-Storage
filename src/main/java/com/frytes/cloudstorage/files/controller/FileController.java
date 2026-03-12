package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.common.validate.ValidStoragePath;
import com.frytes.cloudstorage.files.dto.ArchiveStatus;
import com.frytes.cloudstorage.files.dto.response.DownloadResponse;
import com.frytes.cloudstorage.files.dto.response.FileDto;
import com.frytes.cloudstorage.files.service.*;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class FileController {

    private final ArchiveService archiveService;
    private final FileUploadService fileUploadService;
    private final FileDownloadService fileDownloadService;
    private final SearchService searchService;
    private final ResourceOperationService resourceOperationService;




    @GetMapping
    public FileDto getFileInfo(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return resourceOperationService.getFileInfo(user.getId(), path);
    }

    @GetMapping("/search")
    public List<FileDto> searchFiles(
            @RequestParam("query")
            @NotBlank(message = "Поисковой запрос не может быть пустым")
            String query,

            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return searchService.searchUserFiles(user.getId(), query);
    }

    @GetMapping("/download")
    public ResponseEntity<Object> downloadFile(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        DownloadResponse response = fileDownloadService.processDownload(user.getId(), user.getUsername(), path);

        return switch (response.type()) {
            case ASYNC_TASK -> ResponseEntity.accepted().body(Map.of(
                    "ticket", response.ticket(),
                    "message", "Начата архивация папки",
                    ArchiveStatus.STATUS_KEY, ArchiveStatus.IN_PROGRESS.name()
            ));

            case SYNC_ZIP -> buildAttachmentResponse(
                    response.fileName(),
                    "application/zip",
                    response.zipStream()
            );

            case SINGLE_FILE -> buildAttachmentResponse(
                    response.fileName(),
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    new InputStreamResource(response.stream())
            );
        };
    }

    @GetMapping("/download/status")
    public ResponseEntity<Map<String, String>> checkDownloadStatus(
            @RequestParam("ticket") String ticket,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Map<String, String> result = archiveService.getArchiveStatus(ticket, user.getId());

        String currentStatus = result.get(ArchiveStatus.STATUS_KEY);
        if (ArchiveStatus.READY.name().equals(currentStatus) ||
                ArchiveStatus.ERROR.name().equals(currentStatus)) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.accepted().body(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadFiles(
            @RequestParam("path") String path,
            @RequestParam("object") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails user
    ) {

        fileUploadService.uploadFiles(user.getId(), path, files);
    }

    @PutMapping("/move")
    @ResponseStatus(HttpStatus.OK)
    public void moveFile(
            @RequestParam("from") @ValidStoragePath String from,
            @RequestParam("to") @ValidStoragePath String to,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        resourceOperationService.moveObject(user.getId(), from, to);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(
            @RequestParam("path")
            @NotBlank(message = "Путь для удаления не может быть пустым")
            String path,

            @AuthenticationPrincipal CustomUserDetails user
    ) {
        resourceOperationService.deleteObject(user.getId(), path);
    }

    private ResponseEntity<Object> buildAttachmentResponse(String fileName, String contentType, Object body) {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(body);
    }
}