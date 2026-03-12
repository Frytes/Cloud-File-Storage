package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.files.dto.response.FileResponseDto;
import com.frytes.cloudstorage.files.dto.FileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class FileUploadAndSearchIntegrationTest {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private SearchService searchService;

    @Test
    void shouldUploadAndSearchFile() {
        Long userId = 999L;
        String uploadPath = "documents/work/";
        MockMultipartFile mockFile = new MockMultipartFile(
                "object",
                "report.txt",
                "text/plain",
                "Hello, Integration Test!".getBytes()
        );

        fileUploadService.uploadFiles(userId, uploadPath, List.of(mockFile));
        List<FileResponseDto> foundFiles = searchService.searchUserFiles(userId, "report");

        assertThat(foundFiles).hasSize(1);
        FileResponseDto savedFile = foundFiles.getFirst();

        assertThat(savedFile.name()).isEqualTo("report.txt");
        assertThat(savedFile.path()).isEqualTo("documents/work/report.txt");
        assertThat(savedFile.type()).isEqualTo(FileType.FILE);
        assertThat(savedFile.size()).isGreaterThan(0L);
    }
}