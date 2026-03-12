package com.frytes.cloudstorage.files.controller;

import com.frytes.cloudstorage.files.dto.FileType;
import com.frytes.cloudstorage.files.dto.response.FileResponseDto;
import com.frytes.cloudstorage.files.service.DirectoryService;
import com.frytes.cloudstorage.users.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DirectoryController.class)
class DirectoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private DirectoryService directoryService;

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
    void shouldGetAllDirectory() throws Exception {
        List<FileResponseDto> response = List.of(
                FileResponseDto.builder().name("file1.txt").size(100L).type(FileType.FILE).path("folder/").build(),
                FileResponseDto.builder().name("subfolder/").size(0L).type(FileType.DIRECTORY).path("folder/").build()
        );

        when(directoryService.getAllDirectory(1L, "folder/")).thenReturn(response);

        mockMvc.perform(get("/api/directory")
                        .param("path", "folder/")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("file1.txt"))
                .andExpect(jsonPath("$[1].name").value("subfolder/"));
    }

    @Test
    @WithMockUser
    void shouldCreateDirectory() throws Exception {
        FileResponseDto response = FileResponseDto.builder()
                .name("newfolder/")
                .size(0L)
                .type(FileType.DIRECTORY)
                .path("newfolder/")
                .lastModified("2026-03-12T00:00:00")
                .build();

        when(directoryService.createDirectory(1L, "newfolder/")).thenReturn(response);

        mockMvc.perform(post("/api/directory")
                        .param("path", "newfolder/")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newfolder/"))
                .andExpect(jsonPath("$.type").value("DIRECTORY"));
    }
}