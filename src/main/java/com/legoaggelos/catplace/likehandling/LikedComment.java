package com.legoaggelos.catplace.likehandling;

import org.springframework.data.annotation.Id;

public record LikedComment(@Id Long id, String username, Long commentLikedId) {
}
