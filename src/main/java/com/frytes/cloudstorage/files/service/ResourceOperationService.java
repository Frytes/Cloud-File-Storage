package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.InvalidPathException;
import com.frytes.cloudstorage.common.exception.ResourceAlreadyExistsException;
import com.frytes.cloudstorage.common.exception.StorageOperationException;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.files.dto.response.FileDto;
import com.frytes.cloudstorage.files.dto.FileType;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.model.StorageMetadata;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import com.frytes.cloudstorage.files.repository.UserStorageWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceOperationService {

    private final UserStorageReader userStorageReader;
    private final UserStorageWriter userStorageWriter;

    public FileDto getFileInfo(Long userId, String path) {
        String cleanPath = PathUtils.sanitize(path);
        String objectName = PathUtils.buildUserPath(userId, cleanPath);
        StorageMetadata stat = userStorageReader.getMetadata(objectName);
        boolean isDir = cleanPath.endsWith("/");

        return FileDto.builder()
                .name(PathUtils.getFileNameFromPath(cleanPath))
                .path(cleanPath)
                .size(stat.size())
                .type(isDir ? FileType.DIRECTORY : FileType.FILE)
                .lastModified(stat.lastModified() != null ? stat.lastModified().toString() : "")
                .build();
    }

    public void deleteObject(Long userId, String path) {
        validateNotRoot(path, "Удаление");
        String objectName = PathUtils.buildUserPath(userId, PathUtils.sanitize(path));
        boolean isFolder = path.endsWith("/");

        if (isFolder) {
            deleteFolderRecursively(objectName);
        } else {
            userStorageWriter.removeObject(objectName);
        }
    }

    public void moveObject(Long userId, String fromPath, String toPath) {
        validateNotRoot(fromPath, "Перемещение (источник)");
        String source = PathUtils.buildUserPath(userId, PathUtils.sanitize(fromPath));
        String target = PathUtils.buildUserPath(userId, PathUtils.sanitize(toPath));

        if (userStorageReader.isObjectExist(target)) {
            log.warn("Move failed: Target path '{}' already exists for user {}", toPath, userId);
            throw new ResourceAlreadyExistsException("Ресурс по целевому пути уже существует");
        }

        boolean isFolder = fromPath.endsWith("/");

        if (isFolder) {
            moveFolderRecursively(source, target);
        } else {
            userStorageWriter.copyObject(source, target);
            userStorageWriter.removeObject(source);
        }
    }

    private void moveFolderRecursively(String sourcePrefix, String targetPrefix) {
        String src = PathUtils.ensureTrailingSlash(sourcePrefix);
        String tgt = PathUtils.ensureTrailingSlash(targetPrefix);

        List<StorageItem> objects = userStorageReader.listObjects(src, true);
        List<String> successfullyCopiedSources = new ArrayList<>();
        List<String> successfullyCopiedTargets = new ArrayList<>();

        for (StorageItem item : objects) {
            try {
                String oldKey = item.path();
                String newKey = PathUtils.replacePrefix(oldKey, src, tgt);

                userStorageWriter.copyObject(oldKey, newKey);
                successfullyCopiedSources.add(oldKey);
                successfullyCopiedTargets.add(newKey);
            } catch (Exception e) {
                log.error("Folder move failed. Rolling back successfully copied files. Source: {}", sourcePrefix, e);
                rollbackMovedFiles(successfullyCopiedTargets);
                throw new StorageOperationException("Ошибка при перемещении папки. Изменения отменены.", e);
            }
        }

        if (!successfullyCopiedSources.isEmpty()) {
            userStorageWriter.removeObjects(successfullyCopiedSources);
        }
    }

    private void rollbackMovedFiles(List<String> targetKeys) {
        if (!targetKeys.isEmpty()) {
            userStorageWriter.removeObjects(targetKeys);
        }
    }

    private void deleteFolderRecursively(String prefix) {
        List<StorageItem> objects = userStorageReader.listObjects(prefix, true);
        List<String> keysToDelete = objects.stream().map(StorageItem::path).toList();

        if (!keysToDelete.isEmpty()) {
            userStorageWriter.removeObjects(keysToDelete);
        }
    }

    private void validateNotRoot(String path, String operation) {
        String clean = PathUtils.sanitize(path);
        if (clean.isEmpty() || clean.equals("/")) {
            log.warn("Validation failed: Operation '{}' not allowed on root directory", operation);
            throw new InvalidPathException("Операция недопустима для корневой директории");
        }
    }
}