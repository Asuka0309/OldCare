<template>
  <div class="community-activity-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">社区活动</span>
          <el-button v-if="isAdmin" type="primary" @click="handleAdd">
            <i class="el-icon-plus"></i>
            新建活动
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-select v-model="filters.status" placeholder="请选择活动状态" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="进行中" value="published"></el-option>
            <el-option label="已完成" value="completed"></el-option>
            <el-option label="已取消" value="cancelled"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filters.type" placeholder="请选择活动类型" clearable @change="fetchData">
            <el-option label="讲座" value="lecture"></el-option>
            <el-option label="健身" value="fitness"></el-option>
            <el-option label="娱乐" value="entertainment"></el-option>
            <el-option label="交流" value="exchange"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input v-model="filters.keyword" placeholder="输入标题关键词" clearable @keyup.enter="fetchData" />
        </el-col>
        <el-col :span="6" class="text-right">
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>

      <!-- 活动列表 -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="activityTitle" label="活动标题" min-width="150"></el-table-column>
        <el-table-column prop="activityType" label="活动类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.activityType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="activityDate" label="活动日期" width="120" :formatter="formatDate"></el-table-column>
        <el-table-column prop="maxParticipants" label="最大人数" width="100"></el-table-column>
        <el-table-column prop="currentParticipants" label="已报名" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="isAdmin && row.status === 'published'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="!isAdmin && row.status === 'published' && !isRegistered(row.id)" link type="success" @click="handleRegister(row)">报名</el-button>
            <el-button v-if="!isAdmin && isRegistered(row.id)" link type="danger" @click="handleUnregister(row)">取消报名</el-button>
            <el-button v-if="isAdmin && row.status === 'published'" link type="warning" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="isAdmin && row.status === 'published'" link type="danger" @click="handleCancel(row)">取消</el-button>
            <el-button v-if="isAdmin" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right"
        @pagination="fetchData"
      />
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑活动' : '新建活动'"
      width="600px"
    >
      <el-form v-if="formData" ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="活动标题" prop="activityTitle">
          <el-input v-model="formData.activityTitle" placeholder="请输入活动标题"></el-input>
        </el-form-item>
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="formData.activityType" placeholder="请选择活动类型">
            <el-option label="讲座" value="lecture"></el-option>
            <el-option label="健身" value="fitness"></el-option>
            <el-option label="娱乐" value="entertainment"></el-option>
            <el-option label="交流" value="exchange"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="活动日期" prop="activityDate">
          <el-date-picker v-model="formData.activityDate" type="date" placeholder="选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="活动位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入活动位置"></el-input>
        </el-form-item>
        <el-form-item label="最大参与人数" prop="maxParticipants">
          <el-input-number v-model="formData.maxParticipants" :min="1" :max="999"></el-input-number>
        </el-form-item>
        <el-form-item label="活动描述" prop="activityDescription">
          <el-input
            v-model="formData.activityDescription"
            type="textarea"
            :rows="4"
            placeholder="请详细描述活动内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="活动详情" width="600px">
      <el-descriptions v-if="viewData" :column="1" border>
        <el-descriptions-item label="活动标题">{{ viewData.activityTitle }}</el-descriptions-item>
        <el-descriptions-item label="活动类型">
          <el-tag>{{ getTypeLabel(viewData.activityType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动日期">{{ formatDate({}, viewData.activityDate) }}</el-descriptions-item>
        <el-descriptions-item label="活动位置">{{ viewData.location }}</el-descriptions-item>
        <el-descriptions-item label="最大人数">{{ viewData.maxParticipants }}</el-descriptions-item>
        <el-descriptions-item label="已报名">{{ viewData.currentParticipants }}</el-descriptions-item>
        <el-descriptions-item label="活动描述">{{ viewData.activityDescription }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(viewData.status)">{{ getStatusLabel(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime({}, viewData.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import {
  getAllActivities,
  createActivity,
  updateActivity,
  deleteActivity,
  registerActivity,
  unregisterActivity,
  completeActivity,
  cancelActivity
} from '@/api/communityActivity'

const auth = useAuthStore()
const isAdmin = computed(() => auth.user?.role === 'admin')

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  status: '',
  type: '',
  keyword: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const formData = ref({
  id: null,
  activityTitle: '',
  activityType: '',
  activityDate: null,
  location: '',
  maxParticipants: 50,
  activityDescription: ''
})

const rules = {
  activityTitle: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  activityDate: [{ required: true, message: '请选择活动日期', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动位置', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入最大人数', trigger: 'blur' }],
  activityDescription: [{ required: true, message: '请输入活动描述', trigger: 'blur' }]
}

const viewDialogVisible = ref(false)
const viewData = ref(null)

const registeredActivityIds = ref([])

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      status: filters.status,
      type: filters.type,
      keyword: filters.keyword
    }
    // http拦截器已将 { code, message, data } 解包为 data
    const pageData = await getAllActivities(params)
    tableData.value = pageData?.records || []
    total.value = pageData?.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = {
    id: null,
    activityTitle: '',
    activityType: '',
    activityDate: null,
    location: '',
    maxParticipants: 50,
    activityDescription: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        await updateActivity(formData.value.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createActivity(formData.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const handleRegister = async (row) => {
  ElMessageBox.confirm(
    `确定报名参加此活动吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await registerActivity(row.id)
      ElMessage.success('报名成功')
      registeredActivityIds.value.push(row.id)
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleUnregister = async (row) => {
  ElMessageBox.confirm(
    `确定取消报名吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await unregisterActivity(row.id)
      ElMessage.success('已取消报名')
      registeredActivityIds.value = registeredActivityIds.value.filter(id => id !== row.id)
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleComplete = async (row) => {
  ElMessageBox.confirm(
    `确定完成此活动吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await completeActivity(row.id)
      ElMessage.success('活动已完成')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除此活动吗？删除后不可恢复`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteActivity(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定取消此活动吗？取消后活动状态将变为已取消`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await cancelActivity(row.id)
      ElMessage.success('已取消')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleReset = () => {
  filters.status = ''
  filters.type = ''
  filters.keyword = ''
  fetchData()
}

const isRegistered = (activityId) => {
  return registeredActivityIds.value.includes(activityId)
}

const getTypeLabel = (type) => {
  const map = {
    lecture: '讲座',
    fitness: '健身',
    entertainment: '娱乐',
    exchange: '交流',
    other: '其他'
  }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = {
    published: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

const getStatusColor = (status) => {
  const map = {
    published: 'success',
    completed: 'info',
    cancelled: 'danger'
  }
  return map[status] || ''
}

const formatDate = (row, column, cellValue) => {
  return cellValue ? new Date(cellValue).toLocaleDateString('zh-CN') : '-'
}

const formatTime = (row, column, cellValue) => {
  return cellValue ? new Date(cellValue).toLocaleString('zh-CN') : '-'
}

fetchData()
</script>

<style scoped>
.community-activity-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.search-row {
  margin-bottom: 20px;
}

.text-right {
  text-align: right;
}
</style>
