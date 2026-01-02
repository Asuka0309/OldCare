<template>
  <div class="quotation-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">报价管理</span>
          <el-button type="primary" @click="handleAdd">
            <i class="el-icon-plus"></i>
            新增报价
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="8">
          <el-select v-model="filters.status" placeholder="请选择状态" clearable @change="fetchData">
            <el-option label="全部" value=""></el-option>
            <el-option label="待处理" value="pending"></el-option>
            <el-option label="已接受" value="accepted"></el-option>
            <el-option label="已拒绝" value="rejected"></el-option>
          </el-select>
        </el-col>
        <el-col :span="16" class="text-right">
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-col>
      </el-row>

      <!-- 报价列表 -->
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="serviceNeedId" label="需求ID" width="100"></el-table-column>
        <el-table-column prop="quotedPrice" label="报价金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.quotedPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="报价描述" min-width="180"></el-table-column>
        <el-table-column prop="validityDays" label="有效期" width="100">
          <template #default="{ row }">
            <span>{{ row.validityDays }} 天</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报价时间" width="160" :formatter="formatTime"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'pending'" link type="primary" @click="handleEdit(row)">编辑</el-button>
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
      :title="isEdit ? '编辑报价' : '新增报价'"
      width="600px"
    >
      <el-form v-if="formData" ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="需求ID" prop="serviceNeedId">
          <el-input-number v-model="formData.serviceNeedId" :min="1" disabled></el-input-number>
        </el-form-item>
        <el-form-item label="报价金额" prop="quotedPrice">
          <el-input-number v-model="formData.quotedPrice" :min="0" :max="999999"></el-input-number>
        </el-form-item>
        <el-form-item label="有效期（天）" prop="validityDays">
          <el-input-number v-model="formData.validityDays" :min="1" :max="365"></el-input-number>
        </el-form-item>
        <el-form-item label="报价描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述报价内容"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="报价详情" width="600px">
      <el-descriptions v-if="viewData" :column="1" border>
        <el-descriptions-item label="需求ID">{{ viewData.serviceNeedId }}</el-descriptions-item>
        <el-descriptions-item label="报价金额">
          <span class="price">¥{{ viewData.quotedPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="有效期">{{ viewData.validityDays }} 天</el-descriptions-item>
        <el-descriptions-item label="报价描述">{{ viewData.description }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusColor(viewData.status)">{{ getStatusLabel(viewData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报价时间">{{ formatTime({}, viewData.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime({}, viewData.updatedAt) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyQuotations, createQuotation, updateQuotation, deleteQuotation } from '@/api/quotation'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const formData = ref({
  id: null,
  serviceNeedId: null,
  quotedPrice: 0,
  validityDays: 30,
  description: ''
})

const rules = {
  serviceNeedId: [{ required: true, message: '请输入需求ID', trigger: 'blur' }],
  quotedPrice: [{ required: true, message: '请输入报价金额', trigger: 'blur' }],
  validityDays: [{ required: true, message: '请输入有效期', trigger: 'blur' }],
  description: [{ required: true, message: '请输入报价描述', trigger: 'blur' }]
}

const viewDialogVisible = ref(false)
const viewData = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      status: filters.status
    }

    const response = await getMyQuotations(params)
    if (response.data?.code === 0) {
      tableData.value = response.data.data?.records || []
      total.value = response.data.data?.total || 0
    }
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
    serviceNeedId: null,
    quotedPrice: 0,
    validityDays: 30,
    description: ''
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
        await updateQuotation(formData.value.id, formData.value)
        ElMessage.success('更新成功')
      } else {
        await createQuotation(formData.value)
        ElMessage.success('报价成功')
      }
      dialogVisible.value = false
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除此报价吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteQuotation(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleReset = () => {
  filters.status = ''
  fetchData()
}

const getStatusLabel = (status) => {
  const map = {
    pending: '待处理',
    accepted: '已接受',
    rejected: '已拒绝'
  }
  return map[status] || status
}

const getStatusColor = (status) => {
  const map = {
    pending: 'warning',
    accepted: 'success',
    rejected: 'danger'
  }
  return map[status] || ''
}

const formatTime = (row, column, cellValue) => {
  return cellValue ? new Date(cellValue).toLocaleString('zh-CN') : '-'
}

fetchData()
</script>

<style scoped>
.quotation-container {
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

.price {
  color: #f56c6c;
  font-weight: bold;
}
</style>
