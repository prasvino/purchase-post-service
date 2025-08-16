package com.app.user.mapper;

import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import org.mapstruct.Mapper;

/**
 * Converts User entities to UserResponse DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserSummary toDto(User user);
}
