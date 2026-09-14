<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>岗位列表</span>
          <div class="filters">
            <el-input v-model="keyword" placeholder="岗位 / 企业 / 技能关键词" clearable style="width: 240px" />
            <el-input v-model="city" placeholder="城市" clearable style="width: 140px" />
            <el-button type="primary" @click="load(1)">查询</el-button>
            <el-button v-if="canPublish" @click="publishVisible = true">发布岗位</el-button>
          </div>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="岗位" min-width="170" />
        <el-table-column prop="companyName" label="企业" min-width="170" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column label="薪资(元/月)" width="150">
          <template #default="{ row }">
            {{ row.salaryMin ? `${row.salaryMin}-${row.salaryMax}` : '面议' }}
          </template>
        </el-table-column>
        <el-table-column prop="educationReq" label="学历" width="90" />
        <el-table-column prop="deadline" label="截止日期" width="130" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isStudent" link type="primary" @click="match(row)">智能匹配</el-button>
            <el-button v-if="isStudent" link type="success" @click="apply(row)">一键投递</el-button>
            <el-button v-if="!isStudent" link type="danger" @click="offline(row)">下线</el-button>
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

    <el-drawer v-model="matchVisible" title="智能匹配结果" size="42%">
      <div v-if="matchResult">
        <el-progress type="dashboard" :percentage="matchResult.matchScore" :width="150" />
        <el-tag class="level" type="success" effect="plain">{{ matchResult.matchLevel }}</el-tag>
        <h4>命中技能</h4>
        <el-tag v-for="skill in matchResult.matchedSkills" :key="skill" class="tag">{{ skill }}</el-tag>
        <h4>缺失技能（建议补齐）</h4>
        <el-tag v-for="skill in matchResult.missingSkills" :key="skill" type="warning" class="tag">{{ skill }}</el-tag>
        <h4>打分依据</h4>
        <ul>
          <li v-for="reason in matchResult.scoreReasons" :key="reason">{{ reason }}</li>
        </ul>
        <h4>智能体解读</h4>
        <pre class="json">{{ JSON.stringify(matchResult.agentExplanation, null, 2) }}</pre>
      </div>
    </el-drawer>

    <el-dialog v-model="publishVisible" title="发布岗位" width="640px">
      <el-form :model="publishForm" label-width="90px">
        <el-form-item label="岗位名称"><el-input v-model="publishForm.title" /></el-form-item>
        <el-form-item label="企业名称"><el-input v-model="publishForm.companyName" /></el-form-item>
        <el-form-item label="岗位类别"><el-input v-model="publishForm.jobCategory" placeholder="如：互联网 / 金融 / 制造" /></el-form-item>
        <el-form-item label="工作城市"><el-input v-model="publishForm.city" /></el-form-item>
        <el-form-item label="薪资范围">
          <el-input-number v-model="publishForm.salaryMin" :min="0" :step="1000" />
          <span class="dash">—</span>
          <el-input-number v-model="publishForm.salaryMax" :min="0" :step="1000" />
        </el-form-item>
        <el-form-item label="学历要求">
          <el-select v-model="publishForm.educationReq" style="width: 100%">
            <el-option label="专科" value="专科" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
          </el-select>
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input v-model="publishForm.requirement" type="textarea" :rows="4"
                    placeholder="技能的英文/中文关键词用逗号分隔，便于智能匹配，如：Java,Spring Boot,MySQL" />
        </el-form-item>
        <el-form-item label="岗位职责"><el-input v-model="publishForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" @click="publish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { jobApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { JobMatch, JobPost } from '@/types/api'

const userStore = useUserStore()
const isStudent = computed(() => userStore.roleCode === 'STUDENT')
const canPublish = computed(() => ['HR', 'COUNSELOR', 'COLLEGE_ADMIN'].includes(userStore.roleCode))

const list = ref<JobPost[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)
const keyword = ref('')
const city = ref('')

const matchVisible = ref(false)
const matchResult = ref<JobMatch | null>(null)
const publishVisible = ref(false)
const publishForm = reactive<Record<string, any>>({
  title: '', companyName: '', jobCategory: '互联网', city: '成都',
  salaryMin: 6000, salaryMax: 12000, educationReq: '本科', requirement: '', description: ''
})

async function load(page = 1) {
  pageNum.value = page
  loading.value = true
  try {
    const data = await jobApi.list({ pageNum, pageSize, keyword: keyword.value, city: city.value })
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function match(row: JobPost) {
  matchResult.value = await jobApi.match(row.id)
  matchVisible.value = true
}

async function apply(row: JobPost) {
  await jobApi.apply(row.id)
  ElMessage.success('投递成功，可在“我的投递”中查看进度')
}

async function offline(row: JobPost) {
  await jobApi.offline(row.id)
  ElMessage.success('岗位已下线')
  load(pageNum.value)
}

async function publish() {
  if (!publishForm.title || !publishForm.companyName || !publishForm.requirement) {
    ElMessage.warning('岗位名称、企业名称、任职要求为必填项')
    return
  }
  await jobApi.publish({ ...publishForm })
  ElMessage.success('岗位发布成功')
  publishVisible.value = false
  load(1)
}

onMounted(() => load())
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

.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.level {
  margin-left: 16px;
}

.tag {
  margin: 0 8px 8px 0;
}

.dash {
  margin: 0 8px;
}

.json {
  white-space: pre-wrap;
  background: #f7f9fc;
  padding: 10px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.6;
}
</style>
