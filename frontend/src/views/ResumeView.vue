<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的简历</span>
          <el-button type="primary" @click="dialogVisible = true">新增简历</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="简历标题" min-width="180" />
        <el-table-column prop="targetJob" label="目标岗位" min-width="160" />
        <el-table-column prop="fileName" label="附件" width="180" />
        <el-table-column label="诊断得分" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.score" type="success" effect="plain">{{ row.score }}</el-tag>
            <span v-else class="muted">未诊断</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :loading="diagnosing === row.id" @click="diagnose(row)">
              AI 诊断
            </el-button>
            <el-button link type="info" @click="showReport(row)">查看报告</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="load"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增简历" width="620px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="简历标题"><el-input v-model="form.title" placeholder="如：Java后端开发-张三" /></el-form-item>
        <el-form-item label="目标岗位"><el-input v-model="form.targetJob" placeholder="如：Java后端开发工程师" /></el-form-item>
        <el-form-item label="简历正文">
          <el-input
            v-model="form.contentText"
            type="textarea"
            :rows="8"
            placeholder="粘贴简历文字内容（后续版本将支持 PDF / Word 自动解析）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="create">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="reportVisible" title="AI 简历诊断报告" size="46%">
      <pre class="report">{{ reportText }}</pre>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { resumeApi } from '@/api'
import type { ResumeItem } from '@/types/api'

const list = ref<ResumeItem[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const diagnosing = ref<number | null>(null)
const dialogVisible = ref(false)
const reportVisible = ref(false)
const reportText = ref('')

const form = reactive({ title: '', targetJob: '', contentText: '' })

async function load(page = 1) {
  pageNum.value = page
  loading.value = true
  try {
    const data = await resumeApi.list(pageNum.value, pageSize)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!form.title) {
    ElMessage.warning('请填写简历标题')
    return
  }
  await resumeApi.create({ ...form })
  ElMessage.success('简历已保存')
  dialogVisible.value = false
  form.title = ''
  form.targetJob = ''
  form.contentText = ''
  load()
}

async function diagnose(row: ResumeItem) {
  diagnosing.value = row.id
  try {
    const result = await resumeApi.diagnose(row.id, row.targetJob)
    reportText.value = JSON.stringify(result, null, 2)
    reportVisible.value = true
    ElMessage.success('诊断完成')
    load(pageNum.value)
  } finally {
    diagnosing.value = null
  }
}

async function showReport(row: ResumeItem) {
  const detail = await resumeApi.detail(row.id)
  const raw = detail.diagnosisJson
  if (!raw) {
    ElMessage.info('该简历还没有诊断报告，请先执行 AI 诊断')
    return
  }
  reportText.value = JSON.stringify(JSON.parse(raw), null, 2)
  reportVisible.value = true
}

onMounted(() => load())
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.muted {
  color: #b1b8c4;
}

.report {
  white-space: pre-wrap;
  font-family: 'Consolas', 'Microsoft YaHei', monospace;
  font-size: 13px;
  line-height: 1.7;
}
</style>
