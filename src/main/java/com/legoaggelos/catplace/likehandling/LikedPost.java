package com.legoaggelos.catplace.likehandling;

import org.springframework.data.annotation.Id;

public record LikedPost(@Id Long id, String username, Long postLikedId) {
}
