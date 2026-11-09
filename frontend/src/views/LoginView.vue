<template>
  <div class="login-page">
    <div class="login-card">
      <div class="brand">
        <h1>AI 大学生就业服务智能体</h1>
        <p>简历诊断 · 岗位匹配 · 实习管理 · 面试训练 · 政策问答</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 账号密码登录 -->
        <el-tab-pane label="账号密码" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top" @keyup.enter="onPasswordSubmit">
            <el-form-item label="登录账号" prop="username">
              <el-input v-model="passwordForm.username" placeholder="请输入学号 / 工号" clearable />
            </el-form-item>
            <el-form-item label="登录密码" prop="password">
              <el-input v-model="passwordForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>

            <el-form-item label="图形验证码" prop="captchaCode">
              <div class="captcha-row">
                <el-input v-model="passwordForm.captchaCode" placeholder="请输入右侧字符" />
                <img
                  class="captcha-img"
                  :src="toImgSrc(imageCaptcha.image)"
                  alt="图形验证码"
                  title="看不清？点击刷新"
                  @click="refreshImage"
                />
              </div>
            </el-form-item>

            <el-form-item label="顺序点选验证码">
              <div class="click-row">
                <div
                  v-for="(img, index) in clickCaptcha.images"
                  :key="img + index"
                  class="click-tile"
                  :class="{ active: orderOf(index) > 0 }"
                  @click="onClickImage(index)"
                >
                  <span class="click-emoji">{{ EMOJI[img] || '❓' }}</span>
                  <span class="click-name">{{ NAME[img] || img }}</span>
                  <span v-if="orderOf(index) > 0" class="click-order">{{ orderOf(index) }}</span>
                </div>
              </div>
              <div class="click-hint">
                请依次点击：<b>{{ clickHintText }}</b>
                <el-link type="primary" @click="resetClick">清空重选</el-link>
                <el-link type="primary" @click="refreshClick">换一批</el-link>
              </div>
            </el-form-item>

            <el-button type="primary" class="submit" :loading="loading" @click="onPasswordSubmit">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <!-- 手机号登录 -->
        <el-tab-pane label="手机号登录" name="sms">
          <el-form ref="smsFormRef" :model="smsForm" :rules="smsRules" label-position="top" @keyup.enter="onSmsSubmit">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="smsForm.phone" placeholder="请输入手机号" clearable maxlength="11" />
            </el-form-item>
            <el-form-item label="短信验证码" prop="code">
              <div class="captcha-row">
                <el-input v-model="smsForm.code" placeholder="请输入短信验证码" maxlength="6" />
                <el-button :disabled="smsCountdown > 0" @click="onSendSmsCode">
                  {{ smsCountdown > 0 ? `${smsCountdown}s后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="图形验证码" prop="captchaCode">
              <div class="captcha-row">
                <el-input v-model="smsForm.captchaCode" placeholder="请输入右侧字符" />
                <img
                  class="captcha-img"
                  :src="toImgSrc(imageCaptcha.image)"
                  alt="图形验证码"
                  title="看不清？点击刷新"
                  @click="refreshImage"
                />
              </div>
            </el-form-item>

            <el-form-item label="顺序点选验证码">
              <div class="click-row">
                <div
                  v-for="(img, index) in clickCaptcha.images"
                  :key="img + index"
                  class="click-tile"
                  :class="{ active: orderOf(index) > 0 }"
                  @click="onClickImage(index)"
                >
                  <span class="click-emoji">{{ EMOJI[img] || '❓' }}</span>
                  <span class="click-name">{{ NAME[img] || img }}</span>
                  <span v-if="orderOf(index) > 0" class="click-order">{{ orderOf(index) }}</span>
                </div>
              </div>
              <div class="click-hint">
                请依次点击：<b>{{ clickHintText }}</b>
                <el-link type="primary" @click="resetClick">清空重选</el-link>
                <el-link type="primary" @click="refreshClick">换一批</el-link>
              </div>
            </el-form-item>

            <el-button type="primary" class="submit" :loading="loading" @click="onSmsSubmit">登 录</el-button>
          </el-form>
        </el-tab-pane>

        <!-- 邮箱登录 -->
        <el-tab-pane label="邮箱登录" name="email">
          <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" label-position="top" @keyup.enter="onEmailSubmit">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="emailForm.email" placeholder="请输入邮箱地址" clearable />
            </el-form-item>
            <el-form-item label="邮箱验证码" prop="code">
              <div class="captcha-row">
                <el-input v-model="emailForm.code" placeholder="请输入邮箱验证码" maxlength="6" />
                <el-button :disabled="emailCountdown > 0" @click="onSendEmailCode">
                  {{ emailCountdown > 0 ? `${emailCountdown}s后重发` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="图形验证码" prop="captchaCode">
              <div class="captcha-row">
                <el-input v-model="emailForm.captchaCode" placeholder="请输入右侧字符" />
                <img
                  class="captcha-img"
                  :src="toImgSrc(imageCaptcha.image)"
                  alt="图形验证码"
                  title="看不清？点击刷新"
                  @click="refreshImage"
                />
              </div>
            </el-form-item>

            <el-button type="primary" class="submit" :loading="loading" @click="onEmailSubmit">登 录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="tips">
        还没有账号？<el-link type="primary" @click="registerVisible = true">注册一个</el-link>
      </div>
      <div class="demo-tip">
        演示账号（密码均为 123456）：student01 / counselor01 / hr01 / admin01
        <br />演示手机号：13800138000（验证码Mock模式，后台日志可查）
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { authApi, captchaApi } from '@/api'
import { setToken } from '@/api/request'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('password')
const loading = ref(false)
const registering = ref(false)
const registerVisible = ref(false)

// 账号密码登录表单
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({ username: 'student01', password: '123456', captchaCode: '' })

// 手机号登录表单
const smsFormRef = ref<FormInstance>()
const smsForm = reactive({ phone: '13800138000', code: '', captchaCode: '' })
const smsCountdown = ref(0)

// 邮箱登录表单
const emailFormRef = ref<FormInstance>()
const emailForm = reactive({ email: '1183385627@qq.com', code: '', captchaCode: '' })
const emailCountdown = ref(0)

const registerForm = reactive({ username: '', password: '', realName: '', roleCode: 'STUDENT' })

// 验证码相关（三种登录方式共用）
const imageCaptcha = reactive({ captchaId: '', image: '' })
const clickCaptcha = reactive({ captchaId: '', images: [] as string[], hint: [] as string[] })
const clickOrder = ref<number[]>([])

// 点选验证码的正确点击顺序提示，由后端每次随机生成
const clickHintText = computed(() => (clickCaptcha.hint.length ? clickCaptcha.hint.join(' → ') : '加载中…'))

// 后端演示返回的是图片文件名，前端用 emoji 占位渲染
const EMOJI: Record<string, string> = {
  'cat.jpg': '🐱',
  'dog.jpg': '🐶',
  'bird.jpg': '🐦',
  'fish.jpg': '🐟',
  'tiger.jpg': '🐯',
  'rabbit.jpg': '🐰',
  'panda.jpg': '🐼',
  'monkey.jpg': '🐵'
}
const NAME: Record<string, string> = {
  'cat.jpg': '猫',
  'dog.jpg': '狗',
  'bird.jpg': '鸟',
  'fish.jpg': '鱼',
  'tiger.jpg': '虎',
  'rabbit.jpg': '兔',
  'panda.jpg': '熊猫',
  'monkey.jpg': '猴'
}

const passwordRules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

const smsRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

const emailRules = {
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  code: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

function toImgSrc(img: string): string {
  return img.startsWith('data:') ? img : `data:image/png;base64,${img}`
}

async function refreshImage() {
  const data = await captchaApi.image()
  imageCaptcha.captchaId = data.captchaId
  imageCaptcha.image = data.image
}

async function refreshClick() {
  const data = await captchaApi.click()
  clickCaptcha.captchaId = data.captchaId
  clickCaptcha.images = data.images
  clickCaptcha.hint = data.hint ?? []
  clickOrder.value = []
}

async function refreshCaptchas() {
  passwordForm.captchaCode = ''
  smsForm.captchaCode = ''
  emailForm.captchaCode = ''
  await Promise.all([refreshImage(), refreshClick()])
}

function onClickImage(index: number) {
  if (clickOrder.value.includes(index)) return
  clickOrder.value.push(index)
}

function resetClick() {
  clickOrder.value = []
}

function orderOf(index: number): number {
  const i = clickOrder.value.indexOf(index)
  return i >= 0 ? i + 1 : 0
}

// 账号密码登录
async function onPasswordSubmit() {
  if (!passwordFormRef.value) return
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (clickOrder.value.length !== clickCaptcha.images.length) {
    ElMessage.warning('请按顺序点选全部图片')
    return
  }
  loading.value = true
  try {
    await userStore.login({
      username: passwordForm.username,
      password: passwordForm.password,
      imageCaptchaId: imageCaptcha.captchaId,
      captchaCode: passwordForm.captchaCode,
      clickCaptchaId: clickCaptcha.captchaId,
      clickCaptcha: [...clickOrder.value]
    })
    ElMessage.success('登录成功')
    router.push((route.query.redirect as string) || '/dashboard')
  } catch {
    await refreshCaptchas()
  } finally {
    loading.value = false
  }
}

// 发送短信验证码
async function onSendSmsCode() {
  if (!smsForm.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  if (!smsForm.captchaCode) {
    ElMessage.warning('请先输入图形验证码')
    return
  }
  try {
    await authApi.sendSmsCode({
      phone: smsForm.phone,
      captchaId: imageCaptcha.captchaId,
      captchaCode: smsForm.captchaCode
    })
    ElMessage.success('验证码已发送（Mock模式，验证码见后端日志）')
    smsForm.captchaCode = ''
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
    // 发送后刷新图形验证码
    await refreshImage()
  } catch {
    // 错误已被拦截器处理
  }
}

// 手机号登录
async function onSmsSubmit() {
  if (!smsFormRef.value) return
  const valid = await smsFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (clickOrder.value.length !== clickCaptcha.images.length) {
    ElMessage.warning('请按顺序点选全部图片')
    return
  }
  loading.value = true
  try {
    const res = await authApi.smsLogin({
      phone: smsForm.phone,
      code: smsForm.code,
      imageCaptchaId: imageCaptcha.captchaId,
      captchaCode: smsForm.captchaCode,
      clickCaptchaId: clickCaptcha.captchaId,
      clickCaptcha: [...clickOrder.value]
    })
    setToken(res.token)
    userStore.token = res.token
    await userStore.loadProfile()
    ElMessage.success('登录成功')
    router.push((route.query.redirect as string) || '/dashboard')
  } catch {
    await refreshCaptchas()
  } finally {
    loading.value = false
  }
}

// 发送邮箱验证码（需先通过图形验证码，防止邮件轰炸）
async function onSendEmailCode() {
  if (!emailForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  if (!emailForm.captchaCode) {
    ElMessage.warning('请先输入图形验证码')
    return
  }
  try {
    await authApi.sendEmailCode({
      email: emailForm.email,
      captchaId: imageCaptcha.captchaId,
      captchaCode: emailForm.captchaCode
    })
    ElMessage.success('验证码已发送，请查收邮箱')
    emailForm.captchaCode = ''
    emailCountdown.value = 60
    const timer = setInterval(() => {
      emailCountdown.value--
      if (emailCountdown.value <= 0) clearInterval(timer)
    }, 1000)
    // 图形验证码已被消费，刷新一张新的
    await refreshImage()
  } catch {
    // 错误已被拦截器处理
  }
}

// 邮箱登录
async function onEmailSubmit() {
  if (!emailFormRef.value) return
  const valid = await emailFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authApi.emailLogin({
      email: emailForm.email,
      code: emailForm.code,
      imageCaptchaId: imageCaptcha.captchaId,
      captchaCode: emailForm.captchaCode
    })
    setToken(res.token)
    userStore.token = res.token
    await userStore.loadProfile()
    ElMessage.success('登录成功')
    router.push((route.query.redirect as string) || '/dashboard')
  } catch {
    // 图形验证码一次性消费，登录失败后刷新
    emailForm.captchaCode = ''
    await refreshImage()
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

onMounted(refreshCaptchas)
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
  width: 440px;
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
  margin: 0 0 16px;
  color: #8a94a6;
  font-size: 13px;
}

.login-tabs {
  margin-bottom: 8px;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.captcha-img {
  height: 40px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  flex-shrink: 0;
}

.click-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.click-tile {
  position: relative;
  flex: 1;
  height: 72px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  transition: all 0.15s;
  background: #fafbfc;
}

.click-tile:hover {
  border-color: #409eff;
}

.click-tile.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.click-emoji {
  font-size: 24px;
  line-height: 1;
}

.click-name {
  margin-top: 4px;
  font-size: 12px;
  color: #5b6b82;
}

.click-order {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
}

.click-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #8a94a6;
  line-height: 1.6;
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
