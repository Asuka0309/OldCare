<template>
  <div class="page-card">
    <div class="search-bar">
      <el-select v-model="query.residentId" placeholder="按老人筛选" clearable style="width: 200px" @change="resetPageAndLoad">
        <el-option v-for="e in elderlyOptions" :key="e.id" :label="e.name" :value="e.userId" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 160px" @change="resetPageAndLoad">
        <el-option label="待确认" value="待确认" />
        <el-option label="已确认" value="已确认" />
        <el-option label="进行中" value="进行中" />
        <el-option label="已完成" value="已完成" />
        <el-option label="已取消" value="已取消" />
      </el-select>
      <el-button v-if="!isCaregiver" type="primary" :icon="Plus" @click="openForm()">新建预约</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="residentId" label="老人" width="140">
        <template #default="scope">{{ findResidentName(elderlyOptions, scope.row.residentId) }}</template>
      </el-table-column>
      <el-table-column prop="caregiverId" label="员工" width="140">
        <template #default="scope">{{ findCaregiverName(caregiverOptions, scope.row.caregiverId) }}</template>
      </el-table-column>
      <el-table-column prop="serviceId" label="服务" width="160">
        <template #default="scope">{{ findService(scope.row.serviceId) }}</template>
      </el-table-column>
      <el-table-column prop="appointmentDate" label="预约时间" width="180" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="费用" width="100" />
      <el-table-column label="操作" width="240">
        <template #default="scope">
          <el-button size="small" type="primary" link :icon="Edit" @click="openForm(scope.row)">编辑</el-button>
          <el-button size="small" text type="success" @click="confirm(scope.row)" v-if="scope.row.status==='待确认'">确认</el-button>
          <el-button size="small" text type="warning" @click="complete(scope.row)" v-if="scope.row.status==='已确认' && canComplete">完成</el-button>
          <el-button size="small" text type="danger" @click="cancel(scope.row)" v-if="scope.row.status!=='已取消' && scope.row.status!=='已完成'">取消</el-button>
          <el-button size="small" type="danger" link :icon="Delete" v-if="isAdmin" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :current-page="query.current"
        :page-size="query.size"
        :total="total"
        @current-change="(p)=>{query.current=p;load()}"
      />
    </div>
  </div>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑预约' : '新建预约'" width="520px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
      <el-form-item label="老人" prop="residentId">
        <el-select v-model="form.residentId" filterable>
          <el-option v-for="e in elderlyOptions" :key="e.id" :label="e.name" :value="e.userId" />
        </el-select>
      </el-form-item>
      <el-form-item label="员工" prop="caregiverId">
        <el-select v-model="form.caregiverId" filterable>
          <el-option v-for="c in caregiverOptions" :key="c.id" :label="c.name" :value="c.userId" />
        </el-select>
      </el-form-item>
      <el-form-item label="服务" prop="serviceId">
        <el-select v-model="form.serviceId" filterable @change="syncPrice">
          <el-option v-for="s in serviceOptions" :key="s.id" :label="`${s.serviceName} / ￥${s.price}`" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="预约时间" prop="appointmentDate">
        <el-date-picker v-model="form.appointmentDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" />
      </el-form-item>
      <el-form-item label="费用" prop="totalAmount"><el-input v-model="form.totalAmount" disabled /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status">
          <el-option label="待确认" value="待确认" />
          <el-option label="已确认" value="已确认" />
          <el-option label="进行中" value="进行中" />
          <el-option label="已完成" value="已完成" />
          <el-option label="已取消" value="已取消" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" prop="remark"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import appointmentApi from '@/api/appointment'
import { fetchAllElderly } from '../api/elderly'
import { fetchAllCaregivers } from '../api/caregiver'
import { fetchAllServices } from '../api/service'
import { useAuthStore } from '../store/auth'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, residentId: null, status: '' })

const auth = useAuthStore()
const role = computed(() => auth.user?.role)
const isCaregiver = computed(() => role.value === 'caregiver')
const canComplete = computed(() => ['service_provider', 'caregiver'].includes(role.value))
const isAdmin = computed(() => role.value === 'admin')

