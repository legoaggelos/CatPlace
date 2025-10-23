package com.legoaggelos.catplace.cats.posts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.legoaggelos.catplace.security.deserializers.OffsetDateTimeDeserializer;

import com.legoaggelos.catplace.security.deserializers.SerialBlobDeserializer;
import org.h2.jdbc.JdbcBlob;
import org.springframework.data.annotation.Id;

import javax.sql.rowset.serial.SerialBlob;
import java.time.OffsetDateTime;
import java.util.List;

public record Post(@Id Long id,
                   @JsonDeserialize(using = SerialBlobDeserializer.class) SerialBlob image,
                   Long likeCount,
                   Long catOwner,
                   String userOwner,
                   String desc,
                   @JsonDeserialize(using = OffsetDateTimeDeserializer.class) OffsetDateTime uploadDate,
                   Boolean isApproved) {
    public Post(Long id, SerialBlob image, Long catOwner, String userOwner, String desc, OffsetDateTime uploadDate, boolean isApproved) {
        this(id, image,0L, catOwner, userOwner, desc, uploadDate, isApproved);
    }
    public Post(Long id, Long likeCount, SerialBlob image, Long catOwner, String userOwner, String desc, OffsetDateTime uploadDate) {
        this(id, image, likeCount, catOwner, userOwner, desc, uploadDate, null);
    }
}