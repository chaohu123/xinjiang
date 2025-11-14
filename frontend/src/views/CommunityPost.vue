<template>
  <div class="community-post-page">
    <div class="container">
      <div v-loading="loading" class="post-detail">
        <div v-if="postDetail" class="post-content">
          <div class="post-header">
            <el-avatar :src="postDetail.author.avatar" :size="50">
              {{ postDetail.author.username[0] }}
            </el-avatar>
            <div class="post-author">
              <div class="author-name">
                {{ postDetail.author.username }}
              </div>
              <div class="post-time">
                {{ fromNow(postDetail.createdAt) }}
              </div>
            </div>
          </div>
          <h1 class="post-title">
            {{ postDetail.title }}
          </h1>
          <div class="post-tags">
            <el-tag
              v-for="tag in postDetail.tags"
              :key="tag"
              size="large"
              style="margin-right: 8px"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="post-body">
            <p class="post-text">
              {{ postDetail.content }}
            </p>
            <div v-if="postDetail.images && postDetail.images.length > 0" class="post-images">
              <el-image
                v-for="(img, index) in postDetail.images"
                :key="index"
                :src="img"
                fit="cover"
                :preview-src-list="postDetail.images"
                :initial-index="index"
                class="post-image"
              />
            </div>
          </div>
          <div class="post-actions">
            <el-button :type="postDetail.isLiked ? 'primary' : 'default'" @click="handleLike">
              <el-icon class="like-icon">
                <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="18" height="18">
                  <path d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-8.1-59.2-26.5-82.3-18.4-23.1-44.6-38.7-73.8-44.4a67.67 67.67 0 0 0-51.1 11.3l-385.4 252.2a41.3 41.3 0 0 0-15.3 48.3l87.2 273.4c8.6 27.1 33.1 45.5 61.4 45.5h258.1c16.8 0 33.1-6.5 45.3-18.1l226.1-207.6a101.55 101.55 0 0 0 25.4-33.7zM112 528v364.7c0 17.7 14.9 32.7 32.7 32.7h267.2V495.3H112z" fill="currentColor"/>
                </svg>
              </el-icon>
              {{ postDetail.likes }}
            </el-button>
            <el-button
              :type="postDetail.isFavorited ? 'primary' : 'default'"
              @click="handleFavorite"
              :class="{ 'favorited': postDetail.isFavorited }"
            >
              <el-icon :class="{ 'favorited-icon': postDetail.isFavorited }">
                <Star />
              </el-icon>
              {{ postDetail.isFavorited ? '已收藏' : '收藏' }}
            </el-button>
            <el-button @click="handleShare">
              <el-icon><Share /></el-icon>
              {{ $t('community.share') }}
            </el-button>
          </div>
        </div>

        <div v-if="postDetail" class="comments-section">
          <h2>评论 ({{ validComments.length }})</h2>
          <div class="comment-form">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
            />
            <el-button type="primary" style="margin-top: 12px" @click="handleComment">
              发表评论
            </el-button>
          </div>
          <div class="comments-list">
            <template v-if="validComments.length > 0">
              <div v-for="comment in validComments" :key="comment.id" class="comment-item">
                <el-avatar :src="comment.author?.avatar" :size="36">
                  {{ comment.author?.username?.[0] || 'U' }}
                </el-avatar>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.author?.username || '匿名用户' }}</span>
                    <span class="comment-time">{{ fromNow(comment.createdAt) }}</span>
                  </div>
                  <p class="comment-text">
                    {{ comment.content }}
                  </p>
                </div>
              </div>
            </template>
            <div v-else class="empty-comments">
              <p>暂无评论，快来发表第一条评论吧！</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getPostDetail, likePost, unlikePost, commentPost, favoritePost, unfavoritePost } from '@/api/community'
import type { CommunityPostDetail, Comment } from '@/types/community'
import { fromNow } from '@/utils'
import { Star, Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const postDetail = ref<CommunityPostDetail | null>(null)
const loading = ref(false)
const commentContent = ref('')

// 过滤出有效的评论（有 author 的评论）
const validComments = computed(() => {
  if (!postDetail.value) {
    return []
  }
  const comments = postDetail.value.comments || (postDetail.value as any).commentList
  // 确保 comments 是数组
  if (!Array.isArray(comments)) {
    return []
  }
  return comments.filter((comment: Comment) => comment && comment.author)
})

const loadDetail = async () => {
  const id = parseInt(route.params.id as string)
  loading.value = true
  try {
    const data = await getPostDetail(id)
    // 处理后端返回的 commentList 字段，统一为 comments
    if (data) {
      // 优先使用 commentList，如果没有则使用 comments
      const commentList = (data as any).commentList || data.comments
      // 确保是数组类型
      if (Array.isArray(commentList)) {
        data.comments = commentList
      } else {
        data.comments = []
      }
    }
    postDetail.value = data
  } catch (error) {
    console.error('Failed to load post detail:', error)
    ElMessage.error('加载帖子详情失败')
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!postDetail.value) return
  try {
    if (postDetail.value.isLiked) {
      await unlikePost(postDetail.value.id)
      postDetail.value.likes--
      postDetail.value.isLiked = false
    } else {
      await likePost(postDetail.value.id)
      postDetail.value.likes++
      postDetail.value.isLiked = true
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleComment = async () => {
  if (!postDetail.value || !commentContent.value.trim()) return
  try {
    await commentPost(postDetail.value.id, commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadDetail()
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

const handleFavorite = async () => {
  if (!postDetail.value) return
  try {
    if (postDetail.value.isFavorited) {
      await unfavoritePost(postDetail.value.id)
      postDetail.value.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await favoritePost(postDetail.value.id)
      postDetail.value.isFavorited = true
      ElMessage.success('收藏成功')
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  }
}

const handleShare = () => {
  const url = window.location.href
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
  } else {
    ElMessage.info('请手动复制链接: ' + url)
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.community-post-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.post-detail {
  background: white;
  border-radius: 8px;
  padding: 30px;
}

.post-header {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.post-author {
  flex: 1;
}

.author-name {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.post-time {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.post-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.post-tags {
  margin-bottom: 24px;
}

.post-body {
  margin-bottom: 30px;
}

.post-text {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  margin-bottom: 24px;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.post-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
}

.post-actions {
  display: flex;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.like-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 16px;
    height: 16px;
  }
}

.comments-section {
  margin-top: 40px;
  padding-top: 40px;
  border-top: 1px solid #ebeef5;

  h2 {
    font-size: 24px;
    margin-bottom: 24px;
    color: #303133;
  }
}

.comment-form {
  margin-bottom: 30px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  color: #606266;
  line-height: 1.6;
}

.favorited {
  background-color: #fdf6ec;
  border-color: #f5dab1;
  color: #e6a23c;

  &:hover {
    background-color: #fdf6ec;
    border-color: #f5dab1;
    color: #e6a23c;
  }
}

.favorited-icon {
  color: #f7ba2a;
}

.empty-comments {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}
</style>
