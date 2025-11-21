<template>
  <div class="ai-explain">
    <div class="container">
      <div class="page-header">
        <h1>AI 文化讲解助手</h1>
        <p>输入想了解的新疆文化元素，AI 将为你生成学术又生动的解读</p>
      </div>

      <el-row :gutter="24">
        <el-col :md="10" :sm="24">
          <el-card class="form-card">
            <el-form :model="form" label-position="top" @submit.prevent>
              <el-form-item label="讲解主题" required>
                <el-input
                  v-model="form.query"
                  placeholder="例如：刀郎木卡姆、艾提尕尔清真寺、非遗项目名称"
                />
              </el-form-item>
              <el-form-item label="补充背景">
                <el-input
                  v-model="form.context"
                  type="textarea"
                  :rows="4"
                  placeholder="想强调的细节、展品背景或学习目的"
                />
              </el-form-item>
              <el-form-item label="相关图片链接">
                <el-input
                  v-model="form.imageUrl"
                  placeholder="可选，上传到图床后的图片 URL"
                />
              </el-form-item>
              <el-form-item label="目标受众">
                <el-select v-model="form.audience" placeholder="选择目标访客">
                  <el-option
                    v-for="audience in audienceOptions"
                    :key="audience.value"
                    :label="audience.label"
                    :value="audience.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="讲解风格">
                <el-select v-model="form.tone" placeholder="选择语气">
                  <el-option
                    v-for="tone in toneOptions"
                    :key="tone.value"
                    :label="tone.label"
                    :value="tone.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="篇幅密度">
                <el-radio-group v-model="form.length">
                  <el-radio-button v-for="option in lengthOptions" :key="option.value" :label="option.value">
                    {{ option.label }}
                  </el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="聚焦要点">
                <el-checkbox-group v-model="form.focusPoints">
                  <el-checkbox v-for="focus in focusOptions" :key="focus.value" :value="focus.value">
                    {{ focus.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-button
                type="primary"
                :loading="loading"
                :disabled="!form.query"
                @click="handleExplain"
              >
                生成讲解
              </el-button>
            </el-form>
          </el-card>

          <el-card class="quick-card" shadow="never">
            <div class="card-title">快捷示例</div>
            <div class="quick-topics">
              <el-tag
                v-for="topic in quickTopics"
                :key="topic.text"
                class="quick-tag"
                type="info"
                @click="applyQuickTopic(topic)"
              >
                {{ topic.text }}
              </el-tag>
            </div>
            <p class="quick-note">点击快速填充主题和推荐设定，帮助你更快体验讲解效果。</p>
          </el-card>
        </el-col>

        <el-col :md="14" :sm="24">
          <el-card v-if="result" class="result-card">
            <template #header>
              <div class="result-header">
                <div>
                  <h2>{{ result.title }}</h2>
                  <p class="result-meta">
                    {{ form.audience || '泛人群' }} · {{ toneLabel(form.tone) }} · {{ lengthLabel(form.length) }}
                  </p>
                </div>
                <el-button-group>
                  <el-button size="small" @click="copyResult">复制</el-button>
                  <el-button size="small" @click="downloadResult">下载</el-button>
                </el-button-group>
              </div>
            </template>
            <p class="summary">{{ result.summary }}</p>
            <h3>讲解要点</h3>
            <ul>
              <li v-for="(item, index) in result.highlights" :key="index">
                {{ item }}
              </li>
            </ul>
            <h3 v-if="result.mediaInsight">图像洞察</h3>
            <p>{{ result.mediaInsight }}</p>
            <h3>参考资料</h3>
            <ol>
              <li v-for="(refItem, index) in result.references" :key="index">
                {{ refItem }}
              </li>
            </ol>
          </el-card>
          <EmptyState v-else text="提交主题后，AI 会在这里给出讲解内容" />

          <el-card class="history-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>讲解历史</span>
                <div>
                  <el-button v-if="history.length" text size="small" @click="clearHistory">清空</el-button>
                </div>
              </div>
            </template>
            <div v-if="history.length">
              <el-timeline>
                <el-timeline-item
                  v-for="entry in history"
                  :key="entry.id"
                  :timestamp="formatTimestamp(entry.createdAt)"
                >
                  <div class="history-item">
                    <div>
                      <strong>{{ entry.query }}</strong>
                      <p class="history-meta">
                        {{ entry.audience || '泛人群' }} · {{ toneLabel(entry.tone) }}
                      </p>
                    </div>
                    <div class="history-actions">
                      <el-button text size="small" @click="reuseHistory(entry)">复用设定</el-button>
                      <el-button text size="small" type="primary" @click="viewHistoryResult(entry)">查看讲解</el-button>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
            <EmptyState v-else text="暂无讲解记录，生成后可快速回看与复用" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { explainCulture } from '@/api/ai'
import type { AiExplainPayload, AiExplainResult } from '@/types/ai'
import EmptyState from '@/components/common/EmptyState.vue'
import { ElMessage } from 'element-plus'

interface ExplanationHistoryEntry extends AiExplainPayload {
  id: string
  createdAt: number
  result: AiExplainResult
}

const HISTORY_KEY = 'ai-explain-history'

const route = useRoute()

const audienceOptions = [
  { label: '大众访客', value: '大众访客' },
  { label: '青少年研学团', value: '青少年研学团' },
  { label: '文博研究者', value: '文博研究者' },
  { label: '国际友人导览', value: '国际友人导览' },
]

const toneOptions = [
  { label: '专业讲解', value: '专业讲解' },
  { label: '故事化叙述', value: '故事化叙述' },
  { label: '互动提问式', value: '互动提问式' },
  { label: '导览词口吻', value: '导览词口吻' },
]

const lengthOptions = [
  { label: '浓缩', value: 'short' },
  { label: '标准', value: 'medium' },
  { label: '详尽', value: 'detailed' },
]

const focusOptions = [
  { label: '历史脉络', value: '历史脉络' },
  { label: '艺术技艺', value: '艺术技艺' },
  { label: '民族故事', value: '民族故事' },
  { label: '现代传承', value: '现代传承' },
  { label: '观展互动', value: '观展互动' },
]

const quickTopics = [
  { text: '莫高窟第220窟壁画', tone: '专业讲解', audience: '文博研究者', focus: ['历史脉络', '艺术技艺'], length: 'detailed' },
  { text: '坎儿井水利系统', tone: '故事化叙述', audience: '大众访客', focus: ['历史脉络', '现代传承'], length: 'medium' },
  { text: '艾德莱斯绸织染技艺', tone: '导览词口吻', audience: '青少年研学团', focus: ['艺术技艺', '观展互动'], length: 'short' },
]

const form = reactive<AiExplainPayload>({
  query: '',
  imageUrl: '',
  context: '',
  audience: audienceOptions[0].value,
  tone: toneOptions[0].value,
  length: 'medium',
  focusPoints: [],
})

const loading = ref(false)
const result = ref<AiExplainResult | null>(null)
const history = ref<ExplanationHistoryEntry[]>([])

const handleExplain = async () => {
  if (!form.query) return
  loading.value = true
  try {
    const payload: AiExplainPayload = {
      query: form.query,
      context: form.context || undefined,
      imageUrl: form.imageUrl || undefined,
      audience: form.audience || undefined,
      tone: form.tone || undefined,
      length: form.length,
      focusPoints: form.focusPoints && form.focusPoints.length ? [...form.focusPoints] : undefined,
    }
    const response = await explainCulture(payload)
    result.value = response
    ElMessage.success('讲解生成完成')
    saveHistoryEntry({ ...payload, focusPoints: payload.focusPoints || [], result: response })
  } catch (error) {
    ElMessage.error('AI 讲解失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const saveHistoryEntry = (entry: Omit<ExplanationHistoryEntry, 'id' | 'createdAt'>) => {
  const withMeta: ExplanationHistoryEntry = {
    id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    createdAt: Date.now(),
    ...entry,
  }
  history.value = [withMeta, ...history.value].slice(0, 8)
  persistHistory()
}

const loadHistory = () => {
  if (typeof window === 'undefined') return
  try {
    const stored = window.localStorage.getItem(HISTORY_KEY)
    if (stored) {
      const parsed: ExplanationHistoryEntry[] = JSON.parse(stored)
      history.value = parsed
    }
  } catch (error) {
    console.warn('讲解历史读取失败', error)
  }
}

const persistHistory = () => {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(
    HISTORY_KEY,
    JSON.stringify(
      history.value.map(({ id, createdAt, query, context, imageUrl, audience, tone, length, focusPoints, result }) => ({
        id,
        createdAt,
        query,
        context,
        imageUrl,
        audience,
        tone,
        length,
        focusPoints,
        result,
      })),
    ),
  )
}

const clearHistory = () => {
  history.value = []
  if (typeof window !== 'undefined') {
    window.localStorage.removeItem(HISTORY_KEY)
  }
  ElMessage.success('历史记录已清空')
}

const reuseHistory = (entry: ExplanationHistoryEntry) => {
  form.query = entry.query
  form.context = entry.context || ''
  form.imageUrl = entry.imageUrl || ''
  form.audience = entry.audience || ''
  form.tone = entry.tone || ''
  form.length = entry.length || 'medium'
  form.focusPoints = entry.focusPoints ? [...entry.focusPoints] : []
  ElMessage.success('已复用历史设定，可直接生成')
}

const viewHistoryResult = (entry: ExplanationHistoryEntry) => {
  result.value = entry.result
}

const applyQuickTopic = (topic: (typeof quickTopics)[number]) => {
  form.query = topic.text
  form.tone = topic.tone
  form.audience = topic.audience
  form.length = topic.length as AiExplainPayload['length']
  form.focusPoints = topic.focus
  ElMessage.success('已应用示例设定')
}

const copyResult = async () => {
  if (!result.value) return
  const text = [
    `【标题】${result.value.title}`,
    '',
    `【概述】${result.value.summary}`,
    '',
    '【讲解要点】',
    ...result.value.highlights.map((item, index) => `${index + 1}. ${item}`),
    '',
    result.value.mediaInsight ? `【图像洞察】${result.value.mediaInsight}` : '',
    '【参考资料】',
    ...result.value.references.map((item, index) => `${index + 1}. ${item}`),
  ]
    .filter(Boolean)
    .join('\n')
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('讲解内容已复制')
  } catch (error) {
    ElMessage.error('复制失败，请手动选择文本')
  }
}

const downloadResult = () => {
  if (!result.value) return
  const blob = new Blob([JSON.stringify(result.value, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${result.value.title || 'ai-explain'}.json`
  link.click()
  URL.revokeObjectURL(url)
}

const formatTimestamp = (timestamp: number) => {
  return new Date(timestamp).toLocaleString()
}

const toneLabel = (tone?: string) => tone || '默认风格'
const lengthLabel = (length?: AiExplainPayload['length']) => {
  switch (length) {
    case 'short':
      return '浓缩'
    case 'detailed':
      return '详尽'
    default:
      return '标准'
  }
}

onMounted(() => {
  if (route.query.q) {
    form.query = route.query.q as string
  }
  if (route.query.context) {
    form.context = route.query.context as string
  }
  if (route.query.image) {
    form.imageUrl = route.query.image as string
  }
  loadHistory()
})
</script>

<style scoped lang="scss">
.ai-explain {
  padding: 40px 0;
  min-height: calc(100vh - 70px);
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-size: 32px;
    margin-bottom: 8px;
  }
}

.form-card {
  margin-bottom: 16px;
}

.summary {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

ul,
ol {
  margin: 12px 0 0 16px;
  color: #303133;
  line-height: 1.6;
}

li + li {
  margin-top: 8px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.result-meta {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}

.quick-card,
.history-card {
  margin-top: 16px;
}

.card-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.quick-topics {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
}

.quick-note {
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.history-meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.history-actions {
  display: flex;
  gap: 8px;
}
</style>





