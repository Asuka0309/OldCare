<template>
  <div class="service-need-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">服务需求管理</span>
          <el-button type="primary" @click="handleAdd">
            <i class="el-icon-plus"></i>
            发布需求
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-select v-model="filters.serviceType" placeholder="请选择服务类型" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="清洁卫生" value="cleaning"></el-option>
            <el-option label="陪护照料" value="care"></el-option>
            <el-option label="医疗护理" value="medical"></el-option>
            <el-option label="家务维修" value="repair"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="待报价" value="pending"></el-option>
            <el-option label="已接受" value="accepted"></el-option>
            <el-option label="已完成" value="completed"></el-option>
          </el-select>
        </el-col>
        <el-col :span="12" class="text-right">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-col>
      </el-row>

      <!-- 需求列表 -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="需求标题" min-width="150"></el-table-column>
        <el-table-column prop="serviceType" label="服务类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.serviceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="budget" label="预算" width="100">
          <template #default="{ row }">
            <span>¥{{ row.budget }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requiredDate" label="需求日期" width="120" :formatter="formatDate"></el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'pending'" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'accepted'" link type="primary" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 'pending'" link type="danger" @click="handleDelete(row)">删除</el-button>
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
      :title="isEdit ? '编辑服务需求' : '发布服务需求'"
      width="600px"
    >
      <el-form v-if="formData" ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="需求标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入需求标题"></el-input>
        </el-form-item>
        <el-form-item label="服务类型" prop="serviceType">
          <el-select v-model="formData.serviceType" placeholder="请选择服务类型">
            <el-option label="清洁卫生" value="cleaning"></el-option>
            <el-option label="陪护照料" value="care"></el-option>
            <el-option label="医疗护理" value="medical"></el-option>
            <el-option label="家务维修" value="repair"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预算（元）" prop="budget">
          <el-input-number v-model="formData.budget" :min="0" :max="999999"></el-input-number>
        </el-form-item>
        <el-form-item label="需求日期" prop="requiredDate">
          <el-date-picker v-model="formData.requiredDate" type="date" placeholder="选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入服务位置"></el-input>
        </el-form-item>
        <el-form-item label="需求描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述服务需求"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="服务需求详情" width="600px">
      <el-descriptions v-if="viewData" :column="1" border>
        <el-descriptions-item label="需求标题">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="服务类型">
          <el-tag>{{ getTypeLabel(viewData.serviceType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预算">¥{{ viewData.budget }}</el-descriptions-item>
        <el-descriptions-item label="需求日期">{{ formatDate(null, null, viewData.requiredDate) }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ viewData.location }}</el-descriptions-item>
        <el-descriptions-item label="需求描述">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(viewData.status)">{{ getStatusLabel(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(null, null, viewData.createdTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyServiceNeeds, createServiceNeed, updateServiceNeed, deleteServiceNeed, completeServiceNeed } from '@/api/serviceNeed'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  serviceType: '',
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const formData = ref({
  id: null,
  title: '',
  serviceType: '',
  description: '',
  budget: 0,
  requiredDate: null,
  location: ''
})

const rules = {
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  serviceType: [{ required: true, message: '请选择服务类型', trigger: 'change' }],
  budget: [{ required: true, message: '请输入预算', trigger: 'blur' }],
  requiredDate: [{ required: true, message: '请选择需求日期', trigger: 'change' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  description: [{ required: true, message: '请输入需求描述', trigger: 'blur' }]
}

const viewDialogVisible = ref(false)
const viewData = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      serviceType: filters.serviceType,
      status: filters.status
    }

    const response = await getMyServiceNeeds(params)
    console.log('Fetch response:', response)
    if (response && response.records) {
      tableData.value = response.records || []
      total.value = response.total || 0
    } else {
      console.warn('Unexpected response structure:', response)
    }
  } catch (error) {
    console.error('Fetch error:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = {
    id: null,
    title: '',
    serviceType: '',
    description: '',
    budget: 0,
    requiredDate: null,
    location: ''
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

const handleSave = () => {
  if (!formRef.value) return

  formRef.value.validate((valid) => {
    if (!valid) {
      console.log('Form validation failed')
      ElMessage.error('请填写所有必填项')
      return
    }

    submitForm()
  })
}

const submitForm = async () => {
  try {
    console.log('Submitting form data:', formData.value)
    if (isEdit.value) {
      const response = await updateServiceNeed(formData.value.id, formData.value)
      console.log('Update response:', response)
      ElMessage.success('更新成功')
    } else {
      const response = await createServiceNeed(formData.value)
      console.log('Create response:', response)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('Save error:', error)
    ElMessage.error(error?.message || '操作失败')
  }
}

const handleComplete = async (row) => {
  ElMessageBox.confirm(
    `确定完成此需求吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await completeServiceNeed(row.id)
      ElMessage.success('已完成')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除此需求吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteServiceNeed(row.id)
      ElMessage.success('删除成功')
      // 从前端列表中移除该条记录
      const index = tableData.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        tableData.value.splice(index, 1)
        total.value = Math.max(0, total.value - 1)
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleReset = () => {
  filters.serviceType = ''
  filters.status = ''
  fetchData()
}

const getTypeLabel = (type) => {
  const map = {
    cleaning: '清洁卫生',
    care: '陪护照料',
    medical: '医疗护理',
    repair: '家务维修',
    other: '其他'
  }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = {
    pending: '待报价',
    accepted: '已接受',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

const getStatusColor = (status) => {
  const map = {
    pending: 'warning',
    accepted: 'success',
    completed: '',
    cancelled: 'info'
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
.service-need-container {
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
