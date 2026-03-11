package com.frytes.cloudstorage.files.dto;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;

public record DownloadResponse(
        DownloadType type,
        String ticket,
        InputStream stream,
        StreamingResponseBody zipStream,
        String fileName
) {}