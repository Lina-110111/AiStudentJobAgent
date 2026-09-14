<template>
  <div class="page">
    <el-card shadow="never" header="个人档案">
      <el-form :model="form" label-width="100px" style="max-width: 720px">
        <el-form-item label="登录账号">
          <el-input :model-value="userStore.info?.username" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-input :model-value="userStore.info?.roleLabel" disabled />
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="学院"><el-input v-model="form.college" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="年级"><el-input v-model="form.grade" placeholder="如 2024" /></el-form-item>
        <el-form-item label="学历">
          <el-select v-model="form.education" style="width: 100%">
            <el-option label="专科" value="专科" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
          </el-select>
        </el-form-item>
        <el-form-item label="技能标签">
          <el-input v-model="form.skills" placeholder="逗号分隔，如：Java,Spring Boot,MySQL,Redis" />
        </el-form-item>
        <el-form-item label="求职意向">
          <el-input v-model="form.jobIntention" placeholder="如：成都 Java后端开发" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存档案</el-button>
        </el-form-item>
      </el-form>
      <el-alert
        type="info"
        :closable="false"
        title="完善的档案会直接影响岗位匹配得分（技能、学历、意向、完整度五个维度）"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const saving = ref(false)
const form = reactive<Record<string, any>>({
  realName: '', phone: '', email: '', college: '', major: '',
  grade: '', education: '', skills: '', jobIntention: ''
})

onMounted(async () => {
  const info = userStore.info ?? (await userStore.loadProfile())
  if (!info) {
    return
  }
  Object.assign(form, {
    realName: info.realName ?? '',
    phone: info.phone ?? '',
    email: info.email ?? '',
    college: info.college ?? '',
    major: info.major ?? '',
    grade: info.grade ?? '',
    education: info.education ?? '',
    skills: info.skills ?? '',
    jobIntention: info.jobIntention ?? ''
  })
})

async function save() {
  saving.value = true
  try {
    await authApi.updateProfile({ ...form })
    await userStore.loadProfile()
    ElMessage.success('档案已更新')
  } finally {
    saving.value = false
  }
}
</script>
