<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never" header="政策智能问答">
          <div class="chat">
            <div v-for="(message, index) in messages" :key="index" :class="['bubble', message.role]">
              <div class="bubble-text">{{ message.content }}</div>
              <div v-if="message.citations?.length" class="citations">
                <div class="citations-title">政策来源</div>
                <div v-for="citation in message.citations" :key="citation.index" class="citation">
                  [{{ citation.index }}] {{ citation.title }} —— {{ citation.publishOrg }}
                </div>
              </div>
            </div>
          </div>
          <div class="ask">
            <el-input
              v-model="question"
              placeholder="例如：应届毕业生到基层就业有补贴吗？"
              @keyup.enter="ask"
            />
            <el-button type="primary" :loading="asking" @click="ask">提问</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" header="政策库">
          <el-input v-model="keyword" placeholder="搜索政策标题 / 标签" clearable @change="loadPolicies(1)" />
          <el-table :data="policies" v-loading="loading" height="420" class="policy-table">
            <el-table-column prop="title" label="政策标题" min-width="200" />
            <el-table-column prop="category" label="分类" width="110" />
            <el-table-column prop="publishOrg" label="发布单位" width="150" />
          </el-table>
          <el-pagination
            layout="total, prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="pageNum"
            @current-change="loadPolicies"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { policyApi } from '@/api'
import type { PolicyItem } from '@/types/api'

interface Citation {
  index: number
  title: string
  publishOrg: string
}

interface Message {
  role: 'user' | 'assistant'
  content: string
  citations?: Citation[]
}

const question = ref('')
const asking = ref(false)
const messages = ref<Message[]>([
  { role: 'assistant', content: '你好，我是就业政策智能助手。可以问我补贴、落户、档案、基层就业等问题，我会引用政策原文作答。' }
])

const policies = ref<PolicyItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const keyword = ref('')

async function ask() {
  if (!question.value.trim()) return
  const text = question.value.trim()
  messages.value.push({ role: 'user', content: text })
  question.value = ''
  asking.value = true
  try {
    const result = await policyApi.ask(text)
    messages.value.push({
      role: 'assistant',
      content: String(result.answer ?? ''),
      citations: (result.citations as Citation[]) || []
    })
  } finally {
    asking.value = false
  }
}

async function loadPolicies(page = 1) {
  pageNum.value = page
  loading.value = true
  try {
    const data = await policyApi.list({ pageNum, pageSize, keyword: keyword.value })
    policies.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPolicies(1))
</script>

<style scoped>
.chat {
  height: 420px;
  overflow-y: auto;
  padding-right: 6px;
}

.bubble {
  margin-bottom: 12px;
  max-width: 88%;
}

.bubble.user {
  margin-left: auto;
  text-align: right;
}

.bubble-text {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f2f4f8;
  line-height: 1.7;
  white-space: pre-wrap;
  text-align: left;
}

.bubble.user .bubble-text {
  background: #409eff;
  color: #fff;
}

.citations {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f8fbff;
  border-left: 3px solid #409eff;
  border-radius: 4px;
}

.citations-title {
  font-size: 12px;
  color: #8a94a6;
  margin-bottom: 4px;
}

.citation {
  font-size: 13px;
  color: #33475b;
  line-height: 1.7;
}

.ask {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.policy-table {
  margin: 12px 0;
}
</style>
