package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.common.validate.ValidStoragePath;
import com.frytes.cloudstorage.files.dto.response.FileResponseDto;
import com.frytes.cloudstorage.files.service.DirectoryService;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Validated
@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

    @GetMapping
    public List<FileResponseDto> getAllDirectory(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return directoryService.getAllDirectory(user.getId(), path);
    }

    @PostMapping
    public ResponseEntity<FileResponseDto> createDirectory(
            @RequestParam("path") @ValidStoragePath String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        FileResponseDto newDirectory = directoryService.createDirectory(user.getId(), path);
        return ResponseEntity.ok(newDirectory);
    }
}