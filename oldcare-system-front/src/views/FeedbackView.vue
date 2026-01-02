<template>
  <div class="feedback-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">反馈与建议</span>
          <el-button v-if="canSubmit" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            提交反馈
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="待处理" value="pending"></el-option>
            <el-option label="处理中" value="processing"></el-option>
            <el-option label="已处理" value="resolved"></el-option>
          </el-select>
        </el-col>
        <el-col v-if="canViewAll" :span="6">
          <el-select v-model="filters.category" placeholder="请选择类别" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="功能建议" value="suggestion"></el-option>
            <el-option label="问题反馈" value="issue"></el-option>
            <el-option label="投诉" value="complaint"></el-option>
            <el-option label="其他" value="general"></el-option>
          </el-select>
        </el-col>
        <el-col v-if="canViewAll" :span="6">
          <el-select v-model="filters.role" placeholder="请选择角色" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="居民" value="resident"></el-option>
            <el-option label="员工" value="caregiver"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6" class="text-right">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-col>
      </el-row>

      <!-- 反馈列表 -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="150"></el-table-column>
        <el-table-column prop="category" label="类别" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryColor(row.category)">{{ getCategoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canViewAll" prop="userName" label="提交人" width="120"></el-table-column>
        <el-table-column v-if="canViewAll" prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'resident' ? 'success' : 'warning'" size="small">
              {{ row.role === 'resident' ? '居民' : '员工' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="提交时间" width="160" :formatter="formatTime"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="isAdmin && row.status !== 'resolved'"
              link
              type="success"
              @click="handleResolve(row)"
            >处理</el-button>
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
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </el-card>

    <!-- 提交反馈对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="提交反馈"
      width="600px"
    >
      <el-form v-if="formData" ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入反馈标题"></el-input>
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="formData.category" placeholder="请选择类别">
            <el-option label="功能建议" value="suggestion"></el-option>
            <el-option label="问题反馈" value="issue"></el-option>
            <el-option label="投诉" value="complaint"></el-option>
            <el-option label="其他" value="general"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您的反馈内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="反馈详情" width="600px">
      <el-descriptions v-if="viewData" :column="1" border>
        <el-descriptions-item label="标题">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="类别">
          <el-tag :type="getCategoryColor(viewData.category)">{{ getCategoryLabel(viewData.category) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="canViewAll" label="提交人">{{ viewData.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="canViewAll" label="提交人角色">
          <el-tag :type="viewData.role === 'resident' ? 'success' : 'warning'" size="small">
            {{ viewData.role === 'resident' ? '居民' : '员工' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap;">{{ viewData.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(viewData.status)">{{ getStatusLabel(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="viewData.handlerName" label="处理人">{{ viewData.handlerName }}</el-descriptions-item>
        <el-descriptions-item v-if="viewData.remark" label="处理备注">
          <div style="white-space: pre-wrap;">{{ viewData.remark }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ displayDateTime(viewData.createdTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ displayDateTime(viewData.updatedTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理反馈对话框 -->
    <el-dialog v-model="resolveDialogVisible" title="处理反馈" width="500px">
      <el-form v-if="resolveData" ref="resolveFormRef" :model="resolveData" label-width="100px">
        <el-form-item label="处理状态">
          <el-select v-model="resolveData.status">
            <el-option label="处理中" value="processing"></el-option>
            <el-option label="已处理" value="resolved"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="resolveData.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明或回复"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResolveSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { submitFeedback, getMyFeedback, getAllFeedback, getFeedbackById, resolveFeedback, deleteFeedback } from '@/api/feedback'
import { useAuthStore } from '@/store/auth'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const auth = useAuthStore()
const role = computed(() => auth.user?.role)
const isAdmin = computed(() => role.value === 'admin')
const canViewAll = computed(() => role.value === 'admin')
const canSubmit = computed(() => role.value === 'resident' || role.value === 'caregiver' || role.value === 'admin')

const filters = reactive({
  status: '',
  category: '',
  role: ''
})

const dialogVisible = ref(false)
const formRef = ref(null)

const formData = ref({
  title: '',
  category: 'general',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const viewDialogVisible = ref(false)
const viewData = ref(null)

const resolveDialogVisible = ref(false)
const resolveFormRef = ref(null)
const resolveData = ref({
  id: null,
  status: 'resolved',
  remark: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      status: filters.status,
      category: filters.category,
      role: filters.role
    }

    // 管理员调用 getAllFeedback，其他角色调用 getMyFeedback
    const response = canViewAll.value 
      ? await getAllFeedback(params) 
      : await getMyFeedback(params)

    if (response && typeof response === 'object') {
      if (response.records) {
        tableData.value = response.records || []
        total.value = response.total || 0
      } else if (Array.isArray(response)) {
        tableData.value = response
        total.value = response.length
      }
    }
  } catch (error) {
    console.error('Fetch error:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  formData.value = {
    title: '',
    category: 'general',
    content: ''
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }

    try {
      await submitFeedback(formData.value)
      ElMessage.success('提交成功')
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('Submit error:', error)
      ElMessage.error(error?.message || '提交失败')
    }
  })
}

const handleView = async (row) => {
  try {
    const data = await getFeedbackById(row.id)
    viewData.value = data || row
  } catch (error) {
    console.error('Load detail error:', error)
    viewData.value = row
  } finally {
    viewDialogVisible.value = true
  }
}

const handleResolve = (row) => {
  resolveData.value = {
    id: row.id,
    status: 'resolved',
    remark: ''
  }
  resolveDialogVisible.value = true
}

const handleResolveSubmit = async () => {
  try {
    await resolveFeedback(resolveData.value.id, {
      status: resolveData.value.status,
      remark: resolveData.value.remark
    })
    ElMessage.success('处理成功')
    resolveDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('Resolve error:', error)
    ElMessage.error(error?.message || '处理失败')
  }
}

const handleReset = () => {
  filters.status = ''
  filters.category = ''
  filters.role = ''
  fetchData()
}

const getCategoryLabel = (category) => {
  const map = {
    suggestion: '功能建议',
    issue: '问题反馈',
    complaint: '投诉',
    general: '其他'
  }
  return map[category] || category
}

const getCategoryColor = (category) => {
  const map = {
    suggestion: 'success',
    issue: 'warning',
    complaint: 'danger',
    general: 'info'
  }
  return map[category] || ''
}

const getStatusLabel = (status) => {
  const map = {
    pending: '待处理',
    processing: '处理中',
    resolved: '已处理'
  }
  return map[status] || status
}

const getStatusColor = (status) => {
  const map = {
    pending: 'warning',
    processing: 'primary',
    resolved: 'success'
  }
  return map[status] || ''
}

const formatTime = (row, column, cellValue) => {
  if (!cellValue) return '-'
  const value = typeof cellValue === 'string' ? cellValue.replace(' ', 'T') : cellValue
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? '-' : date.toLocaleString('zh-CN')
}

const displayDateTime = (value) => {
  if (!value) return '-'
  const v = typeof value === 'string' ? value.replace(' ', 'T') : value
  const d = new Date(v)
  return Number.isNaN(d.getTime()) ? '-' : d.toLocaleString('zh-CN')
}

fetchData()
</script>

<style scoped>
.feedback-container {
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
