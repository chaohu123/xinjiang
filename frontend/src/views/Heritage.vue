<template>
  <div class="heritage-page">
    <div class="container">
      <div class="page-header">
        <div>
          <h1>非遗专题</h1>
          <p>探索新疆各地非物质文化遗产故事</p>
        </div>
        <el-button type="primary" @click="$router.push('/ai-explain')">
          体验AI讲解助手
        </el-button>
      </div>

      <el-card class="filters-card">
        <div class="filters-grid">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索非遗项目"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filters.region" placeholder="地区" clearable>
            <el-option v-for="region in REGIONS" :key="region" :value="region" :label="region" />
          </el-select>
          <el-select v-model="filters.category" placeholder="类别" clearable>
            <el-option
              v-for="category in HERITAGE_CATEGORIES"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
          <el-select v-model="filters.level" placeholder="级别" clearable>
            <el-option
              v-for="level in HERITAGE_LEVELS"
              :key="level.value"
              :label="level.label"
              :value="level.value"
            />
          </el-select>
          <el-button type="primary" @click="handleSearch">筛选</el-button>
        </div>
      </el-card>

      <div v-loading="loading" class="heritage-grid">
        <el-card
          v-for="item in heritageList"
          :key="item.id"
          class="heritage-card"
          @click="$router.push(`/heritage/${item.id}`)"
        >
          <el-image :src="item.cover || item.images?.[0]" fit="cover" class="heritage-image" />
          <div class="heritage-body">
            <div class="heritage-meta">
              <el-tag size="small" type="warning">{{ item.heritageLevel }}</el-tag>
              <span>{{ item.region }} · {{ item.category }}</span>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.description }}</p>
            <div class="heritage-tags">
              <el-tag v-for="tag in item.tags.slice(0, 3)" :key="tag" size="small">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <EmptyState v-if="!loading && heritageList.length === 0" text="暂无非遗资源" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        class="pagination"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getHeritageList } from '@/api/heritage'
import type { HeritageItem } from '@/types/heritage'
import EmptyState from '@/components/common/EmptyState.vue'
import { HERITAGE_CATEGORIES, HERITAGE_LEVELS, REGIONS } from '@/utils/constants'
import { Search } from '@element-plus/icons-vue'

const heritageList = ref<HeritageItem[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(9)

const filters = reactive({
  keyword: '',
  region: '',
  category: '',
  level: '',
})

const handleSearch = async () => {
  loading.value = true
  try {
    const response = await getHeritageList({
      keyword: filters.keyword || undefined,
      region: filters.region || undefined,
      category: filters.category || undefined,
      heritageLevel: filters.level || undefined,
      page: currentPage.value,
      size: pageSize.value,
    })
    heritageList.value = response.list || []
    total.value = response.total || 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  handleSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
.heritage-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 32px;
    margin-bottom: 8px;
  }
}

.filters-card {
  margin-bottom: 24px;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.heritage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.heritage-card {
  cursor: pointer;
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-4px);
  }
}

.heritage-image {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.heritage-body {
  h3 {
    font-size: 20px;
    margin: 8px 0;
  }

  p {
    color: #606266;
    line-height: 1.5;
    min-height: 48px;
  }
}

.heritage-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #909399;
}

.heritage-tags {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>





