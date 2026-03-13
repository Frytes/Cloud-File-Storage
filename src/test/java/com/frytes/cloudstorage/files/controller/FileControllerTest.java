package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.files.dto.FileType;
import com.frytes.cloudstorage.files.dto.response.FileResponse;
import com.frytes.cloudstorage.files.service.*;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ArchiveService archiveService;

    @MockitoBean
    private FileUploadService fileUploadService;

    @MockitoBean
    private FileDownloadService fileDownloadService;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private ResourceOperationService resourceOperationService;

    private final CustomUserDetails testUser = new CustomUserDetails(
            1L, "testuser", "password", List.of(() -> "ROLE_USER")
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    void shouldGetFileInfo() throws Exception {
        FileResponse response = FileResponse.builder()
                .name("test.txt")
                .size(100L)
                .type(FileType.FILE)
                .path("test.txt")
                .lastModified("2026-03-12T00:00:00")
                .build();

        when(resourceOperationService.getFileInfo(1L, "test.txt")).thenReturn(response);

        mockMvc.perform(get("/api/resource")
                        .param("path", "test.txt")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test.txt"))
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    @WithMockUser
    void shouldSearchFiles() throws Exception {
        List<FileResponse> response = List.of(
                FileResponse.builder().name("test1.txt").size(100L).type(FileType.FILE).path("test1.txt").build(),
                FileResponse.builder().name("test2.txt").size(200L).type(FileType.FILE).path("test2.txt").build()
        );

        when(searchService.searchUserFiles(1L, "test")).thenReturn(response);

        mockMvc.perform(get("/api/resource/search")
                        .param("query", "test")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("test1.txt"))
                .andExpect(jsonPath("$[1].name").value("test2.txt"));
    }

    @Test
    @WithMockUser
    void shouldUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "object",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes(StandardCharsets.UTF_8)
        );

        doNothing().when(fileUploadService).uploadFiles(eq(1L), eq("uploads/"), anyList());

        mockMvc.perform(multipart("/api/resource")
                        .file(file)
                        .param("path", "uploads/")
                        .with(user(testUser))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldDeleteFile() throws Exception {
        doNothing().when(resourceOperationService).deleteObject(1L, "to-delete.txt");

        mockMvc.perform(delete("/api/resource")
                        .param("path", "to-delete.txt")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}