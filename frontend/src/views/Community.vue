<template>
  <div class="community-page">
    <div class="container">
      <div class="page-header">
        <h1>{{ $t('community.title') }}</h1>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          {{ $t('community.createPost') }}
        </el-button>
      </div>

      <div class="community-tabs">
        <el-radio-group v-model="sortBy" @change="loadPosts">
          <el-radio-button value="latest">
            {{ $t('community.latest') }}
          </el-radio-button>
          <el-radio-button value="hot">
            {{ $t('community.hot') }}
          </el-radio-button>
        </el-radio-group>
      </div>

      <div v-loading="loading" class="posts-list">
        <el-card v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-header">
            <el-avatar :src="post.author.avatar" :size="40">
              {{ post.author.username[0] }}
            </el-avatar>
            <div class="post-author">
              <div class="author-name">
                {{ post.author.username }}
              </div>
              <div class="post-time">
                {{ fromNow(post.createdAt) }}
              </div>
            </div>
          </div>
          <h3 class="post-title" @click="$router.push(`/community/post/${post.id}`)">
            {{ post.title }}
          </h3>
          <p class="post-content">
            {{ post.content }}
          </p>
          <div v-if="post.images && post.images.length > 0" class="post-images">
            <el-image
              v-for="(img, index) in post.images.slice(0, 3)"
              :key="index"
              :src="img"
              fit="cover"
              class="post-image"
            />
          </div>
          <div class="post-footer">
            <div class="post-tags">
              <el-tag v-for="tag in post.tags" :key="tag" size="small" style="margin-right: 8px">
                {{ tag }}
              </el-tag>
            </div>
            <div class="post-actions">
              <el-button text @click="handleLike(post)">
                <el-icon><Star /></el-icon>
                {{ post.likes }}
              </el-button>
              <el-button text>
                <el-icon><ChatLineRound /></el-icon>
                {{ post.comments }}
              </el-button>
              <el-button text>
                <el-icon><Share /></el-icon>
                {{ $t('community.share') }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <EmptyState v-if="!loading && posts.length === 0" :text="$t('common.noData')" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        class="pagination"
        @current-change="handlePageChange"
      />

      <el-dialog v-model="showCreateDialog" :title="$t('community.createPost')" width="800px">
        <el-form :model="createForm" label-width="80px">
          <el-form-item label="标题">
            <el-input v-model="createForm.title" :placeholder="$t('community.titlePlaceholder')" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input
              v-model="createForm.content"
              type="textarea"
              :rows="6"
              :placeholder="$t('community.contentPlaceholder')"
            />
          </el-form-item>
          <el-form-item label="标签">
            <el-input
              v-model="tagInput"
              placeholder="输入标签，用逗号分隔"
              @keyup.enter="addTags"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">
            {{ $t('common.cancel') }}
          </el-button>
          <el-button type="primary" @click="handleCreate">
            {{ $t('common.submit') }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCommunityPosts, createPost, likePost, unlikePost } from '@/api/community'
import type { CommunityPost } from '@/types/community'
import EmptyState from '@/components/common/EmptyState.vue'
import { fromNow } from '@/utils'
import { Plus, Star, ChatLineRound, Share } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const posts = ref<CommunityPost[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const sortBy = ref('latest')
const showCreateDialog = ref(false)
const createForm = ref({
  title: '',
  content: '',
  tags: [] as string[],
})
const tagInput = ref('')

const addTags = () => {
  if (tagInput.value.trim()) {
    const tags = tagInput.value
      .split(',')
      .map(t => t.trim())
      .filter(t => t)
    createForm.value.tags = [...new Set([...createForm.value.tags, ...tags])]
    tagInput.value = ''
  }
}

const loadPosts = async () => {
  loading.value = true
  try {
    const response = await getCommunityPosts({
      page: currentPage.value,
      size: pageSize.value,
      sort: sortBy.value as 'latest' | 'hot',
    })
    posts.value = response.list || []
    total.value = response.total || 0
  } catch (error) {
    console.error('Failed to load posts:', error)
    posts.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  loadPosts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleLike = async (post: CommunityPost) => {
  try {
    if (post.isLiked) {
      await unlikePost(post.id)
      post.likes--
      post.isLiked = false
    } else {
      await likePost(post.id)
      post.likes++
      post.isLiked = true
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCreate = async () => {
  try {
    await createPost(createForm.value)
    ElMessage.success('发布成功')
    showCreateDialog.value = false
    createForm.value = { title: '', content: '', tags: [] }
    loadPosts()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style lang="scss" scoped>
.community-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;

  h1 {
    font-size: 32px;
    color: #303133;
  }
}

.community-tabs {
  margin-bottom: 24px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 30px;
}

.post-card {
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-2px);
  }
}

.post-header {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.post-author {
  flex: 1;
}

.author-name {
  font-weight: 500;
  color: #303133;
}

.post-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.post-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
  cursor: pointer;
  transition: color 0.3s;

  &:hover {
    color: #409eff;
  }
}

.post-content {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.post-images {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.post-image {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.post-tags {
  flex: 1;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
</style>
