package com.frytes.cloudstorage.users.dto;

import com.frytes.cloudstorage.common.validate.ValidPassword;
import com.frytes.cloudstorage.common.validate.ValidUsername;

public record LoginRequest(
        @ValidUsername String username,
        @ValidPassword String password
) {}
