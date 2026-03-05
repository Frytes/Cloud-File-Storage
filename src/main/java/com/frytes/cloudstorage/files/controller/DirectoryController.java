package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.files.dto.FileDto;
import com.frytes.cloudstorage.files.service.FileService;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final FileService fileService;

    @GetMapping
    public List<FileDto> getAllDirectory(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return fileService.getAllDirectory(user.getId(), path);
    }

    @PostMapping
    public ResponseEntity<FileDto> createDirectory(
            @RequestParam("path") String path,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        FileDto newDirectory = fileService.createDirectory(user.getId(), path);
        return ResponseEntity.ok(newDirectory);
    }
}