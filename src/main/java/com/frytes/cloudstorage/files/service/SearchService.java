package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.config.properties.AppProperties;
import com.frytes.cloudstorage.files.dto.response.FileDto;
import com.frytes.cloudstorage.files.dto.FileType;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserStorageReader userStorageReader;
    private final AppProperties appProperties;

    public List<FileDto> searchUserFiles(Long userId, String query) {
        String prefix = PathUtils.buildUserPath(userId, "");
        List<StorageItem> items = userStorageReader.listObjects(prefix, true);

        List<FileDto> foundFiles = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (StorageItem item : items) {
            if (foundFiles.size() >= appProperties.search().limit()) {
                break;
            }

            String objectName = item.path();
            String fileName = PathUtils.getFileNameFromPath(objectName);

            if (fileName.toLowerCase().contains(lowerQuery)) {
                String userPath = objectName.substring(prefix.length());
                String lastModified = item.lastModified() != null ? item.lastModified().toString() : "";

                foundFiles.add(FileDto.builder()
                        .name(fileName)
                        .size(item.size())
                        .path(userPath)
                        .type(item.isDir() ? FileType.DIRECTORY : FileType.FILE)
                        .lastModified(lastModified)
                        .build());
            }
        }
        return foundFiles;
    }
}