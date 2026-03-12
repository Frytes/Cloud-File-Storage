package com.frytes.cloudstorage.files.repository;

import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.model.StorageMetadata;

import java.io.InputStream;
import java.util.List;

public interface UserStorageReader {
    InputStream getFile(String path);
    boolean isObjectExist(String path);
    StorageMetadata getMetadata(String path);
    List<StorageItem> listObjects(String prefix, boolean recursive);
}