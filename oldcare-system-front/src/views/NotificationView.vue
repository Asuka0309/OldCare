<template>
  <div class="notification-page">
    <el-row :gutter="20" class="filter-row">
      <el-col :xs="24" :sm="12">
        <el-input
          v-model="searchText"
          placeholder="搜索通知..."
          clearable
          @input="handleSearch"
        />
      </el-col>
      <el-col :xs="24" :sm="12" class="filter-buttons">
        <el-button type="primary" @click="markAllAsRead" v-if="unreadCount > 0">
          标记全部已读
        </el-button>
        <el-button @click="fetchNotifications">刷新</el-button>
      </el-col>
    </el-row>

    <el-table :data="filteredNotifications" stripe style="width: 100%;">
      <el-table-column prop="title" label="通知标题" min-width="200" />
      <el-table-column prop="content" label="内容" min-width="300" />
      <el-table-column prop="notificationType" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="getTypeTagType(row.notificationType)">
            {{ getTypeText(row.notificationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="!row.isRead" type="info">未读</el-tag>
          <el-tag v-else type="success">已读</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button
            v-if="!row.isRead"
            type="primary"
            text
            size="small"
            @click="markAsRead(row.id)"
          >
            标记已读
          </el-button>
          <span v-else style="color: #909399;">已读</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const notifications = ref([])
const searchText = ref('')
const loading = ref(false)
let refreshInterval = null

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    // 先尝试获取所有通知，如果不支持则获取未读通知
    let data = null
    try {
      data = await http.get('/notifications')
    } catch (err) {
      // 如果全部通知端点不存在，则获取未读通知
      data = await http.get('/notifications/unread')
    }
    notifications.value = data || []
  } catch (err) {
    ElMessage.error('加载通知失败: ' + err.message)
  } finally {
    loading.value = false
  }
}

// 标记单条为已读
const markAsRead = async (notificationId) => {
  try {
    await http.put(`/notifications/${notificationId}/read`)
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.isRead = true
    }
    ElMessage.success('已标记为已读')
    // 不重新刷新列表，保留通知记录
  } catch (err) {
    ElMessage.error('标记失败: ' + err.message)
  }
}

// 标记全部为已读
const markAllAsRead = async () => {
  try {
    for (const notification of notifications.value) {
      if (!notification.isRead) {
        await markAsRead(notification.id)
      }
    }
    ElMessage.success('已标记全部为已读')
  } catch (err) {
    ElMessage.error('操作失败: ' + err.message)
  }
}

// 搜索
const handleSearch = () => {
  // 实时搜索
}

// 过滤的通知列表
const filteredNotifications = computed(() => {
  if (!searchText.value) {
    return notifications.value
  }
  return notifications.value.filter(n =>
    n.title.includes(searchText.value) || n.content.includes(searchText.value)
  )
})

// 未读数量
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.isRead).length
})

// 获取通知类型对应的标签类型
const getTypeTagType = (type) => {
  const map = {
    emergency_help: 'danger',
    appointment: 'primary',
    evaluation: 'warning'
  }
  return map[type] || 'info'
}

// 获取通知类型文本
const getTypeText = (type) => {
  const map = {
    emergency_help: '紧急求助',
    appointment: '服务预约',
    evaluation: '评价反馈'
  }
  return map[type] || '其他'
}

// 组件加载时获取通知并设置定时刷新
onMounted(() => {
  fetchNotifications()
  // 每30秒刷新一次通知
  refreshInterval = setInterval(() => {
    fetchNotifications()
  }, 30000)
})

// 组件卸载时清除定时器
onBeforeUnmount(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.notification-page {
  padding: 20px;
}

.filter-row {
  margin-bottom: 20px;
}

.filter-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
