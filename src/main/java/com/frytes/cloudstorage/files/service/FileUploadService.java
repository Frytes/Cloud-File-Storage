package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.common.exception.FileUploadException;
import com.frytes.cloudstorage.common.exception.ResourceAlreadyExistsException;
import com.frytes.cloudstorage.common.util.PathUtils;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import com.frytes.cloudstorage.files.repository.UserStorageWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final UserStorageReader userStorageReader;
    private final UserStorageWriter userStorageWriter;

    public void uploadFiles(Long userId, String path, List<MultipartFile> files) {
        String directoryPath = PathUtils.ensureTrailingSlash(PathUtils.sanitize(path));

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();

            if (file.isEmpty() || originalFilename == null || originalFilename.isBlank()) {
                log.warn("Попытка загрузки пустого файла или файла без имени. Пропускаем.");
                continue;
            }

            String fullPath = directoryPath + originalFilename;
            String objectName = PathUtils.buildUserPath(userId, fullPath);

            if (userStorageReader.isObjectExist(objectName)) {
                throw new ResourceAlreadyExistsException("Файл " + originalFilename + " уже существует");
            }

            try (InputStream is = file.getInputStream()) {
                userStorageWriter.uploadFile(objectName, is, file.getContentType());
            } catch (Exception e) {
                log.error("Ошибка при загрузке файла: {}", originalFilename, e);
                throw new FileUploadException("Не удалось сохранить файл " + originalFilename, e);
            }
        }
    }
}