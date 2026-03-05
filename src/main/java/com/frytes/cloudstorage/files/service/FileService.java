package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.DirectoryReadException;
import com.frytes.cloudstorage.common.exception.FileUploadException;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.files.dto.FileDto;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioService minioService;

    public FileDto createDirectory(Long userId, String path) {
        String processedPath = PathUtils.ensureTrailingSlash(PathUtils.sanitize(path));
        String objectName = PathUtils.buildUserPath(userId, processedPath);

        minioService.createDirectory(objectName);

        return FileDto.builder()
                .name(PathUtils.getFileNameFromPath(processedPath))
                .path(path)
                .size(0L)
                .type("DIRECTORY")
                .lastModified(java.time.LocalDateTime.now().toString())
                .build();
    }

    public void uploadFile(Long userId, String path, MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("Файл пустой", null);
        }

        String directoryPath = PathUtils.ensureTrailingSlash(PathUtils.sanitize(path));
        String fullPath = directoryPath + file.getOriginalFilename();
        String objectName = PathUtils.buildUserPath(userId, fullPath);

        minioService.upload(objectName, getInputStream(file), file.getContentType());
    }

    public List<FileDto> getAllDirectory(Long userId, String path) {
        String processedPath = PathUtils.ensureTrailingSlash(PathUtils.sanitize(path));
        String prefix = PathUtils.buildUserPath(userId, processedPath);

        Iterable<Result<Item>> results = minioService.listObjects(prefix);
        List<FileDto> files = new ArrayList<>();

        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                String objectName = item.objectName();

                if (objectName.equals(prefix)) {
                    continue;
                }

                FileDto dto = FileDto.builder()
                        .name(PathUtils.getFileNameFromPath(objectName))
                        .size(item.size())
                        .path(path == null ? "" : path)
                        .type(item.isDir() ? "DIRECTORY" : "FILE")
                        .lastModified(formatDate(item))
                        .build();

                files.add(dto);

            } catch (Exception e) {
                log.error("Ошибка при чтении файла из MinIO", e);
                throw new DirectoryReadException("Ошибка чтения списка файлов из хранилища", e);
            }
        }

        return files;
    }

    private InputStream getInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            throw new FileUploadException("Ошибка чтения загружаемого файла", e);
        }
    }

    private String formatDate(Item item) {
        if (item.isDir()) return "";
        return item.lastModified() != null ? item.lastModified().toString() : "";
    }
}