<template>
  <div class="assignment-container">
    <div class="header">
      <h2>作业管理</h2>
      <el-button
        v-if="userStore.userInfo?.role === 'TEACHER'"
        type="primary"
        @click="dialogVisible = true"
      >
        发布作业
      </el-button>
    </div>

    <!-- 作业列表 -->
    <div class="assignment-list">
      <el-table
        v-loading="loading"
        :data="assignments"
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip/>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="teacher" label="发布教师" min-width="120">
          <template #default="{ row }">
            {{ row.teacher?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止时间" min-width="160">
          <template #default="{ row }">
            <el-tag 
              :type="isOverdue(row.dueDate) ? 'danger' : 'success'"
              size="small"
            >
              {{ formatDate(row.dueDate) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" min-width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="userStore.userInfo?.role === 'STUDENT'"
              link
              type="primary"
              @click="handleSubmit(row)"
            >
              提交
            </el-button>
            <el-button
              v-if="canManage(row)"
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <el-button
              v-if="canManage(row)"
              link
              type="primary"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 发布作业对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入作业标题"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入作业描述"
          />
        </el-form-item>
        <el-form-item label="截止时间" prop="dueDate">
          <el-date-picker
            v-model="form.dueDate"
            type="datetime"
            placeholder="请选择截止时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled-date="disabledDate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFormFunc">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 提交作业对话框 -->
    <el-dialog
      v-model="submitDialogVisible"
      :title="`提交作业: ${currentAssignment?.title || ''}`"
      width="500px"
    >
      <div class="assignment-info" v-if="currentAssignment">
        <p><strong>截止时间：</strong>{{ formatDate(currentAssignment.dueDate) }}</p>
        <p><strong>剩余时间：</strong>
          <el-tag :type="getRemainingTimeType(currentAssignment.dueDate)">
            {{ getRemainingTime(currentAssignment.dueDate) }}
          </el-tag>
        </p>
      </div>

      <el-upload
        ref="uploadRef"
        :action="`/api/submissions/${currentAssignment?.id}`"
        :headers="uploadHeaders"
        :before-upload="beforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
      >
        <template #trigger>
          <el-button type="primary">选择文件</el-button>
        </template>
        <template #tip>
          <div class="el-upload__tip">
            支持PDF、Word、Excel、PowerPoint、图片和文本文件，大小不超过100MB
          </div>
        </template>
      </el-upload>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submitDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitAssignment"
            :disabled="!hasFile"
          >
            确认提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import axios from '@/utils/axios'

const userStore = useUserStore()
const router = useRouter()
const formRef = ref(null)
const uploadRef = ref(null)
const dialogVisible = ref(false)
const submitDialogVisible = ref(false)
const loading = ref(false)
const currentAssignment = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const assignments = ref([])
const hasFile = ref(false)

const form = ref({
  title: '',
  description: '',
  dueDate: ''
})

const rules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入作业描述', trigger: 'blur' }
  ],
  dueDate: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 计算属性
const dialogTitle = computed(() => 
  currentAssignment.value ? '编辑作业' : '发布作业'
)

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 方法
const fetchAssignments = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/assignments', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    assignments.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAssignments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAssignments()
}

const handleCreate = () => {
  form.value = {
    title: '',
    description: '',
    dueDate: ''
  }
}

const submitFormFunc = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    const data = {
      title: form.value.title.trim(),
      description: form.value.description?.trim() || '',
      dueDate: form.value.dueDate
    }

    const response = await axios.post('/api/assignments', data)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    resetForm()
    await fetchAssignments()
  } catch (error) {
    if (error.response) {
      console.error('API错误:', error.response.data)
      ElMessage.error(error.response.data?.message || '发布失败')
    } else if (error.name === 'ValidationError') {
      console.error('表单验证失败:', error)
    } else {
      console.error('未知错误:', error)
      ElMessage.error('发布失败，请重试')
    }
  }
}

const handleSubmit = (assignment) => {
  if (isOverdue(assignment.dueDate)) {
    ElMessage.warning('作业已截止，无法提交')
    return
  }
  currentAssignment.value = assignment
  submitDialogVisible.value = true
}

