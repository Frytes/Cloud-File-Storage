package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.files.dto.FileType;
import com.frytes.cloudstorage.files.dto.response.FileResponse;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import com.frytes.cloudstorage.files.repository.UserStorageWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FileUploadAndSearchIntegrationTest {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SearchService searchService;

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
    void shouldUploadAndSearchFile() {
        String uploadPath = "documents/work/";
        MockMultipartFile mockFile = new MockMultipartFile(
                "object",
                "report.txt",
                "text/plain",
                "Hello, Integration Test!".getBytes(StandardCharsets.UTF_8)
        );

        fileUploadService.uploadFiles(USER_ID, uploadPath, List.of(mockFile));
        List<FileResponse> foundFiles = searchService.searchUserFiles(USER_ID, "report");

        assertThat(foundFiles).hasSize(1);
        FileResponse savedFile = foundFiles.getFirst();

        assertThat(savedFile.name()).isEqualTo("report.txt");
        assertThat(savedFile.path()).isEqualTo("documents/work/report.txt");
        assertThat(savedFile.type()).isEqualTo(FileType.FILE);
        assertThat(savedFile.size()).isGreaterThan(0L);
    }
}