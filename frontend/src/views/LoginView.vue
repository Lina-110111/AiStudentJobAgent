<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <h1>AI 大学生就业服务智能体</h1>
        <p>简历诊断 · 岗位匹配 · 实习管理 · 面试训练 · 政策问答</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onSubmit">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入学号 / 工号" clearable />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">登 录</el-button>
      </el-form>

      <div class="tips">
        还没有账号？<el-link type="primary" @click="registerVisible = true">注册一个</el-link>
      </div>
      <div class="demo-tip">
        演示账号（密码均为 123456）：student01 / counselor01 / hr01 / admin01
      </div>
    </div>

    <el-dialog v-model="registerVisible" title="注册账号" width="460px">
      <el-form :model="registerForm" label-width="90px">
        <el-form-item label="账号"><el-input v-model="registerForm.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="registerForm.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="registerForm.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="registerForm.roleCode" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="辅导员" value="COUNSELOR" />
            <el-option label="企业HR" value="HR" />
            <el-option label="院系管理员" value="COLLEGE_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerVisible = false">取消</el-button>
        <el-button type="primary" :loading="registering" @click="onRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const registering = ref(false)
const registerVisible = ref(false)

const form = reactive({ username: 'student01', password: '123456' })
const registerForm = reactive({ username: '', password: '', realName: '', roleCode: 'STUDENT' })

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push((route.query.redirect as string) || '/dashboard')
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  registering.value = true
  try {
    await authApi.register({ ...registerForm })
    ElMessage.success('注册成功，请登录')
    registerVisible.value = false
  } finally {
    registering.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #2c5364 60%, #409eff 100%);
}

.login-card {
  width: 420px;
  padding: 36px 32px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.18);
}

.brand h1 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1f2d3d;
}

.brand p {
  margin: 0 0 24px;
  color: #8a94a6;
  font-size: 13px;
}

.submit {
  width: 100%;
  margin-top: 6px;
}

.tips {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #8a94a6;
}

.demo-tip {
  margin-top: 12px;
  padding: 8px 10px;
  background: #f4f8ff;
  border-radius: 6px;
  font-size: 12px;
  color: #5b6b82;
  line-height: 1.6;
}
</style>
