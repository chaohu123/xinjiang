<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.users }}
              </div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon culture-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.culture }}
              </div>
              <div class="stat-label">文化资源</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon event-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.events }}
              </div>
              <div class="stat-label">活动总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon post-icon">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                {{ stats.posts }}
              </div>
              <div class="stat-label">社区投稿</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/users')">
              <el-icon><User /></el-icon>
              用户管理
            </el-button>
            <el-button type="primary" @click="$router.push('/admin/carousels')">
              <el-icon><Picture /></el-icon>
              轮播图管理
            </el-button>
            <el-button type="primary" @click="$router.push('/admin/culture')">
              <el-icon><Document /></el-icon>
              文化资源管理
            </el-button>
            <el-button type="primary" @click="$router.push('/admin/events')">
              <el-icon><Calendar /></el-icon>
              活动管理
            </el-button>
            <el-button type="primary" @click="$router.push('/admin/posts')">
              <el-icon><ChatLineRound /></el-icon>
              社区投稿审核
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 左侧：待审核的社区投稿 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待审核的社区投稿</span>
              <el-button link type="primary" @click="$router.push('/admin/posts?status=pending')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="pendingPostsLoading" class="dashboard-list">
            <div v-if="pendingPosts.length === 0" class="empty-state">
              <el-empty description="暂无待审核的投稿" :image-size="80" />
            </div>
            <div
              v-for="post in pendingPosts"
              :key="post.id"
              class="dashboard-item"
              @click="$router.push(`/admin/posts`)"
            >
              <div class="item-content">
                <div class="item-title">{{ post.title }}</div>
                <div class="item-meta">
                  <span>作者：{{ post.author?.username }}</span>
                  <span>{{ formatDate(post.createdAt) }}</span>
                </div>
              </div>
              <el-tag type="warning" size="small">待审核</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：正在进行的活动 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>正在进行的活动</span>
              <el-button link type="primary" @click="$router.push('/admin/events?status=ongoing')">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <div v-loading="ongoingEventsLoading" class="dashboard-list">
            <div v-if="ongoingEvents.length === 0" class="empty-state">
              <el-empty description="暂无正在进行的活动" :image-size="80" />
            </div>
            <div
              v-for="event in ongoingEvents"
              :key="event.id"
              class="dashboard-item"
              @click="$router.push(`/admin/events`)"
            >
              <div class="item-content">
                <div class="item-title">{{ event.title }}</div>
                <div class="item-meta">
                  <span>{{ event.startDate }} - {{ event.endDate }}</span>
                  <span v-if="event.capacity">
                    已报名：{{ event.registered }}/{{ event.capacity }}
                  </span>
                </div>
              </div>
              <el-tag type="success" size="small">进行中</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, Document, Calendar, ChatLineRound, Picture, ArrowRight } from '@element-plus/icons-vue'
import { getDashboardStats, getPendingPosts, getOngoingEvents } from '@/api/admin'
import type { CommunityPost } from '@/types/community'
import type { Event } from '@/types/event'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils'

const stats = ref({
  users: 0,
  culture: 0,
  events: 0,
  posts: 0,
})

const pendingPosts = ref<CommunityPost[]>([])
const ongoingEvents = ref<Event[]>([])
const pendingPostsLoading = ref(false)
const ongoingEventsLoading = ref(false)

const loadDashboardData = async () => {
  try {
    // 加载统计数据
    const statsData = await getDashboardStats()
    stats.value = statsData

    // 加载待审核投稿
    pendingPostsLoading.value = true
    try {
      const posts = await getPendingPosts(5)
      pendingPosts.value = posts
    } catch (error) {
      console.error('Failed to load pending posts:', error)
      ElMessage.error('加载待审核投稿失败')
    } finally {
      pendingPostsLoading.value = false
    }

    // 加载正在进行的活动
    ongoingEventsLoading.value = true
    try {
      const events = await getOngoingEvents(5)
      ongoingEvents.value = events
    } catch (error) {
      console.error('Failed to load ongoing events:', error)
      ElMessage.error('加载正在进行的活动失败')
    } finally {
      ongoingEventsLoading.value = false
    }
  } catch (error) {
    console.error('Failed to load dashboard stats:', error)
    ElMessage.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;

        &.user-icon {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.culture-icon {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        &.event-icon {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }

        &.post-icon {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 32px;
          font-weight: 600;
          color: #303133;
          line-height: 1;
          margin-bottom: 8px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }

  .quick-actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;

    .el-button {
      padding: 12px 24px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .dashboard-list {
    min-height: 200px;
  }

  .empty-state {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;
  }

  .dashboard-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    transition: background-color 0.3s;

    &:hover {
      background-color: #f5f7fa;
      padding-left: 8px;
      padding-right: 8px;
      margin-left: -8px;
      margin-right: -8px;
      border-radius: 4px;
    }

    &:last-child {
      border-bottom: none;
    }

    .item-content {
      flex: 1;
      min-width: 0;

      .item-title {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 6px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-meta {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #909399;

        span {
          white-space: nowrap;
        }
      }
    }
  }
}
</style>
