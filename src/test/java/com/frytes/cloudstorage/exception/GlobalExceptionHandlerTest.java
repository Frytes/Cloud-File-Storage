package com.frytes.cloudstorage.exception;

import com.frytes.cloudstorage.common.exception.*;
import com.frytes.cloudstorage.files.controller.FileController;
import com.frytes.cloudstorage.files.service.*;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    @WithMockUser
    void shouldReturn400ForInvalidPath() throws Exception {
        doThrow(new InvalidPathException("Недопустимый путь"))
                .when(resourceOperationService).getFileInfo(1L, "../bad-path");

        mockMvc.perform(get("/api/resource")
                        .param("path", "../bad-path")
                        .with(user(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Недопустимый путь"));
    }

    @Test
    @WithMockUser
    void shouldReturn404ForNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Файл не найден"))
                .when(resourceOperationService).deleteObject(1L, "not-found.txt");

        mockMvc.perform(delete("/api/resource")
                        .param("path", "not-found.txt")
                        .with(user(testUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Файл не найден"));
    }

    @Test
    @WithMockUser
    void shouldReturn409ForConflict() throws Exception {
        doThrow(new ResourceAlreadyExistsException("Файл уже существует"))
                .when(resourceOperationService).moveObject(1L, "source.txt", "target.txt");

        mockMvc.perform(put("/api/resource/move")
                        .param("from", "source.txt")
                        .param("to", "target.txt")
                        .with(user(testUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Файл уже существует"));
    }
}