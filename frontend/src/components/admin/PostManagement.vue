<template>
  <div class="post-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>社区投稿列表</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索投稿标题"
              style="width: 300px; margin-right: 12px"
              clearable
              @keyup.enter="loadPosts"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="filterStatus"
              placeholder="状态"
              style="width: 150px; margin-right: 12px"
              clearable
            >
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-button type="primary" @click="loadPosts"> 搜索 </el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="posts" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="author.username" label="作者" width="150" />
        <el-table-column label="内容预览" min-width="200">
          <template #default="{ row }">
            <div class="content-preview">{{ row.content.substring(0, 50) }}...</div>
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞" width="80" />
        <el-table-column prop="comments" label="评论" width="80" />
        <el-table-column prop="views" label="浏览" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status || 'pending')">
              {{ getStatusLabel(row.status || 'pending') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="拒绝原因" min-width="200">
          <template #default="{ row }">
            <span v-if="row.rejectReason" style="color: #f56c6c">{{ row.rejectReason }}</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)"> 查看 </el-button>
            <el-button
              v-if="!row.status || row.status === 'pending'"
              link
              type="success"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="!row.status || row.status === 'pending'"
              link
              type="warning"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)"> 删除 </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadPosts"
          @current-change="loadPosts"
        />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="detailVisible" title="投稿详情" width="800px">
      <div v-if="currentPost" class="post-detail">
        <h3>{{ currentPost.title }}</h3>
        <div class="post-meta">
          <span>作者：{{ currentPost.author?.username }}</span>
          <span>发布时间：{{ formatDate(currentPost.createdAt) }}</span>
          <span>点赞：{{ currentPost.likes }}</span>
          <span>评论：{{ currentPost.comments }}</span>
          <span>浏览：{{ currentPost.views }}</span>
          <span>
            状态：
            <el-tag :type="getStatusType(currentPost.status || 'pending')" size="small">
              {{ getStatusLabel(currentPost.status || 'pending') }}
            </el-tag>
          </span>
        </div>
        <div v-if="currentPost.rejectReason" class="reject-reason" style="margin-top: 16px; padding: 12px; background: #fef0f0; border-radius: 4px; border-left: 4px solid #f56c6c">
          <strong style="color: #f56c6c">拒绝原因：</strong>
          <span style="color: #606266">{{ currentPost.rejectReason }}</span>
        </div>
        <div class="post-content">
          {{ currentPost.content }}
        </div>
        <div v-if="currentPost.images && currentPost.images.length > 0" class="post-images">
          <el-image
            v-for="(img, index) in currentPost.images"
            :key="index"
            :src="img"
            fit="cover"
            style="width: 200px; height: 150px; margin-right: 10px; margin-top: 10px"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectVisible" title="拒绝投稿" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因" :rules="[{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，以便用户了解投稿未通过审核的原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false"> 取消 </el-button>
        <el-button type="primary" @click="handleRejectConfirm"> 确定 </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAdminPosts, approvePost, rejectPost, deletePost } from '@/api/admin'
import type { CommunityPost } from '@/types/community'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { formatDate } from '@/utils'

const loading = ref(false)
const posts = ref<CommunityPost[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const filterStatus = ref('')
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentPost = ref<CommunityPost | null>(null)
const rejectForm = ref({ reason: '' })
const rejectingPostId = ref<number | null>(null)

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
  }
  return labels[status] || status
}

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
  }
  return types[status] || ''
}

const loadPosts = async () => {
  loading.value = true
  try {
    const response = await getAdminPosts({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value || undefined,
    })
    posts.value = response.list || []
    total.value = response.total || 0
  } catch (error: any) {
    console.error('Failed to load posts:', error)
    // 如果是404或500错误，说明后端API还未实现
    if (error?.response?.status === 404 || error?.response?.status === 500) {
      ElMessage.warning('管理员API功能尚未实现，请等待后端开发完成')
      posts.value = []
      total.value = 0
    } else {
      ElMessage.error('加载投稿列表失败：' + (error?.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const handleView = (post: CommunityPost) => {
  currentPost.value = post
  detailVisible.value = true
}

const handleApprove = async (post: CommunityPost) => {
  try {
    await ElMessageBox.confirm(`确定要通过投稿 "${post.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await approvePost(post.id)
    ElMessage.success('审核通过')
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to approve post:', error)
      ElMessage.error('操作失败')
    }
  }
}

const handleReject = (post: CommunityPost) => {
  rejectingPostId.value = post.id
  rejectForm.value.reason = ''
  rejectVisible.value = true
}

const handleRejectConfirm = async () => {
  if (!rejectingPostId.value) return

  if (!rejectForm.value.reason || !rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    await rejectPost(rejectingPostId.value, rejectForm.value.reason.trim())
    ElMessage.success('已拒绝')
    rejectVisible.value = false
    rejectForm.value.reason = ''
    loadPosts()
  } catch (error) {
    console.error('Failed to reject post:', error)
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (post: CommunityPost) => {
  try {
    await ElMessageBox.confirm(`确定要删除投稿 "${post.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deletePost(post.id)
    ElMessage.success('删除成功')
    loadPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete post:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-preview {
  color: #606266;
  font-size: 14px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.post-detail {
  h3 {
    font-size: 20px;
    margin-bottom: 16px;
    color: #303133;
  }

  .post-meta {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    color: #909399;
    font-size: 14px;
  }

  .post-content {
    line-height: 1.8;
    color: #606266;
    margin-bottom: 20px;
    white-space: pre-wrap;
  }

  .post-images {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>
