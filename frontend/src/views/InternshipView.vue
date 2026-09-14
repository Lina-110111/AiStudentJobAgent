<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isStudent ? '我的实习申请' : '待审批实习申请' }}</span>
          <el-button v-if="isStudent" type="primary" @click="applyVisible = true">提交实习申请</el-button>
          <el-button v-else @click="load()">刷新</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="companyName" label="实习单位" min-width="180" />
        <el-table-column prop="position" label="岗位" min-width="140" />
        <el-table-column label="流程节点" width="150">
          <template #default="{ row }">
            <el-tag :type="nodeTagType(row.currentNode)" effect="plain">{{ nodeLabel(row.currentNode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PASSED' ? 'success' : row.status === 'REJECTED' ? 'danger' : 'warning'">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="三方评分" width="140">
          <template #default="{ row }">
            <span>自评 {{ row.selfScore ?? '-' }} / 导师 {{ row.mentorScore ?? '-' }} / 校方 {{ row.teacherScore ?? '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="finalScore" label="汇总分" width="90" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showFlow(row)">流程轨迹</el-button>
            <el-button v-if="isStudent" link type="success" @click="openLog(row)">提交日志</el-button>
            <el-button v-if="!isStudent && row.status === 'RUNNING'" link type="warning" @click="approve(row)">审批</el-button>
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

    <el-dialog v-model="applyVisible" title="提交实习申请" width="560px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="实习单位"><el-input v-model="applyForm.companyName" /></el-form-item>
        <el-form-item label="实习岗位"><el-input v-model="applyForm.position" /></el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="applyForm.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="applyForm.endDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="applyForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logVisible" title="提交实习日志" width="620px">
      <el-form :model="logForm" label-width="80px">
        <el-form-item label="第几周"><el-input-number v-model="logForm.weekNo" :min="1" :max="30" /></el-form-item>
        <el-form-item label="日志内容">
          <el-input v-model="logForm.content" type="textarea" :rows="8"
                    placeholder="建议按“本周任务 / 完成情况 / 问题与解决 / 下周计划”结构书写，便于智能体点评" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="logVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLog">提交</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="flowVisible" title="流程轨迹" size="40%">
      <el-timeline>
        <el-timeline-item v-for="(item, index) in flowTrace" :key="index" :timestamp="String(item.time)">
          <strong>{{ item.action }}</strong> —— 节点：{{ nodeLabel(String(item.node)) }}
          <div class="trace-comment">{{ item.comment }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { internshipApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isStudent = computed(() => userStore.roleCode === 'STUDENT')

const list = ref<Array<Record<string, any>>>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10
const loading = ref(false)

const applyVisible = ref(false)
const logVisible = ref(false)
const flowVisible = ref(false)
const flowTrace = ref<Array<Record<string, unknown>>>([])
const currentApplyId = ref<number | null>(null)

const applyForm = reactive<Record<string, any>>({ companyName: '', position: '', startDate: '', endDate: '', remark: '' })
const logForm = reactive<Record<string, any>>({ weekNo: 1, content: '' })

const NODE_LABEL: Record<string, string> = {
  MENTOR_REVIEW: '企业导师审批',
  COUNSELOR_REVIEW: '辅导员审批',
  COLLEGE_REVIEW: '院系管理员审批',
  PASSED: '审批通过',
  REJECTED: '已驳回'
}

function nodeLabel(node: string) {
  return NODE_LABEL[node] || node
}

function nodeTagType(node: string) {
  if (node === 'PASSED') return 'success'
  if (node === 'REJECTED') return 'danger'
  return 'warning'
}

function statusLabel(status: string) {
  return status === 'PASSED' ? '已通过' : status === 'REJECTED' ? '已驳回' : '审批中'
}

async function load(page = 1) {
  pageNum.value = page
  loading.value = true
  try {
    const data = isStudent.value ? await internshipApi.my(page, pageSize) : await internshipApi.pending(page, pageSize)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function submitApply() {
  await internshipApi.apply({ ...applyForm })
  ElMessage.success('申请已提交，进入企业导师审批节点')
  applyVisible.value = false
  load(1)
}

function openLog(row: Record<string, any>) {
  currentApplyId.value = row.id
  logVisible.value = true
}

async function submitLog() {
  if (!currentApplyId.value) return
  await internshipApi.submitLog(currentApplyId.value, { ...logForm })
  ElMessage.success('日志已提交')
  logVisible.value = false
  logForm.content = ''
}

async function approve(row: Record<string, any>) {
  const action = await ElMessageBox.confirm(
    `当前节点：${nodeLabel(row.currentNode)}。通过将流转到下一节点，驳回将回退到上一节点。`,
    '审批确认',
    { distinguishCancelAndClose: true, confirmButtonText: '通过', cancelButtonText: '驳回' }
  ).then(() => true).catch((action) => (action === 'cancel' ? false : null))

  if (action === null) return
  const { value } = await ElMessageBox.prompt('请填写审批意见', '审批意见', { inputValue: '' })
  await internshipApi.approve(row.id, { approve: action, comment: value })
  ElMessage.success('审批已提交')
  load(pageNum.value)
}

function showFlow(row: Record<string, any>) {
  try {
    flowTrace.value = JSON.parse(row.flowTraceJson || '[]')
  } catch {
    flowTrace.value = []
  }
  flowVisible.value = true
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

.trace-comment {
  color: #6b7785;
  font-size: 13px;
  margin-top: 4px;
}
</style>
