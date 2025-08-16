package com.app.post.mapper;

import com.app.platform.entity.Platform;
import com.app.post.dto.PostResponse;
import com.app.post.entity.Post;
import com.app.user.mapper.UserMapper;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-16T21:22:19+0530",
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

        postResponse.platformId( postPlatformId( post ) );
        postResponse.author( userMapper.toDto( post.getAuthor() ) );
        postResponse.id( post.getId() );
        postResponse.text( post.getText() );
        postResponse.purchaseDate( post.getPurchaseDate() );
        postResponse.price( post.getPrice() );
        postResponse.currency( post.getCurrency() );
        postResponse.productUrl( post.getProductUrl() );
        postResponse.likeCount( post.getLikeCount() );
        postResponse.commentCount( post.getCommentCount() );
        postResponse.repostCount( post.getRepostCount() );
        postResponse.createdAt( post.getCreatedAt() );

        return postResponse.build();
    }

    private UUID postPlatformId(Post post) {
        if ( post == null ) {
            return null;
        }
        Platform platform = post.getPlatform();
        if ( platform == null ) {
            return null;
        }
        UUID id = platform.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
