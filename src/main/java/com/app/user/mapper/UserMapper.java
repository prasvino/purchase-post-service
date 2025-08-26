package com.app.user.mapper;

import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Converts User entities to UserResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "name", source = "displayName")
    @Mapping(target = "avatar", source = "avatarUrl")
    UserSummary toDto(User user);
}
