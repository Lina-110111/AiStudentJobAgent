<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>角色 - 权限矩阵</span>
          <el-tag type="success" effect="plain">当前角色：{{ userStore.info?.roleLabel }}</el-tag>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="tip"
        title="数据来源：sys_user → sys_user_role → sys_role → sys_role_permission → sys_permission。这里显示的权限码，就是后端 @PreAuthorize 校验用的同一份数据。"
      />

      <el-table :data="permRows" v-loading="loading" stripe border class="matrix">
        <el-table-column prop="code" label="权限码" min-width="190" />
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column
          v-for="role in roles"
          :key="role.roleCode"
          :label="role.roleName"
          align="center"
          width="130"
        >
          <template #default="{ row }">
            <el-tag v-if="role.permissions.includes(row.code)" type="success" size="small" effect="plain">有</el-tag>
            <span v-else class="none">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="mine">
      <template #header>
        <span>我的权限（{{ userStore.permCount }} 项）</span>
      </template>
      <el-tag v-for="perm in userStore.permissions || []" :key="perm" class="perm-tag" effect="plain">{{ perm }}</el-tag>
      <span v-if="!userStore.permCount" class="none">当前账号没有任何权限码</span>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { rbacApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { RolePerm } from '@/types/api'

const userStore = useUserStore()
const roles = ref<RolePerm[]>([])
const loading = ref(false)

/** 把所有角色的权限码取并集，一行一个权限，列对应角色 */
const permRows = computed(() => {
  const set = new Set<string>()
  roles.value.forEach((role) => role.permissions.forEach((perm) => set.add(perm)))
  return Array.from(set)
    .sort((a, b) => a.localeCompare(b))
    .map((code) => ({ code, module: code.split(':')[0] }))
})

onMounted(async () => {
  loading.value = true
  try {
    roles.value = await rbacApi.matrix()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip {
  margin-bottom: 12px;
}

.matrix {
  width: 100%;
}

.none {
  color: #c0c4cc;
}

.mine {
  margin-top: 4px;
}

.perm-tag {
  margin: 0 8px 8px 0;
}
</style>
