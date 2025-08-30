package com.app.media.mapper;

import com.app.media.dto.MediaResponse;
import com.app.media.entity.Media;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    
    MediaResponse toDto(Media media);
}