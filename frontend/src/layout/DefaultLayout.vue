<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-mark">AI</span>
        <span class="logo-text">就业服务智能体</span>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="item in visibleMenus" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ route.meta.title || 'AI 大学生就业服务智能体' }}</div>
        <div class="header-right">
          <el-tag v-if="aiStatus" :type="aiStatus.degraded ? 'warning' : 'success'" effect="plain">
            {{ aiStatus.degraded ? 'AI：本地规则模式' : `AI：${aiStatus.provider}` }}
          </el-tag>
          <el-dropdown @command="onCommand">
            <span class="user">
              <el-icon><User /></el-icon>
              {{ userStore.realName }}
              <el-tag size="small" class="role-tag">{{ userStore.info?.roleLabel }}</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人档案</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive :max="6">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { aiApi } from '@/api'
import { menus } from '@/router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const aiStatus = ref<Record<string, any> | null>(null)

const visibleMenus = computed(() =>
  menus.filter((menu) => !userStore.info || menu.roles.includes(userStore.info.roleCode))
)

async function onCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  try {
    aiStatus.value = await aiApi.status()
  } catch {
    aiStatus.value = null
  }
})
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #1f2d3d;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.logo-mark {
  background: #409eff;
  border-radius: 6px;
  padding: 2px 8px;
  font-size: 14px;
}

.logo-text {
  font-size: 15px;
}

.menu {
  border-right: none;
  background: transparent;
  flex: 1;
}

.menu :deep(.el-menu-item) {
  color: #cfd8e3;
}

.menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: #409eff;
}

.menu :deep(.el-menu-item:hover) {
  background: #2c3e50;
  color: #fff;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}

.role-tag {
  margin-left: 4px;
}

.main {
  background: #f5f7fa;
}
</style>
