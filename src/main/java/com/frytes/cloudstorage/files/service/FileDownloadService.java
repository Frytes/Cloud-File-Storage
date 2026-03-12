package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.config.properties.AppProperties;
import com.frytes.cloudstorage.files.dto.DownloadResponse;
import com.frytes.cloudstorage.files.dto.DownloadType;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileDownloadService {

    private final UserStorageReader userStorageReader;
    private final ArchiveService archiveService;
    private final AppProperties appProperties;


    public DownloadResponse processDownload(Long userId, String username, String path) {
        boolean isFolder = path.endsWith("/");

        if (isFolder) {
            Long totalSize = calculateFolderSize(userId, path);

            if (totalSize < appProperties.archive().syncZipLimitBytes()) {
                StreamingResponseBody zipStream = archiveService.createSyncZipStream(userId, path);
                String fileName = PathUtils.getFileNameFromPath(path);
                fileName = fileName.substring(0, fileName.length() - 1) + ".zip";

                return new DownloadResponse(DownloadType.SYNC_ZIP, null, null, zipStream, fileName);
            } else {
                String ticket = archiveService.sendArchivingTask(userId, username, path, totalSize);
                return new DownloadResponse(DownloadType.ASYNC_TASK, ticket, null, null, null);
            }
        } else {
            String objectName = PathUtils.buildUserPath(userId, PathUtils.sanitize(path));
            InputStream inputStream = userStorageReader.getFile(objectName);
            String fileName = PathUtils.getFileNameFromPath(path);

            return new DownloadResponse(DownloadType.SINGLE_FILE, null, inputStream, null, fileName);
        }
    }

    private Long calculateFolderSize(Long userId, String path) {
        String prefix = PathUtils.ensureTrailingSlash(PathUtils.buildUserPath(userId, PathUtils.sanitize(path)));
        List<StorageItem> items = userStorageReader.listObjects(prefix, true);

        long totalSize = 0L;
        for (StorageItem item : items) {
            if (!item.isDir()) {
                totalSize += item.size();
            }
        }
        return totalSize;
    }
}