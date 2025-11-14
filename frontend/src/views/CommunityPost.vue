<template>
  <div class="community-post-page">
    <div class="container">
      <div v-loading="loading" class="post-detail">
        <div v-if="postDetail" class="post-content">
          <!-- 顶部操作栏 -->
          <div class="post-top-bar">
            <el-button
              class="back-button"
              :icon="ArrowLeft"
              text
              @click="handleBack"
            >
              返回社区
            </el-button>
            <div class="post-author-info">
              <el-avatar
                :src="postDetail.author.avatar"
                :size="40"
                class="author-avatar"
                @click="handleAuthorClick"
              >
                {{ postDetail.author.username[0] }}
              </el-avatar>
              <div class="author-details">
                <div class="author-name" @click="handleAuthorClick">
                  {{ postDetail.author.username }}
                </div>
                <div class="post-meta">
                  <span class="post-time">{{ fromNow(postDetail.createdAt) }}</span>
                  <span v-if="postDetail.views" class="post-views">
                    <el-icon><View /></el-icon>
                    {{ postDetail.views }} 次浏览
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- 帖子标题 -->
          <h1 class="post-title">
            {{ postDetail.title }}
          </h1>

          <!-- 标签 -->
          <div class="post-tags">
            <el-tag
              v-for="tag in postDetail.tags"
              :key="tag"
              size="large"
              class="post-tag"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>

          <!-- 帖子内容 -->
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
                :preview-teleported="true"
                class="post-image"
                loading="lazy"
                @click="handleImageClick(index)"
              />
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="post-actions">
            <div class="action-group">
              <LikeButton
                :initial-count="postDetail.likes"
                :initial-active="postDetail.isLiked || false"
                @like-toggled="(isActive) => handleLikeToggle(isActive)"
              />
              <el-button
                :type="postDetail.isFavorited ? 'primary' : 'default'"
                @click="handleFavorite"
                :class="{ 'favorited': postDetail.isFavorited }"
                circle
              >
                <el-icon :class="{ 'favorited-icon': postDetail.isFavorited }">
                  <Star />
                </el-icon>
              </el-button>
              <el-button @click="handleShare" circle>
                <el-icon><Share /></el-icon>
              </el-button>
            </div>
            <div class="action-stats">
              <span class="stat-item">
                <el-icon><ChatLineRound /></el-icon>
                {{ validComments.length }} 条评论
              </span>
            </div>
          </div>
        </div>

        <div v-if="postDetail" class="comments-section">
          <div class="comments-header">
            <h2>
              <el-icon><ChatLineRound /></el-icon>
              评论 ({{ validComments.length }})
            </h2>
          </div>
          <div class="comment-form">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="4"
              placeholder="写下你的评论..."
              maxlength="500"
              show-word-limit
              :disabled="!userStore.isLoggedIn"
            />
            <div class="comment-form-actions">
              <el-button
                v-if="!userStore.isLoggedIn"
                type="primary"
                @click="$router.push('/login')"
              >
                登录后评论
              </el-button>
              <el-button
                v-else
                type="primary"
                :loading="commenting"
                @click="handleComment"
              >
                <el-icon><Edit /></el-icon>
                发表评论
              </el-button>
            </div>
          </div>
          <div class="comments-list">
            <template v-if="validComments.length > 0">
              <div v-for="comment in validComments" :key="comment.id" class="comment-item">
                <el-avatar
                  :src="comment.author?.avatar"
                  :size="40"
                  class="comment-avatar"
                  @click="handleCommentAuthorClick(comment.author?.id)"
                >
                  {{ comment.author?.username?.[0] || 'U' }}
                </el-avatar>
                <div class="comment-content">
                  <div class="comment-header">
                    <span
                      class="comment-author"
                      @click="handleCommentAuthorClick(comment.author?.id)"
                    >
                      {{ comment.author?.username || '匿名用户' }}
                    </span>
                    <div class="comment-meta">
                      <span class="comment-time">{{ fromNow(comment.createdAt) }}</span>
                      <el-button
                        v-if="userStore.isLoggedIn"
                        text
                        size="small"
                        class="reply-button"
                        @click="handleReply(comment)"
                      >
                        <el-icon><Message /></el-icon>
                        回复
                      </el-button>
                    </div>
                  </div>
                  <p class="comment-text">
                    {{ comment.content }}
                  </p>
                  <!-- 回复列表 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                    <div
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      class="reply-item"
                    >
                      <el-avatar :src="reply.author?.avatar" :size="28">
                        {{ reply.author?.username?.[0] || 'U' }}
                      </el-avatar>
                      <div class="reply-content">
                        <span
                          class="reply-author"
                          @click="handleCommentAuthorClick(reply.author?.id)"
                        >
                          {{ reply.author?.username }}
                        </span>
                        <span class="reply-text">{{ reply.content }}</span>
                        <span class="reply-time">{{ fromNow(reply.createdAt) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <div v-else class="empty-comments">
              <el-empty description="暂无评论，快来发表第一条评论吧！" :image-size="120" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostDetail, likePost, unlikePost, commentPost, favoritePost, unfavoritePost } from '@/api/community'
import type { CommunityPostDetail, Comment } from '@/types/community'
import { fromNow } from '@/utils'
import LikeButton from '@/components/common/LikeButton.vue'
import { Star, Share, ArrowLeft, View, ChatLineRound, Message, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const postDetail = ref<CommunityPostDetail | null>(null)
const loading = ref(false)
const commentContent = ref('')
const commenting = ref(false)

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

const handleLikeToggle = async (isActive: boolean) => {
  if (!postDetail.value) return
  try {
    if (isActive) {
      await likePost(postDetail.value.id)
      postDetail.value.likes = (postDetail.value.likes || 0) + 1
      postDetail.value.isLiked = true
    } else {
      await unlikePost(postDetail.value.id)
      postDetail.value.likes = Math.max(0, (postDetail.value.likes || 0) - 1)
      postDetail.value.isLiked = false
    }
  } catch (error) {
    ElMessage.error('操作失败')
    // 回滚状态
    if (postDetail.value) {
      postDetail.value.isLiked = !isActive
      postDetail.value.likes = isActive
        ? Math.max(0, (postDetail.value.likes || 0) - 1)
        : (postDetail.value.likes || 0) + 1
    }
  }
}

const handleBack = () => {
  router.push('/community')
}

const handleImageClick = (index: number) => {
  // 图片点击事件，el-image 会自动处理预览
  // 这里可以添加额外的逻辑，比如统计点击次数等
}

const handleAuthorClick = () => {
  // 可以跳转到用户个人主页，目前先显示提示
  ElMessage.info('用户主页功能开发中')
  // router.push(`/user/${postDetail.value?.author.id}`)
}

const handleCommentAuthorClick = (authorId?: number) => {
  if (authorId) {
    ElMessage.info('用户主页功能开发中')
    // router.push(`/user/${authorId}`)
  }
}

const handleReply = (comment: Comment) => {
  // 回复功能，可以在输入框中添加 @用户名
  commentContent.value = `@${comment.author?.username} `
  // 聚焦到输入框
  const textarea = document.querySelector('.comment-form textarea') as HTMLTextAreaElement
  if (textarea) {
    textarea.focus()
  }
}

const handleComment = async () => {
  if (!postDetail.value || !commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  commenting.value = true
  try {
    await commentPost(postDetail.value.id, commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadDetail()
    // 滚动到评论区域
    setTimeout(() => {
      const commentsSection = document.querySelector('.comments-section')
      if (commentsSection) {
        commentsSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    }, 100)
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commenting.value = false
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
  padding: 20px 0 40px;
  min-height: calc(100vh - 70px);
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100px);
}

.post-detail {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  max-width: 900px;
  margin: 0 auto;
}

// 顶部操作栏
.post-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.back-button {
  font-size: 15px;
  color: #606266;
  padding: 8px 16px;
  transition: all 0.3s;

  &:hover {
    color: #409eff;
    background-color: #ecf5ff;
  }
}

.post-author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  cursor: pointer;
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.1);
  }
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #409eff;
  }
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.post-views {
  display: flex;
  align-items: center;
  gap: 4px;
}

// 帖子标题
.post-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #303133;
  line-height: 1.4;
}

// 标签
.post-tags {
  margin-bottom: 28px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.post-tag {
  font-size: 14px;
  padding: 6px 14px;
  border-radius: 16px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
  }
}

// 帖子内容
.post-body {
  margin-bottom: 32px;
}

.post-text {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
  margin-bottom: 24px;
  word-break: break-word;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.post-image {
  width: 100%;
  height: 250px;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.3s;
  object-fit: cover;

  &:hover {
    transform: scale(1.02);
  }
}

// 操作按钮
.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.action-group {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-stats {
  display: flex;
  gap: 20px;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
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

// 评论区域
.comments-section {
  margin-top: 48px;
  padding-top: 40px;
  border-top: 2px solid #ebeef5;
}

.comments-header {
  margin-bottom: 28px;

  h2 {
    font-size: 22px;
    font-weight: 600;
    color: #303133;
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
  }
}

.comment-form {
  margin-bottom: 36px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.comment-form-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 12px;
  transition: all 0.3s;

  &:hover {
    background: #f5f7fa;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
}

.comment-avatar {
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.1);
  }
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 8px;
}

.comment-author {
  font-weight: 600;
  color: #303133;
  cursor: pointer;
  transition: color 0.3s;
  font-size: 15px;

  &:hover {
    color: #409eff;
  }
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.reply-button {
  font-size: 12px;
  color: #606266;
  padding: 4px 8px;

  &:hover {
    color: #409eff;
    background-color: #ecf5ff;
  }
}

.comment-text {
  color: #606266;
  line-height: 1.7;
  word-break: break-word;
  font-size: 15px;
  margin-bottom: 12px;
}

// 回复列表
.replies-list {
  margin-top: 16px;
  padding-left: 16px;
  border-left: 3px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 10px;
  background: white;
  border-radius: 8px;
}

.reply-content {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.reply-author {
  font-weight: 600;
  color: #409eff;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.reply-text {
  color: #606266;
  flex: 1;
  min-width: 200px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
  margin-left: auto;
}

.empty-comments {
  text-align: center;
  padding: 60px 0;
}

// 响应式设计
@media (max-width: 768px) {
  .community-post-page {
    padding: 10px 0 20px;
  }

  .post-detail {
    padding: 20px;
    border-radius: 8px;
  }

  .post-top-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .post-author-info {
    width: 100%;
    justify-content: flex-end;
  }

  .post-title {
    font-size: 24px;
  }

  .post-images {
    grid-template-columns: 1fr;
  }

  .post-actions {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .action-stats {
    width: 100%;
    justify-content: flex-start;
  }

  .comment-item {
    padding: 12px;
  }

  .replies-list {
    padding-left: 8px;
  }
}
</style>
