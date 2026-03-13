package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.common.exception.ResourceAlreadyExistsException;
import com.frytes.cloudstorage.files.dto.FileType;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import com.frytes.cloudstorage.files.repository.UserStorageWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ResourceOperationServiceIntegrationTest {

    @Autowired
    private ResourceOperationService resourceOperationService;

    @Autowired
    private UserStorageReader userStorageReader;

    @Autowired
    private UserStorageWriter userStorageWriter;

    private static final Long USER_ID = 999L;
    private static final String USER_PREFIX = "user-" + USER_ID + "-files/";

    private String buildPath(String path) {
        return USER_PREFIX + path;
    }

    @BeforeEach
    void setUp() {
        var items = userStorageReader.listObjects(USER_PREFIX, true);
        userStorageWriter.removeObjects(items.stream().map(StorageItem::path).toList());
    }

    @Test
    void shouldMoveFile() {
        String sourcePath = "source.txt";
        String targetPath = "target.txt";

        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(sourcePath),
                new ByteArrayInputStream(content),
                content.length,
                "text/plain"
        );

        resourceOperationService.moveObject(USER_ID, sourcePath, targetPath);

        assertThat(userStorageReader.isObjectExist(buildPath(sourcePath))).isFalse();
        assertThat(userStorageReader.isObjectExist(buildPath(targetPath))).isTrue();
    }

    @Test
    void shouldMoveFolderWithContents() {
        String sourceFolder = "source/";
        String targetFolder = "target/";

        userStorageWriter.createDirectory(buildPath(sourceFolder));

        byte[] content1 = "content1".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(sourceFolder + "file1.txt"),
                new ByteArrayInputStream(content1),
                content1.length,
                "text/plain"
        );

        userStorageWriter.createDirectory(buildPath(sourceFolder + "sub/"));

        byte[] content2 = "content2".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(sourceFolder + "sub/file2.txt"),
                new ByteArrayInputStream(content2),
                content2.length,
                "text/plain"
        );

        resourceOperationService.moveObject(USER_ID, sourceFolder, targetFolder);

        assertThat(userStorageReader.isObjectExist(buildPath(sourceFolder))).isFalse();
        assertThat(userStorageReader.isObjectExist(buildPath(sourceFolder + "file1.txt"))).isFalse();
        assertThat(userStorageReader.isObjectExist(buildPath(sourceFolder + "sub/file2.txt"))).isFalse();

        assertThat(userStorageReader.isObjectExist(buildPath(targetFolder))).isTrue();
        assertThat(userStorageReader.isObjectExist(buildPath(targetFolder + "file1.txt"))).isTrue();
        assertThat(userStorageReader.isObjectExist(buildPath(targetFolder + "sub/file2.txt"))).isTrue();
    }

    @Test
    void shouldThrowWhenTargetExists() {
        String sourcePath = "source.txt";
        String targetPath = "target.txt";

        byte[] sourceContent = "source content".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(sourcePath),
                new ByteArrayInputStream(sourceContent),
                sourceContent.length,
                "text/plain"
        );

        byte[] targetContent = "target content".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(targetPath),
                new ByteArrayInputStream(targetContent),
                targetContent.length,
                "text/plain"
        );

        assertThatThrownBy(() ->
                resourceOperationService.moveObject(USER_ID, sourcePath, targetPath)
        ).isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void shouldDeleteFile() {
        String path = "to-delete.txt";

        byte[] content = "content".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(path),
                new ByteArrayInputStream(content),
                content.length,
                "text/plain"
        );

        resourceOperationService.deleteObject(USER_ID, path);

        assertThat(userStorageReader.isObjectExist(buildPath(path))).isFalse();
    }

    @Test
    void shouldDeleteFolderRecursively() {
        String folder = "to-delete/";
        userStorageWriter.createDirectory(buildPath(folder));

        byte[] content1 = "content1".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(folder + "file1.txt"),
                new ByteArrayInputStream(content1),
                content1.length,
                "text/plain"
        );

        userStorageWriter.createDirectory(buildPath(folder + "sub/"));

        byte[] content2 = "content2".getBytes(StandardCharsets.UTF_8);
        userStorageWriter.uploadFile(
                buildPath(folder + "sub/file2.txt"),
                new ByteArrayInputStream(content2),
                content2.length,
                "text/plain"
        );

        resourceOperationService.deleteObject(USER_ID, folder);

        assertThat(userStorageReader.isObjectExist(buildPath(folder))).isFalse();
        assertThat(userStorageReader.isObjectExist(buildPath(folder + "file1.txt"))).isFalse();
        assertThat(userStorageReader.isObjectExist(buildPath(folder + "sub/file2.txt"))).isFalse();
    }

    @Test
    void shouldGetFileInfo() {
        String path = "info.txt";
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);

        userStorageWriter.uploadFile(
                buildPath(path),
                new ByteArrayInputStream(content),
                content.length,
                "text/plain"
        );

        var fileInfo = resourceOperationService.getFileInfo(USER_ID, path);

        assertThat(fileInfo.name()).isEqualTo("info.txt");
        assertThat(fileInfo.size()).isEqualTo(content.length);
        assertThat(fileInfo.type()).isEqualTo(FileType.FILE);
    }
}