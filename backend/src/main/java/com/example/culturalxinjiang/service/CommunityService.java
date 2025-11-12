package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.CommentRequest;
import com.example.culturalxinjiang.dto.request.CreatePostRequest;
import com.example.culturalxinjiang.dto.response.CommunityPostDetailResponse;
import com.example.culturalxinjiang.dto.response.CommunityPostResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.Comment;
import com.example.culturalxinjiang.entity.CommunityPost;
import com.example.culturalxinjiang.entity.PostLike;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.CommentRepository;
import com.example.culturalxinjiang.repository.CommunityPostRepository;
import com.example.culturalxinjiang.repository.PostLikeRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPosts(String sort, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CommunityPost> postPage;

        if ("hot".equals(sort)) {
            postPage = postRepository.findAllOrderByHot(pageable);
        } else {
            postPage = postRepository.findAllOrderByCreatedAtDesc(pageable);
        }

        List<CommunityPostResponse> responses = postPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, postPage.getTotalElements(), page, size);
    }

    @Transactional
    public CommunityPostDetailResponse getPostDetail(Long id) {
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        // Increment views
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        return mapToDetailResponse(post);
    }

    @Transactional
    public CommunityPostDetailResponse createPost(CreatePostRequest request) {
        User user = getCurrentUser();

        CommunityPost post = CommunityPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .tags(request.getTags())
                .author(user)
                .likes(0)
                .comments(0)
                .views(0)
                .build();

        post = postRepository.save(post);
        return mapToDetailResponse(post);
    }

    @Transactional
    public void likePost(Long postId) {
        User user = getCurrentUser();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (postLikeRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            throw new RuntimeException("已经点赞过");
        }

        PostLike postLike = PostLike.builder()
                .user(user)
                .post(post)
                .build();
        postLikeRepository.save(postLike);

        post.setLikes(post.getLikes() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId) {
        User user = getCurrentUser();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        PostLike postLike = postLikeRepository.findByUserIdAndPostId(user.getId(), postId)
                .orElseThrow(() -> new RuntimeException("未点赞"));

        postLikeRepository.delete(postLike);

        post.setLikes(Math.max(0, post.getLikes() - 1));
        postRepository.save(post);
    }

    @Transactional
    public void commentPost(Long postId, CommentRequest request) {
        User user = getCurrentUser();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(user)
                .post(post)
                .build();
        commentRepository.save(comment);

        post.setComments(post.getComments() + 1);
        postRepository.save(post);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private CommunityPostResponse mapToResponse(CommunityPost post) {
        // 显式访问懒加载属性，确保在事务内加载
        // 访问 author 关系
        var author = post.getAuthor();
        CommunityPostResponse.AuthorInfo authorInfo = new CommunityPostResponse.AuthorInfo(
                author.getId(),
                author.getUsername(),
                author.getAvatar()
        );

        // 显式访问懒加载集合
        List<String> images = post.getImages() != null ? new ArrayList<>(post.getImages()) : new ArrayList<>();
        List<String> tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();

        boolean isLiked = false;
        try {
            User currentUser = getCurrentUser();
            isLiked = postLikeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());
        } catch (Exception e) {
            // User not authenticated
        }

        return CommunityPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .images(images)
                .author(authorInfo)
                .tags(tags)
                .likes(post.getLikes())
                .comments(post.getComments())
                .views(post.getViews())
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private CommunityPostDetailResponse mapToDetailResponse(CommunityPost post) {
        // 显式访问懒加载属性，确保在事务内加载
        var author = post.getAuthor();
        List<String> images = post.getImages() != null ? new ArrayList<>(post.getImages()) : new ArrayList<>();
        List<String> tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();
        
        CommunityPostDetailResponse response = new CommunityPostDetailResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setImages(images);
        response.setAuthor(new CommunityPostResponse.AuthorInfo(
                author.getId(),
                author.getUsername(),
                author.getAvatar()
        ));
        response.setTags(tags);
        response.setLikes(post.getLikes());
        response.setComments(post.getComments());
        response.setViews(post.getViews());

        boolean isLiked = false;
        try {
            User currentUser = getCurrentUser();
            isLiked = postLikeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());
        } catch (Exception e) {
            // User not authenticated
        }
        response.setIsLiked(isLiked);

        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());

        List<Comment> topLevelComments = commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId())
                .stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());

        response.setCommentList(topLevelComments.stream()
                .map(comment -> {
                    // 显式访问 comment 的 author
                    var commentAuthor = comment.getAuthor();
                    CommunityPostDetailResponse.CommentResponse commentResponse = new CommunityPostDetailResponse.CommentResponse();
                    commentResponse.setId(comment.getId());
                    commentResponse.setContent(comment.getContent());
                    commentResponse.setAuthor(new CommunityPostResponse.AuthorInfo(
                            commentAuthor.getId(),
                            commentAuthor.getUsername(),
                            commentAuthor.getAvatar()
                    ));
                    commentResponse.setCreatedAt(comment.getCreatedAt());

                    List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId());
                    commentResponse.setReplies(replies.stream()
                            .map(reply -> {
                                // 显式访问 reply 的 author
                                var replyAuthor = reply.getAuthor();
                                CommunityPostDetailResponse.CommentResponse replyResponse = new CommunityPostDetailResponse.CommentResponse();
                                replyResponse.setId(reply.getId());
                                replyResponse.setContent(reply.getContent());
                                replyResponse.setAuthor(new CommunityPostResponse.AuthorInfo(
                                        replyAuthor.getId(),
                                        replyAuthor.getUsername(),
                                        replyAuthor.getAvatar()
                                ));
                                replyResponse.setCreatedAt(reply.getCreatedAt());
                                return replyResponse;
                            })
                            .collect(Collectors.toList()));

                    return commentResponse;
                })
                .collect(Collectors.toList()));

        return response;
    }
}

