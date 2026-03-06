package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ArchiveServiceIntegrationTest {

    @Autowired
    private ArchiveService archiveService;

    @MockitoSpyBean
    private ArchiveListener archiveListener;

    @Test
    void shouldSendAndReceiveMessage() {
        String path = "photos/vacation";
        Long userId = 100L;
        Long size = 500L;

        archiveService.sendArchivingTask(userId, path, size);
        verify(archiveListener, timeout(5000).times(1)).listen(any());
    }
}