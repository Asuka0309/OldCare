<template>
  <div class="emergency-help-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">紧急求助</span>
          <!-- 只有居民可以发起求助 -->
          <el-button v-if="!isAdminOrCaregiver" type="danger" @click="handleAdd">
            <i class="el-icon-plus"></i>
            求助
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="8">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="待响应" value="pending"></el-option>
            <el-option label="已响应" value="responding"></el-option>
            <el-option label="已解决" value="resolved"></el-option>
          </el-select>
        </el-col>
        <el-col :span="16" class="text-right">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-col>
      </el-row>

      <!-- 求助列表 -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%; margin-top: 20px">
        <!--ID列在居民视角不展示-->
        <el-table-column prop="helpType" label="求助类型" width="120">
          <template #default="{ row }">
            <el-tag type="danger">{{ getTypeLabel(row.helpType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="求助内容" min-width="180"></el-table-column>
        <el-table-column prop="location" label="位置" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发起时间" width="160" :formatter="formatTime"></el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <!-- 管理员和员工可以响应和解决 -->
            <template v-if="isAdminOrCaregiver">
              <el-button v-if="row.status === 'pending'" link type="primary" @click="handleRespond(row)">响应</el-button>
              <el-button v-if="row.status === 'responding'" link type="success" @click="handleResolve(row)">解决</el-button>
            </template>
            <!-- 居民只能取消和删除自己的求助 -->
            <template v-else>
              <el-button v-if="row.status === 'pending' || row.status === 'responding'" link type="warning" @click="handleCancel(row)">取消</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
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

    <!-- 发起求助对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发起紧急求助"
      width="600px"
    >
      <el-form v-if="formData" ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="求助类型" prop="helpType">
          <el-select v-model="formData.helpType" placeholder="请选择求助类型">
            <el-option label="医疗急救" value="medical"></el-option>
            <el-option label="跌倒救助" value="fall"></el-option>
            <el-option label="防盗防火" value="security"></el-option>
            <el-option label="其他紧急" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="求助内容" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述紧急情况"
          ></el-input>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入当前位置"></el-input>
        </el-form-item>
        <el-form-item label="通知的紧急联系人" prop="notifiedContacts">
          <el-input v-model="formData.notifiedContacts" placeholder="请输入需要通知的联系人（多个用逗号分隔）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleSave">立即求助</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="紧急求助详情" width="600px">
      <el-descriptions v-if="viewData" :column="1" border>
        <el-descriptions-item label="求助类型">
          <el-tag type="danger">{{ getTypeLabel(viewData.helpType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="求助内容">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ viewData.location }}</el-descriptions-item>
        <el-descriptions-item label="通知联系人">{{ viewData.notifiedContacts }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(viewData.status)">{{ getStatusLabel(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="响应者">{{ viewData.responderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="响应时间">{{ formatTime({}, viewData.responseTime) }}</el-descriptions-item>
        <el-descriptions-item label="发起时间">{{ formatTime({}, viewData.createdAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 响应求助对话框 -->
    <el-dialog v-model="respondDialogVisible" title="响应紧急求助" width="600px">
      <el-form v-if="respondData" ref="respondFormRef" :model="respondData" :rules="respondRules" label-width="100px">
        <el-form-item label="响应备注" prop="responseNote">
          <el-input
            v-model="respondData.responseNote"
            type="textarea"
            :rows="4"
            placeholder="请输入响应备注"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="respondDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRespondSave">确认响应</el-button>
      </template>
    </el-dialog>

    <!-- 解决求助对话框 -->
    <el-dialog v-model="resolveDialogVisible" title="解决紧急求助" width="600px">
      <el-form v-if="resolveData" ref="resolveFormRef" :model="resolveData" :rules="resolveRules" label-width="100px">
        <el-form-item label="解决备注" prop="responseNote">
          <el-input
            v-model="resolveData.responseNote"
            type="textarea"
            :rows="4"
            placeholder="请输入解决备注"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResolveSave">确认解决</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { getAllEmergencyHelps, getMyEmergencyHelps, createEmergencyHelp, respondEmergencyHelp, resolveEmergencyHelp, deleteEmergencyHelp, cancelEmergencyHelp } from '@/api/emergencyHelp'

const authStore = useAuthStore()
const userRole = computed(() => authStore.user?.role)
const isAdminOrCaregiver = computed(() => {
  const role = userRole.value
  return role === 'admin' || role === 'caregiver'
})

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  status: ''
})

const dialogVisible = ref(false)
const formRef = ref(null)

const formData = ref({
  helpType: '',
  description: '',
  location: '',
  notifiedContacts: ''
})

const rules = {
  helpType: [{ required: true, message: '请选择求助类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入求助内容', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

const viewDialogVisible = ref(false)
const viewData = ref(null)

const respondDialogVisible = ref(false)
const respondFormRef = ref(null)
const respondData = ref({
  id: null,
  responseNote: ''
})
const respondRules = {
  responseNote: [{ required: true, message: '请输入响应备注', trigger: 'blur' }]
}

const resolveDialogVisible = ref(false)
const resolveFormRef = ref(null)
const resolveData = ref({
  id: null,
  responseNote: ''
})
const resolveRules = {
  responseNote: [{ required: true, message: '请输入解决备注', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      status: filters.status
    }

    // 管理员和员工查看所有求助，居民只查看自己的求助
    const apiCall = isAdminOrCaregiver.value ? getAllEmergencyHelps : getMyEmergencyHelps
    const response = await apiCall(params)
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
  formData.value = {
    helpType: '',
    description: '',
    location: '',
    notifiedContacts: ''
  }
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
      await createEmergencyHelp(formData.value)
      ElMessage.success('求助已发送，管理员将尽快响应')
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const handleRespond = (row) => {
  respondData.value = {
    id: row.id,
    responseNote: ''
  }
  respondDialogVisible.value = true
}

const handleRespondSave = async () => {
  if (!respondFormRef.value) return

  await respondFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await respondEmergencyHelp(respondData.value.id, respondData.value)
      ElMessage.success('已响应求助')
      respondDialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const handleResolve = (row) => {
  resolveData.value = {
    id: row.id,
    responseNote: ''
  }
  resolveDialogVisible.value = true
}

const handleResolveSave = async () => {
  if (!resolveFormRef.value) return

  await resolveFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      await resolveEmergencyHelp(resolveData.value.id, resolveData.value)
      ElMessage.success('已解决求助')
      resolveDialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const handleDelete = (row) => {
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
      await deleteEmergencyHelp(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定取消该求助吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await cancelEmergencyHelp(row.id)
      ElMessage.success('已取消')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }).catch(() => {})
}

const handleReset = () => {
  filters.status = ''
  fetchData()
}

const getTypeLabel = (type) => {
  const map = {
    medical: '医疗急救',
    fall: '跌倒救助',
    security: '防盗防火',
    other: '其他紧急'
  }
  return map[type] || type
}

const getStatusLabel = (status) => {
  const map = {
    pending: '待响应',
    responding: '已响应',
    resolved: '已解决',
    cancelled: '已取消'
  }
  return map[status] || status
}

const getStatusColor = (status) => {
  const map = {
    pending: 'danger',
    responding: 'warning',
    resolved: 'success',
    cancelled: 'info'
  }
  return map[status] || ''
}

const formatTime = (row, column, cellValue) => {
  // el-table 调用传 (row, column, cellValue)，描述卡片直接传值
  const val = cellValue ?? column ?? row
  if (val === null || val === undefined) return '-'
  if (typeof val === 'string' && val.trim() === '') return '-'
  const d = new Date(val)
  return Number.isNaN(d.getTime()) ? '-' : d.toLocaleString('zh-CN')
}

fetchData()
</script>

<style scoped>
.emergency-help-container {
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
