package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.ResourceAlreadyExistsException;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.files.dto.FileDto;
import com.frytes.cloudstorage.files.dto.FileType;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import com.frytes.cloudstorage.files.repository.UserStorageWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final UserStorageReader userStorageReader;
    private final UserStorageWriter userStorageWriter;

    public List<FileDto> getAllDirectory(Long userId, String path) {
        String processedPath = PathUtils.ensureTrailingSlash(PathUtils.sanitize(path));
        String prefix = PathUtils.buildUserPath(userId, processedPath);

        List<StorageItem> items = userStorageReader.listObjects(prefix, false);
        List<FileDto> files = new ArrayList<>();

        for (StorageItem item : items) {
            if (item.path().equals(prefix)) {
                continue;
            }
            files.add(mapToFileDto(item, path));
        }

        return files;
    }

    public FileDto createDirectory(Long userId, String path) {
        String cleanPath = PathUtils.sanitize(path);
        if (cleanPath.isEmpty() || cleanPath.equals("/")) {
            throw new com.frytes.cloudstorage.common.exception.InvalidPathException("Операция 'Создание папки' недопустима для корневой директории");
        }

        String processedPath = PathUtils.ensureTrailingSlash(cleanPath);
        String objectName = PathUtils.buildUserPath(userId, processedPath);

        if (userStorageReader.isObjectExist(objectName)) {
            throw new ResourceAlreadyExistsException("Папка с таким именем уже существует");
        }

        userStorageWriter.createDirectory(objectName);

        return FileDto.builder()
                .name(PathUtils.getFileNameFromPath(processedPath))
                .path(path)
                .size(0L)
                .type(FileType.DIRECTORY)
                .lastModified(LocalDateTime.now().toString())
                .build();
    }

    private FileDto mapToFileDto(StorageItem item, String requestedPath) {
        return FileDto.builder()
                .name(PathUtils.getFileNameFromPath(item.path()))
                .size(item.size())
                .path(requestedPath == null ? "" : requestedPath)
                .type(item.isDir() ? FileType.DIRECTORY : FileType.FILE)
                .lastModified(item.isDir() ? "" : item.lastModified().toString())
                .build();
    }
}