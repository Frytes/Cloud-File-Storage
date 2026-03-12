package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.config.properties.AppProperties;
import com.frytes.cloudstorage.files.dto.DownloadType;
import com.frytes.cloudstorage.files.model.StorageItem;
import com.frytes.cloudstorage.files.repository.UserStorageReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceTest {

    @Mock
    private UserStorageReader userStorageReader;

    @Mock
    private ArchiveService archiveService;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Archive archiveProps;

    @InjectMocks
    private FileDownloadService fileDownloadService;

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "testuser";

    @Test
    void shouldDownloadSingleFile() {
        String filePath = "document.txt";
        String objectName = "user-1-files/document.txt";
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);

        when(userStorageReader.getFile(objectName))
                .thenReturn(new ByteArrayInputStream(content));

        var response = fileDownloadService.processDownload(USER_ID, USERNAME, filePath);

        assertThat(response.type()).isEqualTo(DownloadType.SINGLE_FILE);
        assertThat(response.fileName()).isEqualTo("document.txt");
        assertThat(response.stream()).isNotNull();
    }

    @Test
    void shouldSyncZipSmallFolder() {
        String folderPath = "photos/";
        String prefix = "user-1-files/photos/";

        when(appProperties.archive()).thenReturn(archiveProps);
        when(archiveProps.syncZipLimitBytes()).thenReturn(100L);

        List<StorageItem> items = List.of(
                new StorageItem(prefix + "1.jpg", false, 30L, LocalDateTime.now()),
                new StorageItem(prefix + "2.jpg", false, 30L, LocalDateTime.now()),
                new StorageItem(prefix + "3.jpg", false, 30L, LocalDateTime.now())
        );

        when(userStorageReader.listObjects(prefix, true)).thenReturn(items);

        StreamingResponseBody mockStream = outputStream -> {};
        when(archiveService.createSyncZipStream(USER_ID, folderPath))
                .thenReturn(mockStream);

        var response = fileDownloadService.processDownload(USER_ID, USERNAME, folderPath);

        assertThat(response.type()).isEqualTo(DownloadType.SYNC_ZIP);
        assertThat(response.fileName()).isEqualTo("photos.zip");
        assertThat(response.zipStream()).isNotNull();
    }

    @Test
    void shouldAsyncZipLargeFolder() {
        String folderPath = "videos/";
        String prefix = "user-1-files/videos/";
        String ticket = "test-ticket-123";

        when(appProperties.archive()).thenReturn(archiveProps);
        when(archiveProps.syncZipLimitBytes()).thenReturn(100L);

        List<StorageItem> items = List.of(
                new StorageItem(prefix + "1.mp4", false, 100L, LocalDateTime.now()),
                new StorageItem(prefix + "2.mp4", false, 100L, LocalDateTime.now())
        );

        when(userStorageReader.listObjects(prefix, true)).thenReturn(items);
        when(archiveService.sendArchivingTask(USER_ID, USERNAME, folderPath, 200L))
                .thenReturn(ticket);

        var response = fileDownloadService.processDownload(USER_ID, USERNAME, folderPath);

        assertThat(response.type()).isEqualTo(DownloadType.ASYNC_TASK);
        assertThat(response.ticket()).isEqualTo(ticket);
    }
}