package com.huynhduc.application.service;

import com.huynhduc.application.entity.Comment;
import com.huynhduc.application.model.request.CreateCommentPostRequest;
import com.huynhduc.application.model.request.CreateCommentProductRequest;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    Comment createCommentPost(CreateCommentPostRequest createCommentPostRequest, long userId);
    Comment createCommentProduct(CreateCommentProductRequest createCommentProductRequest, long userId);
}
