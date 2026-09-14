<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>AI 面试训练</span>
          <div class="filters">
            <el-select v-model="category" style="width: 160px">
              <el-option label="技术岗" value="技术岗" />
              <el-option label="管理岗" value="管理岗" />
              <el-option label="综合素质" value="综合素质" />
            </el-select>
            <el-select v-model="difficulty" style="width: 140px">
              <el-option label="标准（3题）" value="NORMAL" />
              <el-option label="完整（5题）" value="HARD" />
            </el-select>
            <el-button type="primary" @click="start">开始训练</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="!session" description="选择场景后点击“开始训练”，智能体将逐题提问并评分" />

      <div v-else>
        <div class="progress">
          <el-steps :active="currentIndex" simple>
            <el-step v-for="(q, i) in questions" :key="i" :title="`第 ${i + 1} 题`" />
          </el-steps>
        </div>

        <el-alert :title="questions[currentIndex]" type="info" :closable="false" class="question" />

        <el-input
          v-model="answer"
          type="textarea"
          :rows="8"
          placeholder="建议使用“结论先行 + 分点论述 + 实例支撑”的结构作答"
        />
        <div class="actions">
          <el-button type="primary" :loading="submitting" @click="submitAnswer">提交作答并评分</el-button>
          <el-button :disabled="currentIndex >= questions.length - 1" @click="currentIndex++">下一题</el-button>
          <el-button type="success" @click="finish">结束并生成报告</el-button>
        </div>

        <el-card v-if="evaluation" shadow="never" class="evaluation" header="本题评分">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="逻辑性">{{ evaluation.logicScore }}</el-descriptions-item>
            <el-descriptions-item label="完整性">{{ evaluation.completenessScore }}</el-descriptions-item>
            <el-descriptions-item label="表达力">{{ evaluation.expressionScore }}</el-descriptions-item>
            <el-descriptions-item label="总分">{{ evaluation.totalScore }}</el-descriptions-item>
          </el-descriptions>
          <p class="comment">{{ evaluation.comment }}</p>
        </el-card>
      </div>
    </el-card>

    <el-card shadow="never" class="history" header="历史训练记录">
      <el-table :data="history" stripe>
        <el-table-column prop="category" label="场景" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="totalScore" label="综合得分" width="120" />
        <el-table-column prop="createTime" label="训练时间" />
      </el-table>
    </el-card>

    <el-drawer v-model="reportVisible" title="面试训练报告" size="46%">
      <pre class="json">{{ JSON.stringify(report, null, 2) }}</pre>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { interviewApi } from '@/api'
import type { InterviewSession } from '@/types/api'

const category = ref('技术岗')
const difficulty = ref('NORMAL')
const session = ref<InterviewSession | null>(null)
const questions = ref<string[]>([])
const currentIndex = ref(0)
const answer = ref('')
const submitting = ref(false)
const evaluation = ref<Record<string, any> | null>(null)
const report = ref<Record<string, unknown>>({})
const reportVisible = ref(false)
const history = ref<InterviewSession[]>([])

async function start() {
  session.value = await interviewApi.start({ category: category.value, difficulty: difficulty.value })
  questions.value = JSON.parse(session.value.questionsJson || '[]')
  currentIndex.value = 0
  answer.value = ''
  evaluation.value = null
  ElMessage.success('训练已开始，请逐题作答')
}

async function submitAnswer() {
  if (!session.value) return
  if (!answer.value.trim()) {
    ElMessage.warning('请先输入你的回答')
    return
  }
  submitting.value = true
  try {
    const result = await interviewApi.answer(session.value.id, {
      questionIndex: currentIndex.value,
      answer: answer.value
    })
    evaluation.value = result.evaluation as Record<string, any>
    ElMessage.success('评分完成')
  } finally {
    submitting.value = false
  }
}

async function finish() {
  if (!session.value) return
  report.value = await interviewApi.finish(session.value.id)
  reportVisible.value = true
  await loadHistory()
}

async function loadHistory() {
  const data = await interviewApi.history(1, 10)
  history.value = data.records
}

onMounted(loadHistory)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  display: flex;
  gap: 10px;
}

.progress {
  margin-bottom: 16px;
}

.question {
  margin-bottom: 14px;
}

.actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;
}

.evaluation {
  margin-top: 18px;
}

.comment {
  margin: 12px 0 0;
  color: #4b5768;
  line-height: 1.7;
}

.history {
  margin-top: 16px;
}

.json {
  white-space: pre-wrap;
  font-size: 12px;
  line-height: 1.6;
}
</style>
