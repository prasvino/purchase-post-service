package com.app.post.mapper;

import com.app.post.dto.PostResponse;
import com.app.user.mapper.UserMapper;
import com.app.post.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {
    @Mapping(target = "platformId", source = "post.platform.id")
    @Mapping(target = "author", source = "post.author")
    PostResponse toDto(Post post);
}
