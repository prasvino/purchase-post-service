package com.app.user.mapper;

import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-16T21:22:18+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserSummary toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserSummary.UserSummaryBuilder userSummary = UserSummary.builder();

        userSummary.id( user.getId() );
        userSummary.username( user.getUsername() );
        userSummary.displayName( user.getDisplayName() );
        userSummary.avatarUrl( user.getAvatarUrl() );

        return userSummary.build();
    }
}
