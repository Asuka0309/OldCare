<template>
  <div class="page-card">
    <div class="search-bar">
      <el-input v-model="query.name" placeholder="按姓名搜索" clearable style="width: 220px;" @keyup.enter="load">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading" size="small">
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="certification" label="资质" show-overflow-tooltip />
      <el-table-column prop="experienceYears" label="经验(年)" width="100" />
      <el-table-column prop="rating" label="评分" width="170">
        <template #default="scope">
          <el-rate v-model="scope.row.rating" disabled allow-half show-score score-template="{value}" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190">
        <template #default="scope">
          <el-button size="small" type="primary" link :icon="Edit" @click="openForm(scope.row)">编辑</el-button>
          <el-button size="small" type="success" @click="updateRating(scope.row)">更新评分</el-button>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑员工' : '新增员工'" width="520px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
      <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="年龄" prop="age"><el-input-number v-model="form.age" :min="18" :max="80" /></el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="form.gender">
          <el-option label="男" value="男" />
          <el-option label="女" value="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="身份证" prop="idCard"><el-input v-model="form.idCard" /></el-form-item>
      <el-form-item label="电话" prop="phone"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="资质" prop="certification"><el-input v-model="form.certification" type="textarea" :rows="2" /></el-form-item>
      <el-form-item label="经验(年)" prop="experienceYears"><el-input-number v-model="form.experienceYears" :min="0" :max="60" /></el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status">
          <el-option label="在职" :value="1" />
          <el-option label="离职" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="评分" prop="rating"><el-rate v-model="form.rating" allow-half show-score /></el-form-item>
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
import { fetchCaregivers, createCaregiver, updateCaregiver, deleteCaregiver, updateCaregiverRating } from '../api/caregiver'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, name: '' })

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  age: 30,
  gender: '女',
  idCard: '',
  phone: '',
  certification: '',
  experienceYears: 5,
  status: 1,
  rating: 4.5
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

function resetForm(row) {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    age: row?.age || 30,
    gender: row?.gender || '女',
    idCard: row?.idCard || '',
    phone: row?.phone || '',
    certification: row?.certification || '',
    experienceYears: row?.experienceYears || 5,
    status: typeof row?.status === 'number' ? row.status : 1,
    rating: row?.rating || 4.5
  })
}

async function load() {
  loading.value = true
  try {
    const data = await fetchCaregivers(query)
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
      await updateCaregiver(payload)
      ElMessage.success('已更新')
    } else {
      await createCaregiver(payload)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    load()
  })
}

function updateRating(row) {
  ElMessageBox.prompt('输入新的评分 (0-5，可含0.5)', '更新评分', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^([0-4](\.5)?|5)$/,
    inputErrorMessage: '请输入0-5，步长0.5'
  })
    .then(async ({ value }) => {
      await updateCaregiverRating(row.id, Number(value))
      ElMessage.success('评分已更新')
      load()
    })
    .catch(() => {})
}

function remove(row) {
  ElMessageBox.confirm(`确认删除 ${row.name} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteCaregiver(row.id)
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