const elderlyOptions = ref([])
const caregiverOptions = ref([])
const serviceOptions = ref([])

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  residentId: null,
  caregiverId: null,
  serviceId: null,
  appointmentDate: '',
  status: '待确认',
  totalAmount: 0,
  remark: ''
})

const rules = {
  residentId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  caregiverId: [{ required: true, message: '请选择护工', trigger: 'change' }],
  serviceId: [{ required: true, message: '请选择服务', trigger: 'change' }],
  appointmentDate: [{ required: true, message: '请选择时间', trigger: 'change' }]
}

function resetForm(row) {
  Object.assign(form, {
    id: row?.id || null,
    residentId: row?.residentId || null,
    caregiverId: row?.caregiverId || null,
    serviceId: row?.serviceId || null,
    appointmentDate: row?.appointmentDate || '',
    status: row?.status || '待确认',
    totalAmount: row?.totalAmount || 0,
    remark: row?.remark || ''
  })
}

function statusType(status) {
  switch (status) {
    case '待确认':
      return 'warning'
    case '已确认':
      return 'info'
    case '进行中':
      return 'primary'
    case '已完成':
      return 'success'
    case '已取消':
      return 'danger'
    default:
      return 'info'
  }
}

function findName(listData, id) {
  // 保留旧函数以避免潜在引用，但不再用于渲染
  const item = listData.find((i) => i.id === id)
  return item ? item.name : '-'
}

// 居民名称匹配：用 users.id（residentId）匹配 elderlyOptions.userId
function findResidentName(listData, userId) {
  const item = listData.find((i) => i.userId === userId)
  return item ? item.name : '-'
}

// 员工名称匹配：appointment.caregiverId 现在存的是 users.id
// 需用 caregiverOptions.userId 来匹配显示名称
function findCaregiverName(listData, userId) {
  const item = listData.find((i) => i.userId === userId)
  return item ? item.name : '-'
}

function findService(id) {
  const item = serviceOptions.value.find((i) => i.id === id)
  return item ? item.serviceName : '-'
}

function syncPrice() {
  const item = serviceOptions.value.find((i) => i.id === form.serviceId)
  if (item) {
    form.totalAmount = parseFloat(item.price) || 0
  }
}

async function loadRefs() {
  const [elderly, caregivers, services] = await Promise.all([fetchAllElderly(), fetchAllCaregivers(), fetchAllServices()])
  elderlyOptions.value = elderly || []
  caregiverOptions.value = caregivers || []
  serviceOptions.value = services || []
}

async function load() {
  loading.value = true
  try {
    const data = await appointmentApi.list(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function resetPageAndLoad() {
  query.current = 1
  load()
}

function openForm(row) {
  if (!row && isCaregiver.value) {
    ElMessage.warning('护工无需新建预约')
    return
  }
  resetForm(row)
  dialogVisible.value = true
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请填写所有必填项')
      return
    }
    try {
      const payload = {
        ...form,
        totalAmount: parseFloat(form.totalAmount) || 0
      }
      if (payload.id) {
        await appointmentApi.update(payload)
        ElMessage.success('已更新')
      } else {
        await appointmentApi.create(payload)
        ElMessage.success('已创建')
      }
      dialogVisible.value = false
      load()
    } catch (err) {
      ElMessage.error(err.message || '保存失败')
      console.error('Submit error:', err)
    }
  })
}

function cancel(row) {
  ElMessageBox.confirm('确认取消该预约？', '提示', { type: 'warning' })
    .then(async () => {
      await appointmentApi.cancel(row.id)
      ElMessage.success('已取消')
      load()
    })
    .catch(() => {})
}

async function confirm(row) {
  await appointmentApi.confirm(row.id)
  ElMessage.success('已确认')
  load()
}

async function complete(row) {
  if (!canComplete.value) {
    ElMessage.warning('仅服务提供商或护工可完成')
    return
  }
  await appointmentApi.complete(row.id)
  ElMessage.success('已完成')
  load()
}

function remove(row) {
  ElMessageBox.confirm('确认删除该预约记录？', '提示', { type: 'warning' })
    .then(async () => {
      await appointmentApi.delete(row.id)
      ElMessage.success('已删除')
      load()
    })
    .catch(() => {})
}

onMounted(async () => {
  await loadRefs()
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
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.pager {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
