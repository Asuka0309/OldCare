<template>
  <div class="page-card">
    <div class="search-bar">
      <el-select v-model="period" placeholder="选择统计周期" style="width: 200px" @change="load">
        <el-option label="今天" value="today" />
        <el-option label="最近一周" value="week" />
        <el-option label="本月" value="month" />
        <el-option label="本年" value="year" />
      </el-select>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">总收入</div>
        <div class="stat-value">¥{{ formatAmount(statistics.totalAmount) }}</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">交易笔数</div>
        <div class="stat-value">{{ statistics.totalCount }}</div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-label">统计周期</div>
        <div class="stat-value">{{ getPeriodText(period) }}</div>
      </el-card>
    </div>

    <!-- 图表 -->
    <el-card shadow="hover" class="chart-card">
      <template #header>
        <div class="card-header">
          <span>收入趋势图</span>
        </div>
      </template>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="chartData.length === 0" class="empty">暂无数据</div>
      <v-chart v-else :option="chartOption" class="chart" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { getIncomeStatistics } from '../api/income'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const authStore = useAuthStore()
const isAdmin = computed(() => authStore.user?.role === 'admin')
const isCaregiver = computed(() => authStore.user?.role === 'caregiver')

const period = ref('month')
const loading = ref(false)
const statistics = ref({
  totalAmount: 0,
  totalCount: 0,
  chartData: []
})
const chartData = ref([])

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

// 获取周期文本
const getPeriodText = (p) => {
  const map = {
    today: '今天',
    week: '最近一周',
    month: '本月',
    year: '本年'
  }
  return map[p] || '本月'
}

// 图表配置
const chartOption = computed(() => {
  return {
    title: {
      text: isAdmin.value ? '总收入趋势' : '我的收入趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 600
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>${param.seriesName}: ¥${formatAmount(param.value)}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: chartData.value.map(item => {
        const date = new Date(item.date)
        return `${date.getMonth() + 1}/${date.getDate()}`
      })
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (value) => `¥${formatAmount(value)}`
      }
    },
    series: [
      {
        name: isAdmin.value ? '总收入' : '我的收入',
        type: 'line',
        smooth: true,
        data: chartData.value.map(item => Number(item.amount)),
        itemStyle: {
          color: '#000000'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(0, 0, 0, 0.2)' },
              { offset: 1, color: 'rgba(0, 0, 0, 0.02)' }
            ]
          }
        }
      }
    ]
  }
})

// 加载数据
const load = async () => {
  loading.value = true
  try {
    const data = await getIncomeStatistics(period.value)
    statistics.value = {
      totalAmount: data.totalAmount || 0,
      totalCount: data.totalCount || 0,
      chartData: data.chartData || []
    }
    chartData.value = data.chartData || []
  } catch (error) {
    console.error('加载收入统计失败:', error)
    ElMessage.error('加载失败: ' + (error.message || error))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.page-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.search-bar {
  margin-bottom: 24px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #000000;
}

.chart-card {
  margin-top: 24px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.chart {
  height: 400px;
  width: 100%;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #94a3b8;
}
</style>

