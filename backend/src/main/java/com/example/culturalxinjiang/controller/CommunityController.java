package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.CommentRequest;
import com.example.culturalxinjiang.dto.request.CreatePostRequest;
import com.example.culturalxinjiang.dto.request.UpdatePostRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.CommunityPostDetailResponse;
import com.example.culturalxinjiang.dto.response.CommunityPostResponse;
import com.example.culturalxinjiang.dto.response.MyCommentResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.service.CommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    public ApiResponse<PageResponse<CommunityPostResponse>> getPosts(
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<CommunityPostResponse> response = communityService.getPosts(sort, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<CommunityPostDetailResponse> getPostDetail(@PathVariable Long id) {
        CommunityPostDetailResponse response = communityService.getPostDetail(id);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<CommunityPostDetailResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        CommunityPostDetailResponse response = communityService.createPost(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<CommunityPostDetailResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request) {
        CommunityPostDetailResponse response = communityService.updatePost(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        communityService.deletePost(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Void> likePost(@PathVariable Long id) {
        communityService.likePost(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> unlikePost(@PathVariable Long id) {
        communityService.unlikePost(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<Void> commentPost(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        communityService.commentPost(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/comments/my")
    public ApiResponse<PageResponse<MyCommentResponse>> getMyComments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<MyCommentResponse> response = communityService.getMyComments(page, size);
        return ApiResponse.success(response);
    }

    @PutMapping("/comments/{id}")
    public ApiResponse<Void> updateComment(@PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        communityService.updateComment(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/comments/{id}")
    public ApiResponse<Void> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<CommunityPostResponse>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<CommunityPostResponse> response = communityService.getMyPosts(page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/liked")
    public ApiResponse<PageResponse<CommunityPostResponse>> getLikedPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<CommunityPostResponse> response = communityService.getLikedPosts(page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/commented")
    public ApiResponse<PageResponse<CommunityPostResponse>> getCommentedPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<CommunityPostResponse> response = communityService.getCommentedPosts(page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<CommunityPostResponse>> getFavoritePosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<CommunityPostResponse> response = communityService.getFavoritePosts(page, size);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> favoritePost(@PathVariable Long id) {
        communityService.favoritePost(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/favorite")
    public ApiResponse<Void> unfavoritePost(@PathVariable Long id) {
        communityService.unfavoritePost(id);
        return ApiResponse.success(null);
    }
}






