<template>
  <div class="page-card">
    <div class="search-bar">
      <el-select v-model="query.elderlyId" placeholder="按老人筛选" clearable style="width: 220px" @change="resetPageAndLoad">
        <el-option v-for="e in elderlyOptions" :key="e.id" :label="e.name" :value="e.id" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 320px"
        @change="resetPageAndLoad"
      />
      <el-button v-if="!isResident" type="primary" :icon="Plus" @click="openForm()">新增检查</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" size="small">
      <el-table-column prop="elderlyId" label="老人" width="150">
        <template #default="scope">{{ findName(scope.row.elderlyId) }}</template>
      </el-table-column>
      <el-table-column prop="checkDate" label="检查时间" width="190" />
      <el-table-column prop="checkType" label="检查类型" width="120" />
      <el-table-column prop="checkResult" label="检查结果" width="150" />
      <el-table-column prop="normalStatus" label="是否正常" width="120">
        <template #default="scope">{{ formatStatus(scope.row.normalStatus) }}</template>
      </el-table-column>
      <el-table-column prop="notes" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="160">
        <template #default="scope">
          <el-button v-if="isAdmin" size="small" type="primary" link :icon="Edit" @click="openForm(scope.row)">编辑</el-button>
          <el-button v-if="isAdmin" size="small" type="danger" link :icon="Delete" @click="remove(scope.row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑检查' : '新增检查'" width="520px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
      <el-form-item label="老人" prop="elderlyId">
        <el-select v-model="form.elderlyId" filterable>
          <el-option v-for="e in elderlyOptions" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="检查时间" prop="checkDate">
        <el-date-picker v-model="form.checkDate" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" />
      </el-form-item>
      <el-form-item label="检查类型" prop="checkType"><el-input v-model="form.checkType" placeholder="如 blood_pressure(血压)" /></el-form-item>
      <el-form-item label="检查结果" prop="checkResult"><el-input v-model="form.checkResult" placeholder="输入检查结果" /></el-form-item>
      <el-form-item label="是否正常" prop="normalStatus">
        <el-select v-model="form.normalStatus">
          <el-option label="正常" value="normal" />
          <el-option label="异常" value="abnormal" />
        </el-select>
      </el-form-item>
      <el-form-item label="体重" prop="weight"><el-input-number v-model="form.weight" :min="0" :step="0.1" /></el-form-item>
      <el-form-item label="备注" prop="notes"><el-input v-model="form.notes" type="textarea" :rows="2" /></el-form-item>
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
import { fetchHealthChecks, createHealthCheck, updateHealthCheck, deleteHealthCheck } from '../api/healthCheck'
import { fetchAllElderly } from '../api/elderly'
import { useAuthStore } from '../store/auth'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, elderlyId: null, startDate: null, endDate: null })
const dateRange = ref(null)
const auth = useAuthStore()
auth.initialized || auth.hydrate()
function normRole(r) {
  return String(r || '').toUpperCase()
}
const isAdmin = computed(() => ['ADMIN','SYSTEM_ADMIN','ADMINISTRATOR','ROLE_ADMIN'].includes(normRole(auth.user?.role)))
const isCaregiver = computed(() => ['CAREGIVER','ROLE_CAREGIVER','GIVER'].includes(normRole(auth.user?.role)))
const isResident = computed(() => ['RESIDENT','ROLE_RESIDENT','USER'].includes(normRole(auth.user?.role)))

const elderlyOptions = ref([])

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  elderlyId: null,
  checkDate: '',
  checkType: '',
  checkResult: '',
  normalStatus: 'normal',
  notes: ''
})

const rules = {
  elderlyId: [{ required: true, message: '请选择老人', trigger: 'change' }],
  checkDate: [{ required: true, message: '请选择时间', trigger: 'change' }],
  checkType: [{ required: true, message: '请输入检查类型', trigger: 'blur' }],
  checkResult: [{ required: true, message: '请输入检查结果', trigger: 'blur' }],
  normalStatus: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

function findName(id) {
  const item = elderlyOptions.value.find((i) => i.id === id)
  return item ? item.name : '-'
}

function formatStatus(status) {
  const statusMap = {
    'normal': '正常',
    'abnormal': '异常'
  }
  return statusMap[status] || status
}

function resetForm(row) {
  Object.assign(form, {
    id: row?.id || null,
    elderlyId: row?.elderlyId || null,
    checkDate: row?.checkDate || '',
    checkType: row?.checkType || '',
    checkResult: row?.checkResult || '',
    normalStatus: row?.normalStatus || 'normal',
    notes: row?.notes || ''
  })
}

async function load() {
  loading.value = true
  try {
    const response = await fetchHealthChecks(query)
    list.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败：' + (error.message || error))
    console.error('Load error:', error)
  } finally {
    loading.value = false
  }
}

function resetPageAndLoad() {
  query.current = 1
  if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
    query.startDate = dateRange.value[0]
    query.endDate = dateRange.value[1]
  } else {
    query.startDate = null
    query.endDate = null
  }
  load()
}

function openForm(row) {
  resetForm(row)
  dialogVisible.value = true
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const payload = { ...form }
      if (payload.id) {
        if (!isAdmin.value) {
          ElMessage.error('仅管理员可编辑记录')
          return
        }
        await updateHealthCheck(payload)
        ElMessage.success('已更新')
      } else {
        if (isResident.value) {
          ElMessage.error('居民无权新增记录')
          return
        }
        await createHealthCheck(payload)
        ElMessage.success('已创建')
      }
      dialogVisible.value = false
      load()
    } catch (error) {
      ElMessage.error('保存失败：' + (error.message || error))
      console.error('Submit error:', error)
    }
  })
}

function remove(row) {
  ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteHealthCheck(row.id)
        ElMessage.success('已删除')
        load()
      } catch (error) {
        ElMessage.error('删除失败：' + (error.message || error))
      }
    })
    .catch(() => {})
}

onMounted(async () => {
  elderlyOptions.value = await fetchAllElderly()
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
