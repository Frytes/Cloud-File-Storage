package com.frytes.cloudstorage.files.dto;

import java.io.InputStream;

public record DownloadResponse(
        boolean isAsync,
        String ticket,
        InputStream stream,
        String fileName
) {}