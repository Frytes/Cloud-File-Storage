package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.common.exception.InvalidPathException;
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
class DirectoryServiceIntegrationTest {

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private UserStorageReader userStorageReader;

    @Autowired
    private UserStorageWriter userStorageWriter;

    private static final Long USER_ID = 999L;
    private static final String USER_PREFIX = "user-" + USER_ID + "-files/";

    @BeforeEach
    void setUp() {
        var items = userStorageReader.listObjects(USER_PREFIX, true);
        userStorageWriter.removeObjects(items.stream().map(StorageItem::path).toList());
    }

    @Test
    void shouldCreateDirectory() {
        var result = directoryService.createDirectory(USER_ID, "new-folder/");

        assertThat(result.name()).isEqualTo("new-folder/");
        assertThat(result.type()).isEqualTo(FileType.DIRECTORY);
        assertThat(result.size()).isZero();
        assertThat(userStorageReader.isObjectExist(USER_PREFIX + "new-folder/")).isTrue();
    }

    @Test
    void shouldNotCreateRootDirectory() {
        assertThatThrownBy(() -> directoryService.createDirectory(USER_ID, ""))
                .isInstanceOf(InvalidPathException.class);

        assertThatThrownBy(() -> directoryService.createDirectory(USER_ID, "/"))
                .isInstanceOf(InvalidPathException.class);
    }

    @Test
    void shouldNotCreateExistingDirectory() {
        directoryService.createDirectory(USER_ID, "existing/");

        assertThatThrownBy(() -> directoryService.createDirectory(USER_ID, "existing/"))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void shouldListEmptyDirectory() {
        directoryService.createDirectory(USER_ID, "empty/");

        var contents = directoryService.getAllDirectory(USER_ID, "empty/");

        assertThat(contents).isEmpty();
    }

    @Test
    void shouldListDirectoryContents() {
        directoryService.createDirectory(USER_ID, "test/");

        byte[] content1 = "content1".getBytes(StandardCharsets.UTF_8);
        byte[] content2 = "content2".getBytes(StandardCharsets.UTF_8);

        userStorageWriter.uploadFile(
                USER_PREFIX + "test/file1.txt",
                new ByteArrayInputStream(content1),
                content1.length,
                "text/plain"
        );
        userStorageWriter.uploadFile(
                USER_PREFIX + "test/file2.txt",
                new ByteArrayInputStream(content2),
                content2.length,
                "text/plain"
        );
        directoryService.createDirectory(USER_ID, "test/sub/");

        var contents = directoryService.getAllDirectory(USER_ID, "test/");

        assertThat(contents).hasSize(3);

        var file1 = contents.stream().filter(f -> f.name().equals("file1.txt")).findFirst();
        assertThat(file1).isPresent();
        assertThat(file1.get().type()).isEqualTo(FileType.FILE);
        assertThat(file1.get().size()).isEqualTo(content1.length);

        var file2 = contents.stream().filter(f -> f.name().equals("file2.txt")).findFirst();
        assertThat(file2).isPresent();
        assertThat(file2.get().size()).isEqualTo(content2.length);

        var subdir = contents.stream().filter(f -> f.name().equals("sub/")).findFirst();
        assertThat(subdir).isPresent();
        assertThat(subdir.get().type()).isEqualTo(FileType.DIRECTORY);
    }
}