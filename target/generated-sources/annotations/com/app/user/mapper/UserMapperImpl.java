package com.app.user.mapper;

import com.app.user.dto.UserSummary;
import com.app.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-24T00:02:53+0530",
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

        userSummary.name( user.getDisplayName() );
        userSummary.avatar( user.getAvatarUrl() );
        userSummary.id( user.getId() );
        userSummary.username( user.getUsername() );
        userSummary.bio( user.getBio() );
        userSummary.location( user.getLocation() );
        userSummary.website( user.getWebsite() );
        userSummary.joinedAt( user.getJoinedAt() );
        userSummary.isVerified( user.getIsVerified() );
        userSummary.followersCount( user.getFollowersCount() );
        userSummary.followingCount( user.getFollowingCount() );
        userSummary.postsCount( user.getPostsCount() );
        userSummary.totalSpent( user.getTotalSpent() );
        userSummary.avgRating( user.getAvgRating() );
        userSummary.isOnline( user.getIsOnline() );

        return userSummary.build();
    }
}
