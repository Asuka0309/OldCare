<template>
  <div class="page-card">
    <div class="search-bar">
      <el-input v-model="query.username" placeholder="按用户名搜索" clearable style="width: 220px;" @keyup.enter="load">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 160px" @change="resetPageAndLoad">
        <el-option label="管理员" value="admin" />
        <el-option label="护工" value="caregiver" />
        <el-option label="老人家属" value="elderly_family" />
        <el-option label="系统用户" value="system" />
      </el-select>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增用户</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" size="small">
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="真实姓名" width="140" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="scope">
          <el-tag>{{ roleText(scope.row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" type="primary" link :icon="Edit" @click="openForm(scope.row)">编辑</el-button>
          <el-button size="small" text :type="scope.row.status === 1 ? 'danger' : 'success'" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
      <el-form-item label="用户名" prop="username"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
      <el-form-item label="密码" prop="password" v-if="!form.id">
        <el-input v-model="form.password" type="password" placeholder="至少6位" />
      </el-form-item>
      <el-form-item label="真实姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
      <el-form-item label="电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role">
          <el-option label="管理员" value="admin" />
          <el-option label="护工" value="caregiver" />
          <el-option label="老人家属" value="elderly_family" />
          <el-option label="系统用户" value="system" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status">
          <el-option label="正常" :value="1" />
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
import { fetchUsers, createUser, updateUser, deleteUser, updateUserStatus } from '../api/user'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, username: '', role: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: 'elderly_family',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

function roleText(role) {
  const map = { admin: '管理员', caregiver: '护工', elderly_family: '老人家属', system: '系统用户' }
  return map[role] || role
}

function resetForm(row) {
  Object.assign(form, {
    id: row?.id || null,
    username: row?.username || '',
    password: '',
    realName: row?.realName || '',
    phone: row?.phone || '',
    role: row?.role || 'elderly_family',
    status: typeof row?.status === 'number' ? row.status : 1
  })
}

async function load() {
  loading.value = true
  try {
    const params = { ...query }
    // 移除空字符串参数
    if (!params.username) delete params.username
    if (!params.role) delete params.role
    const data = await fetchUsers(params)
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
  resetForm(row)
  dialogVisible.value = true
}

function submit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const payload = { ...form }
    if (payload.id) {
      delete payload.password
      await updateUser(payload)
      ElMessage.success('已更新')
    } else {
      await createUser(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  })
}

function toggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  ElMessageBox.confirm(`确认${newStatus === 1 ? '启用' : '禁用'}该用户？`, '提示')
    .then(async () => {
      await updateUserStatus(row.id, newStatus)
      ElMessage.success('已更新')
      load()
    })
    .catch(() => {})
}

function remove(row) {
  ElMessageBox.confirm(`确认删除 ${row.username} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteUser(row.id)
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
