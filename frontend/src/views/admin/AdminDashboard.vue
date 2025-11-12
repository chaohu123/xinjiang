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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, Document, Calendar, ChatLineRound, Picture } from '@element-plus/icons-vue'

const stats = ref({
  users: 0,
  culture: 0,
  events: 0,
  posts: 0,
})

onMounted(() => {
  // TODO: 加载统计数据
  // 这里可以调用 API 获取统计数据
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
}
</style>
