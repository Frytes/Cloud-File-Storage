package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.files.dto.ArchiveStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ArchiveServiceIntegrationTest {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoSpyBean
    private ArchiveListener archiveListener;

    @Test
    void shouldSendAndReceiveMessage() {
        String path = "photos/vacation";
        Long userId = 100L;
        Long size = 500L;
        String userName = "testName";

        archiveService.sendArchivingTask(userId, userName, path, size);
        verify(archiveListener, timeout(5000).times(1)).listen(any());
    }

    @Test
    void shouldSendTaskAndSaveStatusToRedis() {
        String path = "photos/";
        Long userId = 100L;
        Long size = 500L;
        String userName = "test_user";

        String ticketId = archiveService.sendArchivingTask(userId, userName, path, size);

        Map<String, String> status = archiveService.getArchiveStatus(ticketId);

        assertThat(status).isNotNull();
        assertThat(status.get(ArchiveStatus.STATUS_KEY)).isEqualTo(ArchiveStatus.IN_PROGRESS.name());
        String redisValue = redisTemplate.opsForValue().get("archive:status:" + ticketId);
        assertThat(redisValue).isEqualTo("IN_PROGRESS");
    }
}