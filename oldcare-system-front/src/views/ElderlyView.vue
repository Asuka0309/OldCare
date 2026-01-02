<template>
  <div class="page-card">
    <div class="search-bar">
      <el-input v-model="query.name" placeholder="按姓名搜索" clearable style="width: 240px;" @keyup.enter="load">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增老人</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" style="width: 100%">
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="phone" label="电话" min-width="130" />
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="healthStatus" label="健康状况" min-width="100" />
      <el-table-column prop="medicalHistory" label="病史" min-width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link :icon="Edit" @click="openForm(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" link :icon="Delete" @click="remove(scope.row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑老人' : '新增老人'" width="520px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
      <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="年龄" prop="age"><el-input-number v-model="form.age" :min="0" :max="120" /></el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="form.gender" placeholder="选择">
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="身份证" prop="idCard"><el-input v-model="form.idCard" /></el-form-item>
      <el-form-item label="电话/用户名" prop="phone"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="地址" prop="address"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="紧急联系人" prop="emergencyContact"><el-input v-model="form.emergencyContact" /></el-form-item>
      <el-form-item label="紧急电话" prop="emergencyPhone"><el-input v-model="form.emergencyPhone" /></el-form-item>
      <el-form-item label="健康状况" prop="healthStatus"><el-input v-model="form.healthStatus" /></el-form-item>
      <el-form-item label="病史" prop="medicalHistory"><el-input v-model="form.medicalHistory" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible=false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { fetchElderly, createElderly, updateElderly, deleteElderly } from '../api/elderly'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, name: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  age: 70,
  gender: '女',
  idCard: '',
  phone: '',
  address: '',
  emergencyContact: '',
  emergencyPhone: '',
  healthStatus: '',
  medicalHistory: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话号码', trigger: 'blur' }]
}

function resetForm(row) {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    age: row?.age || 70,
    gender: row?.gender || '女',
    idCard: row?.idCard || '',
    phone: row?.phone || '',
    address: row?.address || '',
    emergencyContact: row?.emergencyContact || '',
    emergencyPhone: row?.emergencyPhone || '',
    healthStatus: row?.healthStatus || '',
    medicalHistory: row?.medicalHistory || ''
  })
}

async function load() {
  loading.value = true
  try {
    const data = await fetchElderly(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  resetForm(row)
  dialogVisible.value = true
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const payload = { ...form }
    if (payload.id) {
      await updateElderly(payload)
      ElMessage.success('已更新')
    } else {
      await createElderly(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  })
}

function remove(row) {
  ElMessageBox.confirm(`确认删除 ${row.name} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteElderly(row.id)
      ElMessage.success('已删除')
      load()
    })
    .catch(() => {})
}

onMounted(load)
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
