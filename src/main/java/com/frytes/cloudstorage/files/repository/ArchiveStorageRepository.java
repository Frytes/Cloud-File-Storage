package com.frytes.cloudstorage.files.repository;

import java.io.InputStream;

public interface ArchiveStorageRepository {
    void uploadArchive(String path, InputStream inputStream);
    String getPresignedUrl(String path);
}




