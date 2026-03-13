package com.frytes.cloudstorage.files.service;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.files.dto.ArchiveStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
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

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldSendAndReceiveMessage() {
        String path = "photos/vacation";
        Long userId = 100L;
        Long size = 500L;
        String userName = "testName";

        String ticketId = archiveService.sendArchivingTask(userId, userName, path, size);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
                verify(archiveListener).listen(argThat(task -> task.ticketId().equals(ticketId)))
        );
    }

    @Test
    void shouldSendTaskAndSaveStatusToRedis() {
        String path = "photos/";
        Long userId = 100L;
        Long size = 500L;
        String userName = "test_user";

        String ticketId = archiveService.sendArchivingTask(userId, userName, path, size);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String redisValue = redisTemplate.opsForValue().get(ArchiveService.getRedisKey(ticketId));
            assertThat(redisValue).isEqualTo("READY");
        });

        Map<String, String> status = archiveService.getArchiveStatus(ticketId, userId);
        assertThat(status)
                .isNotNull()
                .containsEntry(ArchiveStatus.STATUS_KEY, ArchiveStatus.READY.name())
                .containsKey("downloadUrl");
    }
}