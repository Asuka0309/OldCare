<template>
  <div class="page-card">
    <div class="search-bar">
      <el-select v-model="query.residentId" placeholder="按居民筛选" clearable style="width: 220px" @change="resetPageAndLoad">
        <el-option v-for="e in elderlyOptions" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="支付状态" clearable style="width: 160px" @change="resetPageAndLoad">
        <el-option label="未支付" value="未支付" />
        <el-option label="已支付" value="已支付" />
        <el-option label="退款申请中" value="退款申请中" />
        <el-option label="已退款" value="已退款" />
      </el-select>
    </div>

    <el-table :data="list" stripe v-loading="loading" size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="appointmentId" label="预约ID" width="100" />
      <el-table-column prop="serviceName" label="服务名称" width="150">
        <template #default="scope">{{ scope.row.serviceName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="residentId" label="居民" width="150">
        <template #default="scope">{{ scope.row.residentName || findName(scope.row.residentId || scope.row.elderlyId) || '-' }}</template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="100" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentTime" label="支付时间" width="180" />
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="360">
        <template #default="scope">
          <!-- 管理员可以审批确认支付 -->
          <el-button size="small" text type="success" @click="pay(scope.row)" v-if="isAdmin && scope.row.status==='未支付'">标记支付</el-button>
          <!-- 管理员可以审批退款申请 -->
          <el-button size="small" text type="success" @click="approveRefund(scope.row, true)" v-if="isAdmin && scope.row.status==='退款申请中'">同意退款</el-button>
          <el-button size="small" text type="danger" @click="approveRefund(scope.row, false)" v-if="isAdmin && scope.row.status==='退款申请中'">拒绝退款</el-button>
          <!-- 居民可以申请退款自己的费用 -->
          <el-button size="small" text type="warning" @click="refund(scope.row)" v-if="isResident && scope.row.status==='已支付' && canRefund(scope.row)">申请退款</el-button>
          <!-- 护工只能查看，不显示操作按钮 -->
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

  <el-dialog v-model="dialogVisible" title="新增费用" width="480px">
    <el-alert 
      title="提示" 
      type="info" 
      description="费用记录应该通过完成服务预约来自动生成。如需手动添加，请确保输入的是已存在的预约ID。" 
      :closable="false"
      style="margin-bottom: 16px"
    />
    <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
      <el-form-item label="预约ID" prop="appointmentId">
        <el-input-number v-model="form.appointmentId" :min="1" placeholder="请输入已存在的预约ID" />
      </el-form-item>
      <el-form-item label="居民" prop="residentId">
        <el-select v-model="form.residentId" filterable>
          <el-option v-for="e in elderlyOptions" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="金额" prop="amount"><el-input-number v-model="form.amount" :min="0.01" :step="10" /></el-form-item>
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
import { fetchFeeRecords, createFeeRecord, payFeeRecord, refundFeeRecord, approveRefund as approveRefundApi } from '../api/feeRecord'
import { fetchAllElderly } from '../api/elderly'
import { fetchUsers } from '../api/user'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const role = computed(() => authStore.user?.role)
const isAdmin = computed(() => role.value === 'admin')
const isCaregiver = computed(() => role.value === 'caregiver')
const isResident = computed(() => role.value === 'resident')
const currentUserId = computed(() => authStore.user?.id)

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, residentId: null, status: '' })

const elderlyOptions = ref([])
const userOptions = ref([])

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ appointmentId: null, residentId: null, amount: 0 })

const rules = {
  appointmentId: [{ required: true, message: '请输入预约ID', trigger: 'blur' }],
  residentId: [{ required: true, message: '请选择居民', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

function statusType(status) {
  if (status === '已支付') return 'success'
  if (status === '已退款') return 'info'
  if (status === '退款申请中') return 'warning'
  return ''
}

function canRefund(row) {
  // 检查是否是当前用户的费用记录
  // residentId 或 elderlyId 等于当前用户ID
  return row.residentId === currentUserId.value || row.elderlyId === currentUserId.value
}

function findName(id) {
  if (!id) return '-'
  
  // 先尝试在elderly_info中查找（旧数据格式：residentId是elderly_info表的ID）
  const elderlyItem = elderlyOptions.value.find((i) => i.id === id)
  if (elderlyItem) {
    return elderlyItem.name
  }
  
  // 如果找不到，尝试在users中查找（新数据格式：residentId是users表的ID）
  const userItem = userOptions.value.find((i) => i.id === id)
  if (userItem) {
    return userItem.real_name || userItem.name || '-'
  }
  
  // 如果还是找不到，可能是通过elderly_info的user_id关联的
  // 查找elderly_info中user_id等于该ID的记录
  const elderlyByUserId = elderlyOptions.value.find((i) => i.user_id === id)
  if (elderlyByUserId) {
    return elderlyByUserId.name
  }
  
  return '-'
}

async function load() {
  loading.value = true
  try {
    const data = await fetchFeeRecords(query)
    list.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '加载费用记录失败')
  } finally {
    loading.value = false
  }
}

function resetPageAndLoad() {
  query.current = 1
  load()
}

function openForm() {
  Object.assign(form, { appointmentId: null, residentId: null, amount: 0 })
  dialogVisible.value = true
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await createFeeRecord({ ...form })
      ElMessage.success('费用记录已创建')
      dialogVisible.value = false
      load()
    } catch (error) {
      ElMessage.error(error.message || '创建失败')
    }
  })
}

async function pay(row) {
  try {
    await payFeeRecord(row.id)
    ElMessage.success('已标记为已支付')
    load()
  } catch (error) {
    ElMessage.error(error.message || '支付失败')
  }
}

async function refund(row) {
  try {
    await ElMessageBox.confirm(
      '确定要申请退款吗？申请后需要等待管理员审批。',
      '确认申请退款',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await refundFeeRecord(row.id)
    ElMessage.success('退款申请已提交，等待管理员审批')
    load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '申请退款失败')
    }
  }
}

async function approveRefund(row, approved) {
  try {
    await ElMessageBox.confirm(
      approved ? '确定同意该退款申请吗？退款后状态将变为"已退款"。' : '确定拒绝该退款申请吗？拒绝后状态将恢复为"已支付"。',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: approved ? 'success' : 'warning'
      }
    )
    await approveRefundApi(row.id, approved)
    ElMessage.success(approved ? '退款审批通过，已退款' : '退款申请已拒绝，状态恢复为已支付')
    load()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审批失败')
    }
  }
}

onMounted(async () => {
  // 同时加载老人信息和用户列表，以支持两种数据格式
  const [elderly, usersData] = await Promise.all([
    fetchAllElderly(),
    fetchUsers({ role: 'resident', size: 999 })
  ])
  elderlyOptions.value = elderly || []
  userOptions.value = usersData?.records || usersData || []
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