const handleEdit = (row) => {
  currentAssignment.value = row
  form.value = {
    title: row.title,
    description: row.description,
    dueDate: row.dueDate
  }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该作业吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await axios.delete(`/api/homework/${row.id}`)
    ElMessage.success('删除成功')
    await fetchAssignments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const beforeUpload = (file) => {
  const maxSize = 100 * 1024 * 1024 // 100MB
  const allowedTypes = {
    'application/pdf': '.pdf',
    'application/msword': '.doc',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx',
    'application/vnd.ms-excel': '.xls',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '.xlsx',
    'application/vnd.ms-powerpoint': '.ppt',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': '.pptx',
    'image/jpeg': '.jpg',
    'image/png': '.png',
    'image/gif': '.gif',
    'text/plain': '.txt'
  }
  
  if (!allowedTypes[file.type]) {
    ElMessage.error(`不支持的文件类型：${file.type}`)
    return false
  }
  
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  
  return true
}

const handleFileChange = (file) => {
  hasFile.value = !!file
}

const submitAssignment = () => {
  if (!hasFile.value) {
    ElMessage.warning('请选择要提交的文件')
    return
  }
  if (uploadRef.value) {
    uploadRef.value.submit()
  }
}

const handleUploadSuccess = (response) => {
  ElMessage.success('提交成功')
  submitDialogVisible.value = false
  hasFile.value = false
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  router.push('/submissions')
}

const handleUploadError = (error) => {
  console.error('Upload error:', error)
  ElMessage.error(error.response?.data?.message || '提交失败')
  hasFile.value = false
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const canManage = (assignment) => {
  const role = userStore.userInfo?.role
  const isOwner = assignment.teacher?.id === userStore.userInfo?.id
  return role === 'ADMIN' || (role === 'TEACHER' && isOwner)
}

const resetForm = () => {
  currentAssignment.value = null
  form.value = {
    title: '',
    description: '',
    dueDate: ''
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const isOverdue = (dueDate) => {
  return new Date(dueDate) < new Date()
}

const disabledDate = (time) => {
  return time.getTime() < Date.now()
}

const getRemainingTime = (dueDate) => {
  const now = new Date()
  const due = new Date(dueDate)
  const diff = due - now

  if (diff <= 0) {
    return '已截止'
  }

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (days > 0) {
    return `${days}天${hours}小时`
  }
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

const getRemainingTimeType = (dueDate) => {
  const now = new Date()
  const due = new Date(dueDate)
  const diff = due - now

  if (diff <= 0) {
    return 'danger'
  }
  if (diff <= 24 * 60 * 60 * 1000) {
    return 'warning'
  }
  return 'success'
}

// 初始化
fetchAssignments()
</script>

<style scoped>
.assignment-container {
  padding: 16px 24px;
  background-color: white;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.header {
  padding: 12px 0 20px;
  margin-bottom: 0;
  background: transparent;
  box-shadow: none;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h2 {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0;
}

/* 表格样式 */
:deep(.el-table) {
  --el-table-border-color: var(--border-light);
  border-radius: 0;
  box-shadow: none;
}

:deep(.el-table th) {
  background-color: white !important;
  font-weight: 500;
  color: var(--text-regular);
  padding: 12px 16px;
  border-bottom: thin solid var(--border-light);
}

:deep(.el-table td) {
  padding: 12px 16px;
  border-bottom: thin solid var(--border-light);
}

:deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: var(--bg-color) !important;
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 12px;
  padding: 2px 12px;
  height: 24px;
  line-height: 20px;
  font-weight: normal;
}

/* 操作按钮 */
.table-operations {
  display: flex;
  gap: 8px;
}

:deep(.el-button--text) {
  padding: 4px 8px;
  color: var(--text-regular);
}

:deep(.el-button--text:hover) {
  color: var(--primary-color);
  background-color: transparent;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
}

:deep(.el-dialog__header) {
  padding: 16px 24px;
  margin: 0;
  border-bottom: thin solid var(--border-light);
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: thin solid var(--border-light);
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: normal;
  color: var(--text-regular);
}

:deep(.el-input__wrapper) {
  background: var(--bg-color);
  border-radius: 4px !important;
  box-shadow: none !important;
  transition: all 0.2s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
}

/* 上传组件样式 */
:deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  border: thin dashed var(--border-color);
  border-radius: 8px;
  transition: all 0.2s ease;
}

:deep(.el-upload-dragger:hover) {
  border-color: var(--primary-color);
  background: var(--bg-color);
}

/* 分页器 */
.pagination {
  margin-top: 16px;
  padding: 16px 0;
  background: transparent;
  box-shadow: none;
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .assignment-container {
    padding: 12px;
  }
  
  .header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 提交作业对话框样式 */
.assignment-info {
  background: var(--bg-color);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.assignment-info p {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.assignment-info strong {
  color: var(--text-regular);
  min-width: 80px;
}

.el-upload__tip {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 8px;
  line-height: 1.5;
}
</style>
