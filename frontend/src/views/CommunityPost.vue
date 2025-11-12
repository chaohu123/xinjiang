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
              <el-icon><Star /></el-icon>
              {{ postDetail.likes }}
            </el-button>
            <el-button>
              <el-icon><Share /></el-icon>
              {{ $t('community.share') }}
            </el-button>
          </div>
        </div>

        <div v-if="postDetail" class="comments-section">
          <h2>评论 ({{ postDetail.comments.length }})</h2>
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
            <div v-for="comment in postDetail.comments" :key="comment.id" class="comment-item">
              <el-avatar :src="comment.author.avatar" :size="36">
                {{ comment.author.username[0] }}
              </el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.author.username }}</span>
                  <span class="comment-time">{{ fromNow(comment.createdAt) }}</span>
                </div>
                <p class="comment-text">
                  {{ comment.content }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPostDetail, likePost, unlikePost, commentPost } from '@/api/community'
import type { CommunityPostDetail } from '@/types/community'
import { fromNow } from '@/utils'
import { Star, Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const postDetail = ref<CommunityPostDetail | null>(null)
const loading = ref(false)
const commentContent = ref('')

const loadDetail = async () => {
  const id = parseInt(route.params.id as string)
  loading.value = true
  try {
    const data = await getPostDetail(id)
    postDetail.value = data
  } catch (error) {
    console.error('Failed to load post detail:', error)
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
</style>
