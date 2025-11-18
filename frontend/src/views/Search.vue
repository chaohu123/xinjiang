<template>
  <div class="search-page">
    <div class="container">
      <div class="search-header">
        <h1>{{ $t('search.title') }}</h1>
        <el-input
          v-model="searchKeyword"
          :placeholder="$t('search.placeholder')"
          size="large"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #suffix>
            <el-button type="primary" @click="handleSearch">
              {{ $t('common.search') }}
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="search-content">
        <aside class="filters-sidebar">
          <h3>{{ $t('search.filters') }}</h3>
          <el-card>
            <div class="filter-group">
              <label>{{ $t('search.type') }}</label>
              <el-checkbox-group v-model="filters.type">
                <el-checkbox v-for="type in CULTURE_TYPES" :key="type.value" :value="type.value">
                  {{ type.label }}
                </el-checkbox>
              </el-checkbox-group>
            </div>

            <div class="filter-group">
              <label>{{ $t('search.region') }}</label>
              <el-select v-model="filters.region" :placeholder="$t('search.region')" clearable>
                <el-option
                  v-for="region in REGIONS"
                  :key="region"
                  :label="region"
                  :value="region"
                />
              </el-select>
            </div>

            <div class="filter-group">
              <label>{{ $t('search.tags') }}</label>
              <el-input
                v-model="tagInput"
                :placeholder="'输入标签，用逗号分隔'"
                @keyup.enter="addTags"
              />
            </div>

            <el-button type="primary" style="width: 100%" @click="handleSearch">
              {{ $t('common.search') }}
            </el-button>
            <el-button style="width: 100%; margin-top: 10px" @click="resetFilters">
              {{ $t('common.cancel') }}
            </el-button>
          </el-card>
        </aside>

        <main class="results-main">
          <div class="results-header">
            <span class="results-count">
              {{ $t('search.results') }}: {{ total }} {{ $t('common.more') }}
            </span>
            <el-select v-model="sortBy" style="width: 150px">
              <el-option label="最新" value="latest" />
              <el-option label="最热" value="hot" />
              <el-option label="收藏最多" value="favorites" />
            </el-select>
          </div>

          <div v-loading="loading" class="results-grid">
            <CultureCard v-for="item in results" :key="item.id" :resource="item" />
          </div>

          <EmptyState v-if="!loading && results.length === 0" :text="$t('search.noResults')" />

        <section v-if="heritageHighlights.length" class="heritage-highlight">
          <div class="section-header">
            <h3>非遗内容推荐</h3>
          </div>
          <div class="results-grid">
            <CultureCard v-for="item in heritageHighlights" :key="`h-${item.id}`" :resource="item" />
          </div>
        </section>

          <el-pagination
            v-if="total > 0"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="prev, pager, next, jumper"
            class="pagination"
            @current-change="handlePageChange"
          />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchResources } from '@/api/culture'
import type { CultureResource } from '@/types/culture'
import CultureCard from '@/components/common/CultureCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { CULTURE_TYPES, REGIONS } from '@/utils/constants'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()

const searchKeyword = ref('')
const filters = ref({
  type: [] as string[],
  region: '',
  tags: [] as string[],
})
const tagInput = ref('')
const sortBy = ref('latest')
const results = ref<CultureResource[]>([])
const heritageHighlights = ref<CultureResource[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const addTags = () => {
  if (tagInput.value.trim()) {
    const tags = tagInput.value
      .split(',')
      .map(t => t.trim())
      .filter(t => t)
    filters.value.tags = [...new Set([...filters.value.tags, ...tags])]
    tagInput.value = ''
  }
}

const resetFilters = () => {
  filters.value = {
    type: [],
    region: '',
    tags: [],
  }
  tagInput.value = ''
  searchKeyword.value = ''
  handleSearch()
}

const handleSearch = async () => {
  loading.value = true
  try {
    const response = await searchResources({
      keyword: searchKeyword.value || undefined,
      type: filters.value.type.length > 0 ? (filters.value.type[0] as any) : undefined,
      tags: filters.value.tags.length > 0 ? filters.value.tags : undefined,
      region: filters.value.region || undefined,
      page: currentPage.value,
      size: pageSize.value,
    })
    results.value = response.list
    total.value = response.total
    heritageHighlights.value =
      filters.value.type.includes('heritage') || response.extra?.heritageOnly
        ? []
        : ((response.extra?.heritage as CultureResource[]) || [])
  } catch (error) {
    console.error('Search failed:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  handleSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  if (route.query.q) {
    searchKeyword.value = route.query.q as string
  }
  handleSearch()
})

watch(
  () => route.query.q,
  newQ => {
    if (newQ) {
      searchKeyword.value = newQ as string
      handleSearch()
    }
  }
)
</script>

<style lang="scss" scoped>
.search-page {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.search-header {
  margin-bottom: 30px;
  text-align: center;

  h1 {
    font-size: 32px;
    margin-bottom: 20px;
    color: #303133;
  }
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
}

.search-content {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 24px;
  margin-top: 30px;
}

.filters-sidebar {
  h3 {
    font-size: 18px;
    margin-bottom: 16px;
    color: #303133;
  }
}

.filter-group {
  margin-bottom: 20px;

  label {
    display: block;
    margin-bottom: 8px;
    font-weight: 500;
    color: #606266;
  }
}

.results-main {
  min-height: 400px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.results-count {
  color: #606266;
  font-size: 14px;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

.heritage-highlight {
  margin-top: 40px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      font-size: 22px;
      color: #303133;
    }
  }
}

@media (max-width: 768px) {
  .search-content {
    grid-template-columns: 1fr;
  }

  .filters-sidebar {
    order: 2;
  }

  .results-main {
    order: 1;
  }
}
</style>
