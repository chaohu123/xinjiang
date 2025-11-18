<template>
  <div class="heritage-detail" v-loading="loading">
    <div class="hero" :style="{ backgroundImage: `url(${heritage?.cover || heritage?.images?.[0] || ''})` }">
      <div class="hero-overlay">
        <div class="container hero-content">
          <el-tag size="large" effect="dark">{{ heritage?.heritageLevel }}</el-tag>
          <h1>{{ heritage?.title }}</h1>
          <p>{{ heritage?.region }} · {{ heritage?.category }}</p>
          <div class="hero-tags">
            <el-tag v-for="tag in heritage?.tags" :key="tag" size="small">{{ tag }}</el-tag>
          </div>
        </div>
      </div>
    </div>

    <div class="container content">
      <el-row :gutter="24">
        <el-col :md="16" :sm="24">
          <section class="detail-card">
            <h2>文化背景</h2>
            <p>{{ heritage?.description }}</p>
            <div v-if="heritage?.content" v-html="heritage?.content" class="rich-content" />
          </section>

          <section v-if="heritage?.images?.length" class="detail-card">
            <h2>图像集</h2>
            <div class="gallery">
              <el-image
                v-for="img in heritage?.images"
                :key="img"
                :src="img"
                fit="cover"
                lazy
              />
            </div>
          </section>

          <section v-if="heritage?.videoUrl" class="detail-card">
            <h2>相关视频</h2>
            <video controls class="detail-video">
              <source :src="heritage?.videoUrl" />
            </video>
          </section>
        </el-col>
        <el-col :md="8" :sm="24">
          <section class="sidebar-card">
            <h3>项目信息</h3>
            <ul>
              <li>
                <span>所属区域</span>
                <strong>{{ heritage?.region }}</strong>
              </li>
              <li>
                <span>传统类别</span>
                <strong>{{ heritage?.category }}</strong>
              </li>
              <li>
                <span>保护级别</span>
                <strong>{{ heritage?.heritageLevel }}</strong>
              </li>
              <li>
                <span>浏览次数</span>
                <strong>{{ heritage?.views }}</strong>
              </li>
            </ul>
            <el-button type="primary" class="ai-btn" @click="jumpToAi">
              让AI讲解这个项目
            </el-button>
          </section>

          <section class="sidebar-card">
            <h3>相关推荐</h3>
            <ul class="recommend-list">
              <li
                v-for="item in recommendations"
                :key="item.id"
                @click="$router.push(`/heritage/${item.id}`)"
              >
                <el-image :src="item.cover || item.images?.[0]" fit="cover" />
                <div>
                  <h4>{{ item.title }}</h4>
                  <p>{{ item.region }} · {{ item.category }}</p>
                </div>
              </li>
            </ul>
          </section>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHeritageDetail, getHeritageRecommendations } from '@/api/heritage'
import type { HeritageItem } from '@/types/heritage'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const heritage = ref<HeritageItem | null>(null)
const recommendations = ref<HeritageItem[]>([])
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const id = Number(route.params.id)
    heritage.value = await getHeritageDetail(id)
    const rec = await getHeritageRecommendations(4)
    recommendations.value = rec.filter(item => item.id !== id)
  } catch (error) {
    ElMessage.error('加载非遗内容失败')
  } finally {
    loading.value = false
  }
}

const jumpToAi = () => {
  if (!heritage.value) return
  router.push({
    path: '/ai-explain',
    query: {
      q: heritage.value.title,
      context: heritage.value.description,
      image: heritage.value.images?.[0] || '',
    },
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.heritage-detail {
  min-height: calc(100vh - 70px);
}

.hero {
  height: 360px;
  background-size: cover;
  background-position: center;
  position: relative;
  margin-bottom: 24px;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(0, 0, 0, 0.6), rgba(0, 0, 0, 0.3));
  display: flex;
  align-items: flex-end;
}

.hero-content {
  color: #fff;
  padding-bottom: 40px;

  h1 {
    font-size: 36px;
    margin: 12px 0;
  }

  .hero-tags {
    display: flex;
    gap: 8px;
  }
}

.content {
  padding-bottom: 40px;
}

.detail-card,
.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);

  h2,
  h3 {
    margin-bottom: 16px;
  }
}

.gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;

  :deep(.el-image) {
    border-radius: 8px;
    height: 120px;
  }
}

.detail-video {
  width: 100%;
  border-radius: 12px;
}

.sidebar-card ul {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px dashed #ebeef5;

    &:last-child {
      border-bottom: none;
    }
  }
}

.ai-btn {
  width: 100%;
  margin-top: 16px;
}

.recommend-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;

  li {
    display: flex;
    gap: 12px;
    cursor: pointer;
    align-items: center;

    h4 {
      margin: 0 0 4px 0;
    }

    :deep(.el-image) {
      width: 60px;
      height: 60px;
      border-radius: 8px;
    }
  }
}

.rich-content :deep(p) {
  line-height: 1.8;
  margin-bottom: 12px;
  color: #303133;
}
</style>






