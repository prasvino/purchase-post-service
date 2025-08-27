package com.app.post.mapper;

import com.app.post.dto.PostResponse;
import com.app.post.entity.Post;
import com.app.user.mapper.UserMapper;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-26T22:34:41+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PostResponse toDto(Post post) {
        if ( post == null ) {
            return null;
        }

        PostResponse.PostResponseBuilder postResponse = PostResponse.builder();

        postResponse.user( userMapper.toDto( post.getAuthor() ) );
        postResponse.content( post.getText() );
        postResponse.timestamp( post.getCreatedAt() );
        postResponse.likes( post.getLikeCount() );
        postResponse.comments( post.getCommentCount() );
        postResponse.reposts( post.getRepostCount() );
        postResponse.shares( post.getShareCount() );
        postResponse.platform( mapPlatform( post.getPlatform() ) );
        postResponse.media( post.getProductUrl() );
        postResponse.tags( defaultTags( post ) );
        postResponse.id( post.getId() );
        postResponse.purchaseDate( post.getPurchaseDate() );
        postResponse.price( post.getPrice() );
        postResponse.currency( post.getCurrency() );

        postResponse.isLiked( false );
        postResponse.isReposted( false );
        postResponse.isShared( false );

        return postResponse.build();
    }
}
