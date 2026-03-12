package com.frytes.cloudstorage.files.repository;

import java.io.InputStream;
import java.util.List;

public interface UserStorageWriter {
    void createDirectory(String path);
    void uploadFile(String path, InputStream inputStream, String contentType);
    void removeObject(String path);
    void removeObjects(List<String> paths);
    void copyObject(String source, String target);
}
