package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.*;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.files.dto.DownloadResponse;
import com.frytes.cloudstorage.files.dto.FileDto;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final ArchiveService archiveService;

    @Value("${app.search.limit:50}")
    private int searchLimit;

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
                        .type(item.isDir() ? FileDto.TYPE_DIRECTORY : FileDto.TYPE_FILE)
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

    public FileDto createDirectory(Long userId, String path) {
        validateNotRoot(path, "Создание папки");
        String processedPath = PathUtils.ensureTrailingSlash(PathUtils.sanitize(path));
        String objectName = PathUtils.buildUserPath(userId, processedPath);

        if (minioService.isObjectExist(objectName)) {
            throw new ResourceAlreadyExistsException("Папка с таким именем уже существует");
        }

        minioService.createDirectory(objectName);

        return FileDto.builder()
                .name(PathUtils.getFileNameFromPath(processedPath))
                .path(path)
                .size(0L)
                .type(FileDto.TYPE_DIRECTORY)
                .lastModified(java.time.LocalDateTime.now().toString())
                .build();
    }

    public FileDto getFileInfo(Long userId, String path) {
        String cleanPath = PathUtils.sanitize(path);

        String objectName = PathUtils.buildUserPath(userId, cleanPath);

        var stat = minioService.getMetadata(objectName);

        boolean isDir = cleanPath.endsWith("/");

        return FileDto.builder()
                .name(PathUtils.getFileNameFromPath(cleanPath))
                .path(cleanPath)
                .size(stat.size())
                .type(isDir ? FileDto.TYPE_DIRECTORY : FileDto.TYPE_FILE)
                .lastModified(stat.lastModified().toString())
                .build();
    }

    public void uploadFiles(Long userId, String path, List<MultipartFile> files) {
        String directoryPath = PathUtils.ensureTrailingSlash(PathUtils.sanitize(path));

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String fullPath = directoryPath + file.getOriginalFilename();
            String objectName = PathUtils.buildUserPath(userId, fullPath);

            if (minioService.isObjectExist(objectName)) {
                throw new ResourceAlreadyExistsException("Файл " + file.getOriginalFilename() + " уже существует");
            }

            try {
                minioService.upload(objectName, file.getInputStream(), file.getContentType());
            } catch (IOException e) {
                log.error("Ошибка при чтении файла: {}", file.getOriginalFilename(), e);
                throw new FileUploadException("Не удалось прочитать файл " + file.getOriginalFilename(), e);
            }
        }
    }

    public DownloadResponse processDownload(Long userId, String username, String path) {
        boolean isFolder = path.endsWith("/");

        if (isFolder) {
            Long totalSize = calculateFolderSize(userId, path);
            String ticket = archiveService.sendArchivingTask(userId, username, path, totalSize);
            return new DownloadResponse(true, ticket, null, null);
        } else {
            InputStream inputStream = downloadFile(userId, path);
            String fileName = PathUtils.getFileNameFromPath(path);
            return new DownloadResponse(false, null, inputStream, fileName);
        }
    }

    public List<FileDto> searchUserFiles(Long userId, String query) {
        String prefix = PathUtils.buildUserPath(userId, "");
        Iterable<Result<Item>> results = minioService.listObjectsRecursive(prefix);

        List<FileDto> foundFiles = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Result<Item> result : results) {
            if (foundFiles.size() >= searchLimit) {
                break;
            }
            try {
                Item item = result.get();
                String objectName = item.objectName();
                String fileName = PathUtils.getFileNameFromPath(objectName);
                if (fileName.toLowerCase().contains(lowerQuery)) {
                    String userPath = objectName.substring(prefix.length());

                    foundFiles.add(FileDto.builder()
                            .name(fileName)
                            .size(item.size())
                            .path(userPath)
                            .type(item.isDir() ? FileDto.TYPE_DIRECTORY : FileDto.TYPE_FILE)
                            .lastModified(item.lastModified().toString())
                            .build());
                }
            } catch (Exception e) {
                log.error("Ошибка при поиске", e);
            }
        }
        return foundFiles;
    }

    public void deleteObject(Long userId, String path) {
        validateNotRoot(path, "Удаление");
        String objectName = PathUtils.buildUserPath(userId, PathUtils.sanitize(path));
        boolean isFolder = path.endsWith("/");

        if (isFolder) {
            deleteFolderRecursively(objectName);
        } else {
            minioService.removeObject(objectName);
        }
    }

    public void moveObject(Long userId, String fromPath, String toPath) {
        validateNotRoot(fromPath, "Перемещение (источник)");
        String source = PathUtils.buildUserPath(userId, PathUtils.sanitize(fromPath));
        String target = PathUtils.buildUserPath(userId, PathUtils.sanitize(toPath));

        if (minioService.isObjectExist(target)) {
            throw new ResourceAlreadyExistsException("Ресурс по целевому пути уже существует");
        }

        boolean isFolder = fromPath.endsWith("/");

        if (isFolder) {
            moveFolderRecursively(source, target);
        } else {
            minioService.copyObject(source, target);
            minioService.removeObject(source);
        }
    }

    private InputStream downloadFile(Long userId, String path) {
        String objectName = PathUtils.buildUserPath(userId, PathUtils.sanitize(path));
        return minioService.getFile(objectName);
    }

    private void moveFolderRecursively(String sourcePrefix, String targetPrefix) {
        String src = PathUtils.ensureTrailingSlash(sourcePrefix);
        String tgt = PathUtils.ensureTrailingSlash(targetPrefix);

        Iterable<Result<Item>> objects = minioService.listObjectsRecursive(src);
        List<String> successfullyCopiedSources = new ArrayList<>();
        List<String> successfullyCopiedTargets = new ArrayList<>();

        for (Result<Item> result : objects) {
            try {
                String oldKey = result.get().objectName();
                String newKey = oldKey.replaceFirst(src, tgt);

                minioService.copyObject(oldKey, newKey);
                successfullyCopiedSources.add(oldKey);
                successfullyCopiedTargets.add(newKey);
            } catch (Exception e) {
                log.error("Сбой при копировании объекта, запускаем откат: {}", e.getMessage());
                rollbackMovedFiles(successfullyCopiedTargets);
                throw new StorageOperationException("Ошибка при перемещении папки. Изменения отменены.", e);
            }
        }

        boolean hasErrors = false;
        for (String oldKey : successfullyCopiedSources) {
            try {
                minioService.removeObject(oldKey);
            } catch (Exception e) {
                log.error("Не удалось удалить оригинал после копирования: {}", e.getMessage());
                hasErrors = true;
            }
        }

        if (hasErrors) {
            throw new StorageOperationException("Папка скопирована, но некоторые старые файлы не удалось удалить.");
        }
    }

    private void rollbackMovedFiles(List<String> targetKeys) {
        for (String targetKey : targetKeys) {
            try {
                minioService.removeObject(targetKey);
            } catch (Exception e) {
                log.error("КРИТИЧЕСКАЯ ОШИБКА ОТКАТА! Не удалось удалить файл: {}", targetKey, e);
            }
        }
    }

    private void deleteFolderRecursively(String prefix) {
        Iterable<Result<Item>> objects = minioService.listObjectsRecursive(prefix);
        boolean hasErrors = false;

        for (Result<Item> result : objects) {
            try {
                minioService.removeObject(result.get().objectName());
            } catch (Exception e) {
                log.error("Не удалось удалить объект: {}", e.getMessage());
                hasErrors = true;
            }
        }

        if (hasErrors) {
            throw new StorageOperationException("Папка удалена частично. Некоторые файлы не удалось удалить.");
        }
    }

    private Long calculateFolderSize(Long userId, String path) {
        String prefix = PathUtils.buildUserPath(userId, PathUtils.sanitize(path));
        prefix = PathUtils.ensureTrailingSlash(prefix);

        Iterable<Result<Item>> results = minioService.listObjectsRecursive(prefix);
        long totalSize = 0L;

        for (Result<Item> result : results) {
            try {
                Item item = result.get();
                if (!item.isDir()) {
                    totalSize += item.size();
                }
            } catch (Exception e) {
                log.error("Ошибка при подсчете размера папки: {}", prefix, e);
            }
        }
        return totalSize;
    }

    private void validateNotRoot(String path, String operation) {
        String clean = PathUtils.sanitize(path);
        if (clean.isEmpty() || clean.equals("/")) {
            throw new InvalidPathException("Операция '" + operation + "' недопустима для корневой директории");
        }
    }

    private String formatDate(Item item) {
        if (item.isDir()) return "";
        return item.lastModified() != null ? item.lastModified().toString() : "";
    }
}