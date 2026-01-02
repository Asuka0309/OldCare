<template>
  <div class="page-card">
    <div class="search-bar">
      <el-input v-model="query.serviceName" placeholder="按服务名搜索" clearable style="width: 220px;" @keyup.enter="load">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" size="small">
      <el-table-column prop="serviceName" label="名称" width="160" />
      <el-table-column prop="serviceType" label="类型" width="140" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="110" />
      <el-table-column prop="durationMinutes" label="时长(分钟)" width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '可用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170">
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑服务' : '新增服务'" width="520px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
      <el-form-item label="名称" prop="serviceName"><el-input v-model="form.serviceName" /></el-form-item>
      <el-form-item label="类型" prop="serviceType"><el-input v-model="form.serviceType" /></el-form-item>
      <el-form-item label="描述" prop="description"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="价格" prop="price"><el-input-number v-model="form.price" :min="0" :step="10" /></el-form-item>
      <el-form-item label="时长(分钟)" prop="durationMinutes"><el-input-number v-model="form.durationMinutes" :min="15" :step="15" /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status">
          <el-option label="可用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
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
import { fetchServices, createService, updateService, deleteService } from '../api/service'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, serviceName: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  serviceName: '',
  serviceType: '',
  description: '',
  price: 50,
  durationMinutes: 60,
  status: 1
})

const rules = {
  serviceName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

function resetForm(row) {
  Object.assign(form, {
    id: row?.id || null,
    serviceName: row?.serviceName || '',
    serviceType: row?.serviceType || '',
    description: row?.description || '',
    price: row?.price || 50,
    durationMinutes: row?.durationMinutes || 60,
    status: typeof row?.status === 'number' ? row.status : 1
  })
}

async function load() {
  loading.value = true
  try {
    const data = await fetchServices(query)
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
      await updateService(payload)
      ElMessage.success('已更新')
    } else {
      await createService(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  })
}

function remove(row) {
  ElMessageBox.confirm(`确认删除 ${row.serviceName} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteService(row.id)
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
