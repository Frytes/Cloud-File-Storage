package com.frytes.cloudstorage.users.dto;

import com.frytes.cloudstorage.users.dto.request.RegisterRequest;
import com.frytes.cloudstorage.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "provider", ignore = true)
    User toEntity(RegisterRequest request);
}

