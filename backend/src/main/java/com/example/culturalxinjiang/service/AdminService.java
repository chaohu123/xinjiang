package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.dto.response.UserInfoResponse;
import com.example.culturalxinjiang.dto.response.CommunityPostResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.CommunityPost;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import com.example.culturalxinjiang.repository.CommunityPostRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CultureResourceRepository cultureResourceRepository;
    private final CommunityPostRepository communityPostRepository;

    // ==================== 用户管理 ====================

    @Transactional(readOnly = true)
    public PageResponse<UserInfoResponse> getUsers(Integer page, Integer size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.findByUsernameContainingOrEmailContaining(
                    keyword.trim(), keyword.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserInfoResponse> responses = userPage.getContent().stream()
                .map(this::mapToUserInfoResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, userPage.getTotalElements(), page, size);
    }

    @Transactional
    public UserInfoResponse updateUser(Long id, UserInfoResponse request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(u -> {
                        if (!u.getId().equals(id)) {
                            throw new RuntimeException("邮箱已被使用");
                        }
                    });
            user.setEmail(request.getEmail());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        user = userRepository.save(user);
        return mapToUserInfoResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.delete(user);
    }

    @Transactional
    public void toggleUserStatus(Long id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    // ==================== 文化资源管理 ====================

    @Transactional(readOnly = true)
    public PageResponse<CultureResourceResponse> getCultureResources(
            Integer page, Integer size, String keyword, CultureResource.CultureType type) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CultureResource> resourcePage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            resourcePage = cultureResourceRepository.search(
                    keyword.trim(), type, null, pageable);
        } else if (type != null) {
            resourcePage = cultureResourceRepository.findByType(type, pageable);
        } else {
            resourcePage = cultureResourceRepository.findAll(pageable);
        }

        List<CultureResourceResponse> responses = resourcePage.getContent().stream()
                .map(this::mapToCultureResourceResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, resourcePage.getTotalElements(), page, size);
    }

    @Transactional
    public CultureResourceResponse createCultureResource(
            com.example.culturalxinjiang.controller.AdminController.CultureResourceRequest request) {
        CultureResource resource = CultureResource.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .cover(request.getCover())
                .images(request.getImages() != null ? new ArrayList<>(request.getImages()) : new ArrayList<>())
                .videoUrl(request.getVideoUrl())
                .audioUrl(request.getAudioUrl())
                .content(request.getContent())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .region(request.getRegion())
                .views(0)
                .favorites(0)
                .build();

        if (request.getLocation() != null) {
            CultureResource.Location location = new CultureResource.Location(
                    request.getLocation().getLat(),
                    request.getLocation().getLng(),
                    request.getLocation().getAddress()
            );
            resource.setLocation(location);
        }

        resource = cultureResourceRepository.save(resource);
        return mapToCultureResourceResponse(resource);
    }

    @Transactional
    public CultureResourceResponse updateCultureResource(
            Long id, com.example.culturalxinjiang.controller.AdminController.CultureResourceRequest request) {
        CultureResource resource = cultureResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));

        if (request.getType() != null) {
            resource.setType(request.getType());
        }
        if (request.getTitle() != null) {
            resource.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getCover() != null) {
            resource.setCover(request.getCover());
        }
        if (request.getImages() != null) {
            resource.setImages(new ArrayList<>(request.getImages()));
        }
        if (request.getVideoUrl() != null) {
            resource.setVideoUrl(request.getVideoUrl());
        }
        if (request.getAudioUrl() != null) {
            resource.setAudioUrl(request.getAudioUrl());
        }
        if (request.getContent() != null) {
            resource.setContent(request.getContent());
        }
        if (request.getTags() != null) {
            resource.setTags(new ArrayList<>(request.getTags()));
        }
        if (request.getRegion() != null) {
            resource.setRegion(request.getRegion());
        }
        if (request.getLocation() != null) {
            CultureResource.Location location = new CultureResource.Location(
                    request.getLocation().getLat(),
                    request.getLocation().getLng(),
                    request.getLocation().getAddress()
            );
            resource.setLocation(location);
        }

        resource = cultureResourceRepository.save(resource);
        return mapToCultureResourceResponse(resource);
    }

    @Transactional
    public void deleteCultureResource(Long id) {
        CultureResource resource = cultureResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        cultureResourceRepository.delete(resource);
    }

    // ==================== 社区投稿管理 ====================

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPosts(
            Integer page, Integer size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CommunityPost> postPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            postPage = communityPostRepository.findByTitleContainingOrContentContaining(
                    keyword.trim(), keyword.trim(), pageable);
        } else {
            postPage = communityPostRepository.findAllOrderByCreatedAtDesc(pageable);
        }

        List<CommunityPostResponse> responses = postPage.getContent().stream()
                .map(this::mapToCommunityPostResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, postPage.getTotalElements(), page, size);
    }

    @Transactional
    public void approvePost(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        // 如果将来需要状态字段，可以在这里设置状态为已通过
        communityPostRepository.save(post);
    }

    @Transactional
    public void rejectPost(Long id, String reason) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        // 如果将来需要状态字段，可以在这里设置状态为已拒绝
        communityPostRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        communityPostRepository.delete(post);
    }

    // ==================== 映射方法 ====================

    private UserInfoResponse mapToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private CultureResourceResponse mapToCultureResourceResponse(CultureResource resource) {
        List<String> images = resource.getImages() != null ? new ArrayList<>(resource.getImages()) : new ArrayList<>();
        List<String> tags = resource.getTags() != null ? new ArrayList<>(resource.getTags()) : new ArrayList<>();

        CultureResourceResponse.Location location = null;
        if (resource.getLocation() != null) {
            location = new CultureResourceResponse.Location(
                    resource.getLocation().getLat(),
                    resource.getLocation().getLng(),
                    resource.getLocation().getAddress()
            );
        }

        return CultureResourceResponse.builder()
                .id(resource.getId())
                .type(resource.getType())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .cover(resource.getCover())
                .images(images)
                .videoUrl(resource.getVideoUrl())
                .audioUrl(resource.getAudioUrl())
                .content(resource.getContent())
                .tags(tags)
                .region(resource.getRegion())
                .location(location)
                .views(resource.getViews())
                .favorites(resource.getFavorites())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }

    private CommunityPostResponse mapToCommunityPostResponse(CommunityPost post) {
        var author = post.getAuthor();
        CommunityPostResponse.AuthorInfo authorInfo = new CommunityPostResponse.AuthorInfo(
                author.getId(),
                author.getUsername(),
                author.getAvatar()
        );

        List<String> images = post.getImages() != null ? new ArrayList<>(post.getImages()) : new ArrayList<>();
        List<String> tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();

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
                .isLiked(false) // 管理员查看时不需要点赞状态
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}




