package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioService minioService;

    @SneakyThrows // не забыть убрать
    public void uploadFile(Long userId, String path, MultipartFile file) {
        String objectName = generateObjectKey(userId, path, file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new FileUploadException("Файл пустой", null);
        }
        minioService.upload(
                objectName,
                file.getInputStream(),
                file.getContentType()
        );
    }

    private String generateObjectKey(Long userId, String path, String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append("user-").append(userId).append("-files");

        if (path != null && !path.isBlank()) {
            String cleanPath = path.trim()
                    .replaceAll("/+", "/")
                    .replaceAll("^/|/$", "");

            if (!cleanPath.isEmpty()) {
                sb.append("/").append(cleanPath);
            }
        }
        sb.append("/").append(filename);

        return sb.toString();
    }
}