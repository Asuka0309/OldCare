<template>
  <div class="page-card">
    <div class="search-bar">
      <el-select v-model="query.appointmentId" placeholder="按预约筛选" clearable style="width: 220px" @change="resetPageAndLoad">
        <el-option v-for="a in appointments" :key="a.id" :label="`预约#${a.id} - ${findResidentName(a.residentId)}`" :value="a.id" />
      </el-select>
      <el-checkbox v-model="query.isComplaint" label="仅显示投诉" @change="resetPageAndLoad" />
      <el-button v-if="isResident" type="primary" :icon="Plus" @click="openForm()">新增评价</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" size="small">
      <el-table-column prop="appointmentId" label="预约ID" width="100" />
      <el-table-column prop="residentId" label="居民" width="150">
        <template #default="scope">{{ findResidentName(scope.row.residentId) }}</template>
      </el-table-column>
      <el-table-column prop="overallRating" label="总体评分" width="120">
        <template #default="scope">
          <el-rate v-model="scope.row.overallRating" disabled allow-half />
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="评价内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="isComplaint" label="是否投诉" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isComplaint ? 'danger' : 'success'">
            {{ scope.row.isComplaint ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === '已评价' ? 'success' : 'info'">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <!-- 只有居民可以编辑评价 -->
          <el-button 
            v-if="isResident" 
            size="small" 
            type="primary" 
            link 
            :icon="Edit" 
            @click="openForm(scope.row)"
          >
            编辑
          </el-button>
          <!-- 护工不能删除评价，管理员可以删除所有评价，居民可以删除自己的评价（后端会验证） -->
          <el-button 
            v-if="isAdmin || isResident" 
            size="small" 
            type="danger" 
            link 
            :icon="Delete" 
            @click="remove(scope.row)"
          >
            删除
          </el-button>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑评价' : '新增评价'" width="600px">
    <el-alert 
      title="提示" 
      type="info" 
      description="选择一个已完成的预约来进行评价。评价信息包括服务质量、态度、效果等方面。" 
      :closable="false"
      style="margin-bottom: 16px"
    />
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <!-- 只有居民可以创建评价，不需要选择老人 -->
      <el-form-item label="预约" prop="appointmentId">
        <el-select v-model="form.appointmentId" :disabled="!!form.id" placeholder="请选择预约" @change="onAppointmentChange" filterable>
          <el-option 
            v-for="a in filteredAppointments" 
            :key="a.id" 
            :label="formatAppointmentLabel(a)" 
            :value="a.id" 
          />
        </el-select>
      </el-form-item>
      <el-form-item label="服务质量评分" prop="serviceQualityRating">
        <el-rate v-model="form.serviceQualityRating" allow-half />
      </el-form-item>
      <el-form-item label="服务态度评分" prop="attitudeRating">
        <el-rate v-model="form.attitudeRating" allow-half />
      </el-form-item>
      <el-form-item label="服务效果评分" prop="effectRating">
        <el-rate v-model="form.effectRating" allow-half />
      </el-form-item>
      <el-form-item label="总体评分" prop="overallRating">
        <el-rate v-model="form.overallRating" allow-half />
      </el-form-item>
      <el-form-item label="评价内容" prop="comment">
        <el-input v-model="form.comment" type="textarea" :rows="4" placeholder="请输入您的评价意见" />
      </el-form-item>
      <el-form-item label="是否投诉" prop="isComplaint">
        <el-switch v-model="form.isComplaint" />
      </el-form-item>
      <el-form-item v-if="form.isComplaint" label="投诉原因" prop="complaintReason">
        <el-input v-model="form.complaintReason" type="textarea" :rows="3" placeholder="请详细描述投诉原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import api from '@/api/evaluation'
import appointmentApi from '@/api/appointment'
import { fetchAllElderly } from '@/api/elderly'
import { fetchAllServices } from '@/api/service'
import { fetchUsers } from '@/api/user'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
const role = computed(() => authStore.user?.role)
const currentUserId = computed(() => authStore.user?.id)
const isResident = computed(() => role.value === 'resident')
const isAdmin = computed(() => role.value === 'admin')
const isCaregiver = computed(() => role.value === 'caregiver')

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref(null)

const list = ref([])
const total = ref(0)
const appointments = ref([])
const filteredAppointments = ref([]) // 过滤后的预约列表（仅显示当前居民对应的预约）
const serviceOptions = ref([]) // 服务选项（用于显示服务名称）
const elderlyOptions = ref([]) // 老人选项（用于显示居民姓名）
const userOptions = ref([]) // 用户选项（用于显示居民姓名）

const query = ref({
  appointmentId: undefined,
  isComplaint: false,
  current: 1,
  size: 10
})

const form = ref({
  id: undefined,
  appointmentId: undefined,
  residentId: undefined,
  providerId: undefined,
  serviceQualityRating: 3,
  attitudeRating: 3,
  effectRating: 3,
  overallRating: 3,
  comment: undefined,
  isComplaint: false,
  complaintReason: undefined
})

const rules = {
  appointmentId: [{ required: true, message: '预约不能为空', trigger: 'change' }],
  overallRating: [{ required: true, type: 'number', message: '总体评分不能为空', trigger: 'change' }],
  comment: [{ required: true, message: '评价内容不能为空', trigger: 'blur' }]
}

const resetPageAndLoad = () => {
  query.value.current = 1
  load()
}

const findResidentName = (id) => {
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

// 格式化预约显示标签：显示预约ID、服务名称、预约时间
const formatAppointmentLabel = (appointment) => {
  const serviceName = findServiceName(appointment.serviceId)
  const dateStr = appointment.appointmentDate ? new Date(appointment.appointmentDate).toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }) : ''
  return `预约#${appointment.id} - ${serviceName || '未知服务'}${dateStr ? ' - ' + dateStr : ''}`
}

// 查找服务名称
const findServiceName = (serviceId) => {
  if (!serviceId) return '未知服务'
  const service = serviceOptions.value.find(s => s.id === serviceId)
  return service ? service.serviceName : `服务#${serviceId}`
}

const loadAppointments = async () => {
  try {
    console.log('Loading completed appointments...')
    
    // 只有居民可以创建评价，所以只加载居民的已完成预约
    let queryParams = { status: '已完成', size: 999 }
    
    if (isResident.value && currentUserId.value) {
      // 居民：先查找对应的elderly_info，使用elderly_info的ID查询预约
      // 因为预约表的resident_id存储的是elderly_info表的ID
      try {
        const elderlyList = await fetchAllElderly()
        const myElderly = elderlyList.find(e => e.user_id === currentUserId.value)
        if (myElderly && myElderly.id) {
          queryParams.elderlyId = myElderly.id
          console.log('居民查询预约：使用elderly_info.id=', myElderly.id)
        } else {
          // 如果找不到elderly_info，尝试直接使用userId（可能预约表存储的是users表的ID）
          queryParams.elderlyId = currentUserId.value
          console.log('居民查询预约：未找到elderly_info，使用userId=', currentUserId.value)
        }
      } catch (e) {
        console.error('查找老人信息失败:', e)
        // 如果查找失败，直接使用userId
        queryParams.elderlyId = currentUserId.value
      }
    } else {
      // 非居民用户（管理员/护工）不能创建评价，不需要加载预约
      appointments.value = []
      filteredAppointments.value = []
      return
    }
    
    let res = await appointmentApi.list(queryParams)
    console.log('Completed appointments response:', res)
    let appts = res.records || res.data || []
    
    appointments.value = appts
    console.log('Loaded appointments count:', appointments.value.length)
    
    // 过滤预约（居民只能看到自己的预约）
    await filterAppointments()
    
    console.log('Loaded appointments:', appointments.value)
  } catch (e) {
    console.error('加载预约失败:', e)
    ElMessage.error('加载预约失败: ' + (e.message || e))
  }
}

// 过滤预约列表，只显示当前居民对应的预约
const filterAppointments = async () => {
  if (isResident.value && currentUserId.value) {
    // 居民：只显示自己的预约
    // 需要先查找对应的elderly_info的ID，然后匹配预约
    try {
      const elderlyList = await fetchAllElderly()
      const myElderly = elderlyList.find(e => e.user_id === currentUserId.value)
      
      if (myElderly && myElderly.id) {
        // 找到了对应的elderly_info，使用elderly_info的ID匹配
        // 预约表的resident_id存储的是elderly_info表的ID
        filteredAppointments.value = appointments.value.filter(a => {
          const appointmentElderlyId = a.elderlyId || a.residentId
          return appointmentElderlyId === myElderly.id
        })
        console.log('居民过滤预约：使用elderly_info.id=', myElderly.id, '匹配到', filteredAppointments.value.length, '条预约')
      } else {
        // 如果找不到elderly_info，尝试使用userId匹配（可能预约表存储的是users表的ID）
        filteredAppointments.value = appointments.value.filter(a => {
          const appointmentElderlyId = a.elderlyId || a.residentId
          return appointmentElderlyId === currentUserId.value
        })
        console.log('居民过滤预约：使用userId=', currentUserId.value, '匹配到', filteredAppointments.value.length, '条预约')
      }
    } catch (e) {
      console.error('过滤预约失败:', e)
      // 如果查找失败，直接使用userId匹配
      filteredAppointments.value = appointments.value.filter(a => {
        const elderlyId = a.elderlyId || a.residentId
        return elderlyId === currentUserId.value
      })
    }
  } else {
    // 非居民用户不能创建评价，不需要过滤
    filteredAppointments.value = []
  }
}

// 注意：已移除加载老人选项的功能，因为只有居民可以创建评价

// 加载服务选项（用于显示服务名称）
const loadServiceOptions = async () => {
  try {
    serviceOptions.value = await fetchAllServices()
  } catch (e) {
    console.error('加载服务列表失败:', e)
  }
}

// 注意：已移除选择老人的功能，因为只有居民可以创建评价

const load = async () => {
  loading.value = true
  try {
    console.log('Loading evaluations with query:', query.value)
    // 构建查询参数
    const params = { ...query.value }
    // 删除false值的isComplaint参数
    if (params.isComplaint === false) {
      delete params.isComplaint
    }
    
    const data = await api.list(params)
    console.log('Evaluations response:', data)
    
    // http拦截器已经提取了data字段，所以这里直接使用Page对象
    list.value = data?.records || []
    total.value = data?.total || 0
    
    console.log('Loaded evaluations:', list.value, 'total:', total.value)
  } catch (e) {
    console.error('加载评价失败:', e)
    ElMessage.error('加载失败: ' + (e.message || e))
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const openForm = async (row) => {
  if (row) {
    // 编辑现有评价
    form.value = { ...row }
  } else {
    // 新增评价 - 重置表单（只有居民可以创建）
    if (!isResident.value) {
      ElMessage.warning('只有居民可以创建评价')
      return
    }
    form.value = {
      id: undefined,
      appointmentId: undefined,
      residentId: undefined,
      providerId: undefined,
      serviceQualityRating: 3,
      attitudeRating: 3,
      effectRating: 3,
      overallRating: 3,
      comment: undefined,
      isComplaint: false,
      complaintReason: undefined
    }
    // 自动过滤自己的预约
    await filterAppointments()
  }
  dialogVisible.value = true
}

const onAppointmentChange = () => {
  // 当选择预约时，自动填充居民和服务商信息
  if (form.value.appointmentId) {
    const appt = appointments.value.find(a => a.id === form.value.appointmentId)
    console.log('Selected appointmentId:', form.value.appointmentId)
    console.log('Found appointment:', appt)
    console.log('Appointment fields:', appt ? Object.keys(appt) : 'none')
    console.log('Available appointments:', appointments.value)
    if (appt) {
      console.log('appointment.residentId:', appt.residentId, 'type:', typeof appt.residentId)
      console.log('appointment.providerId:', appt.providerId, 'type:', typeof appt.providerId)
      console.log('appointment.elderlyId:', appt.elderlyId, 'type:', typeof appt.elderlyId)
      console.log('appointment.caregiverId:', appt.caregiverId, 'type:', typeof appt.caregiverId)
      
      // 尝试两种字段名
      form.value.residentId = appt.residentId || appt.elderlyId
      form.value.providerId = appt.providerId || appt.caregiverId
      console.log('After mapping, residentId:', form.value.residentId, 'providerId:', form.value.providerId)
      
      if (!form.value.residentId || !form.value.providerId) {
        ElMessage.warning('预约数据不完整，请刷新后重试')
      }
    } else {
      ElMessage.warning('未找到对应的预约，请刷新后重试')
    }
  }
}

const submit = async () => {
  try {
    // 确保residentId和providerId被填充
    if (form.value.appointmentId && !form.value.residentId) {
      onAppointmentChange()
      console.log('After onAppointmentChange, residentId:', form.value.residentId)
    }
    
    // 检查必填字段
    if (!form.value.appointmentId) {
      ElMessage.warning('请选择预约')
      return
    }
    if (!form.value.residentId) {
      ElMessage.warning('居民ID不能为空，请重新选择预约')
      return
    }
    if (!form.value.providerId) {
      ElMessage.warning('服务商ID不能为空，请重新选择预约')
      return
    }
    
    console.log('Form before validate:', form.value)
    const valid = await formRef.value.validate()
    console.log('Form validation result:', valid)
    if (!valid) {
      ElMessage.warning('请检查表单信息')
      return
    }
    
    const isUpdate = !!form.value.id
    console.log('Submitting evaluation:', form.value, 'isUpdate:', isUpdate)
    const res = await api[isUpdate ? 'update' : 'add'](form.value)
    console.log('Response:', res)
    ElMessage.success(isUpdate ? '更新成功' : '新增成功')
    dialogVisible.value = false
    await load()
  } catch (e) {
    console.error('Submit error:', e)
    ElMessage.error('保存失败: ' + (e.message || e))
  }
}

const remove = async (row) => {
  await ElMessageBox.confirm('确认删除该评价吗？', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  const res = await api.delete(row.id)
  ElMessage.success(res.message || '删除成功')
  load()
}

// Initial load
onMounted(async () => {
  // 加载老人和用户数据，用于显示居民姓名
  try {
    elderlyOptions.value = await fetchAllElderly()
  } catch (e) {
    console.error('加载老人列表失败:', e)
  }
  try {
    const usersData = await fetchUsers({ role: 'resident', size: 999 })
    userOptions.value = usersData?.records || usersData || []
  } catch (e) {
    console.error('加载用户列表失败:', e)
  }
  
  await loadServiceOptions()
  if (isResident.value) {
    await loadAppointments()
  }
  await load()
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
