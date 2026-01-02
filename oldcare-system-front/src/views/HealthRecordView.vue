<template>
  <div class="health-record-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">健康记录</span>
          <el-button v-if="canAdd" type="primary" @click="handleAdd">
            <i class="el-icon-plus"></i>
            新增记录
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-select v-model="filters.recordType" placeholder="请选择记录类型" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="检查报告" value="checkup"></el-option>
            <el-option label="用药记录" value="medication"></el-option>
            <el-option label="诊疗记录" value="treatment"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="fetchData"
          />
        </el-col>
        <el-col :span="12" class="text-right">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-col>
      </el-row>

      <!-- 记录列表 -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="记录标题" min-width="150"></el-table-column>
        <el-table-column prop="recordType" label="记录类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.recordType)">{{ getTypeLabel(row.recordType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="记录日期" width="120" :formatter="formatDate"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" :formatter="formatTime"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="canEdit"
              link
              type="primary"
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
              v-if="canDelete"
              link
              type="danger"
              @click="handleDelete(row)"
            >删除</el-button>
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
      :title="isEdit ? '编辑健康记录' : '新增健康记录'"
      width="600px"
    >
      <el-form v-if="formData" ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="记录标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入记录标题"></el-input>
        </el-form-item>
        <el-form-item label="记录类型" prop="recordType">
          <el-select v-model="formData.recordType" placeholder="请选择记录类型">
            <el-option label="检查报告" value="checkup"></el-option>
            <el-option label="用药记录" value="medication"></el-option>
            <el-option label="诊疗记录" value="treatment"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="formData.recordDate" type="date" placeholder="选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="医院" prop="hospital">
          <el-input v-model="formData.hospital" placeholder="请输入医院名称"></el-input>
        </el-form-item>
        <el-form-item label="医生" prop="doctorName">
          <el-input v-model="formData.doctorName" placeholder="请输入医生姓名"></el-input>
        </el-form-item>
        <el-form-item label="记录内容" prop="details">
          <el-input
            v-model="formData.details"
            type="textarea"
            :rows="4"
            placeholder="请输入记录详细内容"
          ></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input
            v-model="formData.notes"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="健康记录详情" width="600px">
      <el-descriptions v-if="viewData" :column="1" border>
        <el-descriptions-item label="记录标题">{{ viewData.title }}</el-descriptions-item>
        <el-descriptions-item label="记录类型">
          <el-tag :type="getTypeColor(viewData.recordType)">{{ getTypeLabel(viewData.recordType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="记录日期">{{ displayDate(viewData.recordDate) }}</el-descriptions-item>
        <el-descriptions-item label="医院">{{ viewData.hospital }}</el-descriptions-item>
        <el-descriptions-item label="记录内容">{{ viewData.details }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ viewData.notes }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime({}, viewData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime({}, viewData.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyHealthRecords, getAllHealthRecords, createHealthRecord, updateHealthRecord, deleteHealthRecord } from '@/api/healthRecord'
import { useAuthStore } from '@/store/auth'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const auth = useAuthStore()
const role = computed(() => auth.user?.role)
const userId = computed(() => auth.user?.id)
const isAdmin = computed(() => role.value === 'admin')
const isCaregiver = computed(() => role.value === 'caregiver')
const isResident = computed(() => role.value === 'resident')
const canAdd = computed(() => isAdmin.value || isCaregiver.value)
const canEdit = computed(() => isAdmin.value || isCaregiver.value)
const canDelete = computed(() => isAdmin.value) // 只有管理员可以删除
const canModify = (row) => {
  // 居民不能编辑和删除
  if (isResident.value) return false
  // 管理员和护工可以编辑所有记录
  return isAdmin.value || isCaregiver.value
}

const filters = reactive({
  recordType: '',
  dateRange: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const formData = ref({
  id: null,
  title: '',
  recordType: '',
  recordDate: null,
  hospital: '',
  doctorName: '',
  details: '',
  notes: ''
})

const rules = {
  title: [{ required: true, message: '请输入记录标题', trigger: 'blur' }],
  recordType: [{ required: true, message: '请选择记录类型', trigger: 'change' }],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
  details: [{ required: true, message: '请输入记录内容', trigger: 'blur' }]
}

const viewDialogVisible = ref(false)
const viewData = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      recordType: filters.recordType
    }
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = formatDateForAPI(filters.dateRange[0])
      params.endDate = formatDateForAPI(filters.dateRange[1])
    }

    // 管理员和护工查看所有记录，居民只查看自己的
    const response = isResident.value ? await getMyHealthRecords(params) : await getAllHealthRecords(params)

    if (response && typeof response === 'object') {
      if (Array.isArray(response)) {
        tableData.value = response
        total.value = response.length
      } else if (response.records) {
        tableData.value = response.records || []
        total.value = response.total || 0
      } else {
        console.warn('Unexpected response structure:', response)
      }
    } else {
      console.warn('Invalid response:', response)
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
    recordType: '',
    recordDate: null,
    hospital: '',
    details: '',
    notes: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!canEdit.value) {
    ElMessage.warning('无权限编辑')
    return
  }
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
    if (!valid) {
      console.log('Form validation failed')
      return
    }

    try {
      console.log('Submitting health record:', formData.value)
      if (isEdit.value) {
        const response = await updateHealthRecord(formData.value.id, formData.value)
        console.log('Update response:', response)
        ElMessage.success('更新成功')
      } else {
        const response = await createHealthRecord(formData.value)
        console.log('Create response:', response)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('Save error:', error)
      ElMessage.error(error?.message || error.response?.data?.message || '操作失败')
    }
  })
}

const handleDelete = (row) => {
  if (!canDelete.value) {
    ElMessage.warning('仅管理员可删除记录')
    return
  }
  ElMessageBox.confirm(
    `确定删除此记录吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteHealthRecord(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleReset = () => {
  filters.recordType = ''
  filters.dateRange = null
  fetchData()
}

const getTypeLabel = (type) => {
  const map = {
    checkup: '检查报告',
    medication: '用药记录',
    treatment: '诊疗记录',
    other: '其他'
  }
  return map[type] || type
}

const getTypeColor = (type) => {
  const map = {
    checkup: 'success',
    medication: 'warning',
    treatment: 'info',
    other: ''
  }
  return map[type] || ''
}

const formatDate = (row, column, cellValue) => {
  const val = cellValue ?? column ?? row
  if (val === null || val === undefined) return '-'
  if (typeof val === 'string' && val.trim() === '') return '-'
  const d = new Date(val)
  return Number.isNaN(d.getTime()) ? '-' : d.toLocaleDateString('zh-CN')
}

const formatTime = (row, column, cellValue) => {
  const val = cellValue ?? column ?? row
  if (val === null || val === undefined) return '-'
  if (typeof val === 'string' && val.trim() === '') return '-'
  const d = new Date(val)
  return Number.isNaN(d.getTime()) ? '-' : d.toLocaleString('zh-CN')
}

const displayDate = (value) => {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'string' && value.trim() === '') return '-'
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '-' : d.toLocaleDateString('zh-CN')
}

const formatDateForAPI = (date) => {
  return date.toISOString().split('T')[0]
}

fetchData()
</script>

<style scoped>
.health-record-container {
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
