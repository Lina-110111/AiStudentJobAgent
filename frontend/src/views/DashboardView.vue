<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}<span class="unit">{{ card.unit }}</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts">
      <el-col :span="12">
        <el-card shadow="never" header="行业分布（在招岗位）">
          <EChart :option="industryOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="薪资分析（各行业平均月薪 / 元）">
          <EChart :option="salaryOption" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts">
      <el-col :span="14">
        <el-card shadow="never" header="近 7 天投递趋势">
          <EChart :option="trendOption" height="280px" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" header="投递状态分布">
          <EChart :option="statusOption" height="280px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import EChart from '@/components/EChart.vue'
import { dashboardApi } from '@/api'
import type { DashboardData } from '@/types/api'

const data = ref<DashboardData | null>(null)

const cards = computed(() => [
  { label: '就业率', value: data.value?.employmentRate ?? 0, unit: '%' },
  { label: '在招岗位', value: Number(data.value?.statistics?.jobCount ?? 0), unit: '个' },
  { label: '累计投递', value: Number(data.value?.statistics?.applicationCount ?? 0), unit: '次' },
  { label: '政策条目', value: Number(data.value?.statistics?.policyCount ?? 0), unit: '条' }
])

const industryOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: ['40%', '68%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{c} 个' },
      data: (data.value?.industryDistribution || []).map((item) => ({ name: item.name, value: item.value }))
    }
  ]
}))

const salaryOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 60, right: 20, top: 20, bottom: 60 },
  xAxis: {
    type: 'category',
    data: (data.value?.salaryAnalysis || []).map((item) => item.name),
    axisLabel: { interval: 0, rotate: 20 }
  },
  yAxis: { type: 'value' },
  series: [
    {
      type: 'bar',
      barWidth: '45%',
      itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
      data: (data.value?.salaryAnalysis || []).map((item) => Number(item.avgSalary))
    }
  ]
}))

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 20, top: 20, bottom: 40 },
  xAxis: { type: 'category', data: (data.value?.applicationTrend || []).map((item) => item.day) },
  yAxis: { type: 'value', minInterval: 1 },
  series: [
    {
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      itemStyle: { color: '#67c23a' },
      data: (data.value?.applicationTrend || []).map((item) => item.value)
    }
  ]
}))

const statusOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      type: 'pie',
      radius: '62%',
      data: (data.value?.applicationStatus || []).map((item) => ({ name: item.name, value: item.value }))
    }
  ]
}))

onMounted(async () => {
  data.value = await dashboardApi.overview()
})
</script>

<style scoped>
.charts {
  margin-top: 16px;
}

.stat-card {
  border-radius: 10px;
}

.stat-label {
  color: #8a94a6;
  font-size: 13px;
}

.stat-value {
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
  color: #1f2d3d;
}

.unit {
  margin-left: 4px;
  font-size: 13px;
  color: #8a94a6;
  font-weight: 400;
}
</style>
