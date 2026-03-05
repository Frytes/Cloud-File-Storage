package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.files.service.FileService;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public void uploadFile(
            @RequestParam("path") String path,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        fileService.uploadFile(user.getId(), path, file);
    }
}