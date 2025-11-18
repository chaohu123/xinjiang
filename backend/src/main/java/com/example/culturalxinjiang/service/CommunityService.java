package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.CommentRequest;
import com.example.culturalxinjiang.dto.request.CreatePostRequest;
import com.example.culturalxinjiang.dto.request.UpdatePostRequest;
import com.example.culturalxinjiang.dto.response.CommunityPostDetailResponse;
import com.example.culturalxinjiang.dto.response.CommunityPostResponse;
import com.example.culturalxinjiang.dto.response.MyCommentResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.Comment;
import com.example.culturalxinjiang.entity.CommunityPost;
import com.example.culturalxinjiang.entity.PostLike;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.CommentRepository;
import com.example.culturalxinjiang.repository.CommunityPostRepository;
import com.example.culturalxinjiang.repository.FavoriteRepository;
import com.example.culturalxinjiang.repository.PostLikeRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import com.example.culturalxinjiang.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final FavoriteRepository favoriteRepository;

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

        // 检查帖子状态：只有已审核通过的帖子才能被查看，除非是作者本人
        boolean isAuthor = false;
        try {
            User currentUser = getCurrentUser();
            // 如果是作者本人，可以查看
            isAuthor = post.getAuthor().getId().equals(currentUser.getId());
        } catch (Exception e) {
            // 用户未登录，继续检查
        }

        // 如果不是作者，只能查看已审核通过的帖子
        if (!isAuthor && !"approved".equals(post.getStatus())) {
            throw new RuntimeException("帖子不存在或未通过审核");
        }

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
    public CommunityPostDetailResponse updatePost(Long postId, UpdatePostRequest request) {
        User user = getCurrentUser();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        // 检查是否是帖子作者
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("无权修改此帖子");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getImages() != null) {
            post.setImages(new ArrayList<>(request.getImages()));
        }
        if (request.getTags() != null) {
            post.setTags(new ArrayList<>(request.getTags()));
        }

        // 如果投稿被拒绝，编辑后重新提交（状态改回pending）
        if ("rejected".equals(post.getStatus())) {
            post.setStatus("pending");
            post.setRejectReason(null); // 清空拒绝原因
        }

        post = postRepository.save(post);
        return mapToDetailResponse(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        User user = getCurrentUser();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        // 检查是否是帖子作者
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此帖子");
        }

        postRepository.delete(post);
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

    @Transactional(readOnly = true)
    public PageResponse<MyCommentResponse> getMyComments(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = commentRepository.findByAuthorId(user.getId(), pageable);

        List<MyCommentResponse> responses = commentPage.getContent().stream()
                .map(this::mapToMyCommentResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, commentPage.getTotalElements(), page, size);
    }

    @Transactional
    public void updateComment(Long commentId, CommentRequest request) {
        User user = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("无权编辑该评论");
        }

        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User user = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除该评论");
        }

        CommunityPost post = comment.getPost();
        commentRepository.delete(comment);

        long commentCount = commentRepository.countByPostId(post.getId());
        post.setComments((int) commentCount);
        postRepository.save(post);
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getRequiredUsername();
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
        boolean isFavorited = false;
        try {
            User currentUser = getCurrentUser();
            isLiked = postLikeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());
            isFavorited = favoriteRepository.existsByUserIdAndResourceTypeAndResourceId(
                    currentUser.getId(),
                    com.example.culturalxinjiang.entity.Favorite.ResourceType.POST,
                    post.getId());
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
                .status(post.getStatus() != null ? post.getStatus() : "pending")
                .rejectReason(post.getRejectReason())
                .isLiked(isLiked)
                .isFavorited(isFavorited)
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
        boolean isFavorited = false;
        try {
            User currentUser = getCurrentUser();
            isLiked = postLikeRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());
            isFavorited = favoriteRepository.existsByUserIdAndResourceTypeAndResourceId(
                    currentUser.getId(),
                    com.example.culturalxinjiang.entity.Favorite.ResourceType.POST,
                    post.getId());
        } catch (Exception e) {
            // User not authenticated
        }
        response.setIsLiked(isLiked);
        response.setIsFavorited(isFavorited);
        response.setStatus(post.getStatus() != null ? post.getStatus() : "pending");
        response.setRejectReason(post.getRejectReason());

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

    private MyCommentResponse mapToMyCommentResponse(Comment comment) {
        CommunityPost post = comment.getPost();
        List<String> images = post.getImages() != null ? post.getImages() : new ArrayList<>();
        return MyCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .postCover(images.isEmpty() ? null : images.get(0))
                .postStatus(post.getStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getMyPosts(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CommunityPost> postPage = postRepository.findByAuthorId(user.getId(), pageable);

        List<CommunityPostResponse> responses = postPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, postPage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getLikedPosts(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);
        List<PostLike> likes = postLikeRepository.findByUserId(user.getId());

        List<Long> postIds = likes.stream()
                .map(like -> like.getPost().getId())
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return PageResponse.of(new ArrayList<>(), 0L, page, size);
        }

        List<CommunityPost> posts = postRepository.findAllById(postIds);
        // Sort by like time (most recent first)
        posts.sort((a, b) -> {
            PostLike likeA = likes.stream()
                    .filter(l -> l.getPost().getId().equals(a.getId()))
                    .findFirst()
                    .orElse(null);
            PostLike likeB = likes.stream()
                    .filter(l -> l.getPost().getId().equals(b.getId()))
                    .findFirst()
                    .orElse(null);
            if (likeA == null || likeB == null) return 0;
            return likeB.getCreatedAt().compareTo(likeA.getCreatedAt());
        });

        // Apply pagination manually
        int start = (page - 1) * size;
        int end = Math.min(start + size, posts.size());
        List<CommunityPost> paginatedPosts = start < posts.size() ? posts.subList(start, end) : new ArrayList<>();

        List<CommunityPostResponse> responses = paginatedPosts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, (long) posts.size(), page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getCommentedPosts(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Comment> comments = commentRepository.findByAuthorId(user.getId());

        List<Long> postIds = comments.stream()
                .map(comment -> comment.getPost().getId())
                .distinct()
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return PageResponse.of(new ArrayList<>(), 0L, page, size);
        }

        List<CommunityPost> posts = postRepository.findAllById(postIds);
        // Sort by most recent comment time
        posts.sort((a, b) -> {
            Comment commentA = comments.stream()
                    .filter(c -> c.getPost().getId().equals(a.getId()))
                    .max((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()))
                    .orElse(null);
            Comment commentB = comments.stream()
                    .filter(c -> c.getPost().getId().equals(b.getId()))
                    .max((c1, c2) -> c1.getCreatedAt().compareTo(c2.getCreatedAt()))
                    .orElse(null);
            if (commentA == null || commentB == null) return 0;
            return commentB.getCreatedAt().compareTo(commentA.getCreatedAt());
        });

        // Apply pagination manually
        int start = (page - 1) * size;
        int end = Math.min(start + size, posts.size());
        List<CommunityPost> paginatedPosts = start < posts.size() ? posts.subList(start, end) : new ArrayList<>();

        List<CommunityPostResponse> responses = paginatedPosts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, (long) posts.size(), page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getFavoritePosts(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);

        // Get all favorites of type POST for current user
        List<com.example.culturalxinjiang.entity.Favorite> favorites =
                favoriteRepository.findByUserId(user.getId(), pageable).getContent()
                .stream()
                .filter(f -> f.getResourceType() == com.example.culturalxinjiang.entity.Favorite.ResourceType.POST)
                .collect(Collectors.toList());

        List<Long> postIds = favorites.stream()
                .map(com.example.culturalxinjiang.entity.Favorite::getResourceId)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return PageResponse.of(new ArrayList<>(), 0L, page, size);
        }

        List<CommunityPost> posts = postRepository.findAllById(postIds);
        // Sort by favorite time (most recent first)
        posts.sort((a, b) -> {
            com.example.culturalxinjiang.entity.Favorite favA = favorites.stream()
                    .filter(f -> f.getResourceId().equals(a.getId()))
                    .findFirst()
                    .orElse(null);
            com.example.culturalxinjiang.entity.Favorite favB = favorites.stream()
                    .filter(f -> f.getResourceId().equals(b.getId()))
                    .findFirst()
                    .orElse(null);
            if (favA == null || favB == null) return 0;
            return favB.getCreatedAt().compareTo(favA.getCreatedAt());
        });

        // Apply pagination manually
        int start = (page - 1) * size;
        int end = Math.min(start + size, posts.size());
        List<CommunityPost> paginatedPosts = start < posts.size() ? posts.subList(start, end) : new ArrayList<>();

        List<CommunityPostResponse> responses = paginatedPosts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, (long) posts.size(), page, size);
    }

    @Transactional
    public void favoritePost(Long postId) {
        User user = getCurrentUser();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));

        if (favoriteRepository.existsByUserIdAndResourceTypeAndResourceId(
                user.getId(),
                com.example.culturalxinjiang.entity.Favorite.ResourceType.POST,
                postId)) {
            throw new RuntimeException("已经收藏过");
        }

        com.example.culturalxinjiang.entity.Favorite favorite = com.example.culturalxinjiang.entity.Favorite.builder()
                .user(user)
                .resourceType(com.example.culturalxinjiang.entity.Favorite.ResourceType.POST)
                .resourceId(postId)
                .build();
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void unfavoritePost(Long postId) {
        User user = getCurrentUser();

        com.example.culturalxinjiang.entity.Favorite favorite = favoriteRepository.findByUserIdAndResourceTypeAndResourceId(
                user.getId(),
                com.example.culturalxinjiang.entity.Favorite.ResourceType.POST,
                postId)
                .orElseThrow(() -> new RuntimeException("未收藏"));

        favoriteRepository.delete(favorite);
    }
}

