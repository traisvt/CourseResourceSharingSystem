<template>
  <div class="submission-container">
    <div class="header">
      <h2>作业提交</h2>
      <el-button
        v-if="isTeacher"
        type="primary"
        @click="showBatchGradeDialog = true"
        :disabled="!selectedSubmissions.length"
      >
        批量评分
      </el-button>
    </div>

    <!-- 作业提交列表 -->
    <el-table
      v-loading="loading"
      :data="submissions"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column
        v-if="userStore.canGrade"
        type="selection"
        width="55"
      />
      <el-table-column prop="assignment" label="作业" min-width="150">
        <template #default="{ row }">
          <el-button 
            link 
            type="primary" 
            @click="router.push(`/assignments/${row.assignment?.id}`)"
          >
            {{ row.assignment?.title || '-' }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="student" label="学生" min-width="120">
        <template #default="{ row }">
          {{ row.student?.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="filename" label="文件名" min-width="180" show-overflow-tooltip/>
      <el-table-column prop="submittedAt" label="提交时间" min-width="160">
        <template #default="{ row }">
          {{ formatDate(row.submittedAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分数" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="getScoreTagType(row.score)"
            size="small"
            v-if="row.score !== null"
          >
            {{ row.score }}
          </el-tag>
          <el-tag
            type="info"
            size="small"
            v-else
          >
            未评分
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="feedback" label="反馈" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-tooltip
            v-if="row.feedback"
            :content="row.feedback"
            placement="top"
            :hide-after="2000"
          >
            <span>{{ row.feedback }}</span>
          </el-tooltip>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="220">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            @click="router.push(`/submissions/${row.id}`)"
          >
            查看
          </el-button>
          <el-button
            link
            type="primary"
            @click="handleDownload(row)"
          >
            下载
          </el-button>
          <template v-if="userStore.canGrade">
            <el-button
              link
              type="success"
              @click="handleGrade(row)"
            >
              {{ row.score !== null ? '重新评分' : '评分' }}
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 评分对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      :title="`评分 - ${currentSubmission?.student?.name || ''} 的作业`"
      width="500px"
    >
      <el-form
        ref="gradeFormRef"
        :model="gradeForm"
        :rules="gradeRules"
        label-width="80px"
      >
        <el-form-item label="分数" prop="score">
          <el-input-number
            v-model="gradeForm.score"
            :min="0"
            :max="100"
            :precision="1"
            :step="0.5"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="反馈" prop="feedback">
          <el-input
            v-model="gradeForm.feedback"
            type="textarea"
            :rows="3"
            placeholder="请输入评分反馈"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="gradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGrade">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量评分对话框 -->
    <el-dialog
      v-model="batchGradeDialogVisible"
      title="批量评分"
      width="500px"
    >
      <div class="batch-grade-info">
        <p>已选择 {{ selectedSubmissions.length }} 个提交进行评分</p>
      </div>
      <el-form
        ref="batchGradeFormRef"
        :model="batchGradeForm"
        :rules="gradeRules"
        label-width="80px"
      >
        <el-form-item label="分数" prop="score">
          <el-input-number
            v-model="batchGradeForm.score"
            :min="0"
            :max="100"
            :precision="1"
            :step="0.5"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="反馈" prop="feedback">
          <el-input
            v-model="batchGradeForm.feedback"
            type="textarea"
            :rows="3"
            placeholder="请输入评分反馈"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchGradeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBatchGrade">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const submissions = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const gradeDialogVisible = ref(false)
const batchGradeDialogVisible = ref(false)
const currentSubmission = ref(null)
const gradeFormRef = ref(null)
const batchGradeFormRef = ref(null)
const selectedSubmissions = ref([])

const gradeForm = ref({
  score: null,
  feedback: ''
})

const batchGradeForm = ref({
  score: null,
  feedback: ''
})

const gradeRules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' },
    { type: 'number', message: '分数必须是数字', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value < 0 || value > 100) {
        callback(new Error('分数必须在0-100之间'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  feedback: [
    { required: true, message: '请输入反馈', trigger: 'blur' }
  ]
}

// 计算属性
const isStudent = computed(() => userStore.isStudent)
const canGrade = computed(() => userStore.canGrade)
const displayRole = computed(() => userStore.displayRole)

// 方法
const fetchSubmissions = async () => {
  loading.value = true
  try {
    const url = userStore.isStudent
      ? '/api/submissions/my-submissions'
      : '/api/submissions/all-submissions'
    
    const response = await axios.get(url, {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    submissions.value = response.data.content
    total.value = response.data.totalElements
  } catch (error) {
    console.error('Fetch submissions error:', error)
    ElMessage.error('获取提交列表失败')
  } finally {
    loading.value = false
  }
}

const handleDownload = async (submission) => {
  try {
    const response = await axios.get(`/api/submissions/${submission.id}/download`, {
      responseType: 'blob'
    })
    
    const contentDisposition = response.headers['content-disposition']
    let filename = submission.filename
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename="?(.+)"?/)
      if (filenameMatch && filenameMatch[1]) {
        filename = decodeURIComponent(filenameMatch[1])
      }
    }
    
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('Download error:', error)
    ElMessage.error('下载失败')
  }
}

const handleGrade = (submission) => {
  currentSubmission.value = submission
  gradeForm.value = {
    score: submission.score || null,
    feedback: submission.feedback || ''
  }
  gradeDialogVisible.value = true
}

const submitGrade = async () => {
  if (!gradeFormRef.value) return

  await gradeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const params = {
          score: Number(gradeForm.value.score),
          feedback: gradeForm.value.feedback
        }
        
        console.log('Submitting grade:', {
          submissionId: currentSubmission.value.id,
          ...params
        })

        const response = await axios.post(
          `/api/submissions/${currentSubmission.value.id}/grade`,
          null,  // 不发送请求体
          { params }  // 作为查询参数发送
        )

        console.log('Grade response:', response.data)
        ElMessage.success('评分成功')
        gradeDialogVisible.value = false
        fetchSubmissions()
      } catch (error) {
        console.error('Grade submission error:', error)
        if (error.response) {
          console.error('Error response:', error.response.data)
          const message = error.response.data.message || 
            (error.response.status === 400 ? '请检查分数格式是否正确（0-100之间的数字）' : '评分失败')
          ElMessage.error(message)
        } else {
          ElMessage.error('评分失败：' + error.message)
        }
      }
    }
  })
}

const handleSelectionChange = (selection) => {
  selectedSubmissions.value = selection
}

const handleBatchGrade = () => {
  if (selectedSubmissions.value.length === 0) {
    ElMessage.warning('请选择要评分的提交')
    return
  }
  if (selectedSubmissions.value.length > 50) {
    ElMessage.warning('每次最多只能批量评分50条记录')
    return
  }
  batchGradeForm.value = {
    score: null,
    feedback: ''
  }
  batchGradeDialogVisible.value = true
}

const submitBatchGrade = async () => {
  if (!batchGradeFormRef.value) return

  await batchGradeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const grades = selectedSubmissions.value.map(submission => ({
          submissionId: submission.id,
          score: batchGradeForm.value.score,
          feedback: batchGradeForm.value.feedback
        }))

        const response = await axios.post('/api/submissions/batch-grade', { grades })
        
        if (response.data.successIds.length > 0) {
          ElMessage.success(`成功评分 ${response.data.successIds.length} 条记录`)
        }
        
        if (Object.keys(response.data.failures).length > 0) {
          ElMessage.warning(`${Object.keys(response.data.failures).length} 条记录评分失败`)
        }
        
        batchGradeDialogVisible.value = false
        fetchSubmissions()
      } catch (error) {
        console.error('Batch grade error:', error)
        ElMessage.error('批量评分失败')
      }
    }
  })
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchSubmissions()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSubmissions()
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getScoreTagType = (score) => {
  if (score === null) return 'info'
  if (score >= 90) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 初始化
onMounted(() => {
  console.log('Component mounted')
  console.log('User info:', userStore.userInfo)
  console.log('User role:', userStore.userInfo?.role)
  if (!userStore.userInfo) {
    userStore.initUserInfo()
  }
  fetchSubmissions()
})
</script>

<style scoped>
.submission-container {
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

/* 移除角色信息相关样式 */
.role-info {
  display: none;
}

/* 表格样式 */
:deep(.el-table) {
  --el-table-border-color: var(--border-light);
  border-radius: 0;
  box-shadow: none;
  font-size: 14px;
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

/* 表格单元格内容样式 */
:deep(.el-table .cell) {
  padding: 0;
  line-height: 1.5;
  white-space: nowrap;
}

/* 作业标题链接样式 */
:deep(.assignment-title) {
  color: var(--text-regular);
  text-decoration: none;
  cursor: pointer;
}

:deep(.assignment-title:hover) {
  color: var(--primary-color);
}

/* 分数标签样式 */
:deep(.el-tag) {
  border-radius: 12px;
  padding: 2px 12px;
  height: 24px;
  line-height: 20px;
  font-weight: normal;
  min-width: 48px;
  text-align: center;
}

/* 操作按钮样式 */
.operation-buttons {
  display: flex;
  gap: 16px;
}

:deep(.el-button--text) {
  padding: 0;
  height: auto;
  border: none;
  background: none;
  color: var(--text-regular);
  font-size: 13px;
  font-weight: normal;
}

:deep(.el-button--text:hover) {
  color: var(--primary-color);
  background: none;
}

/* 表格行悬浮效果 */
:deep(.el-table__row) {
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: var(--bg-color) !important;
}

/* 表格内容对齐方式 */
:deep(.el-table td.el-table__cell) {
  text-align: left;
}

:deep(.el-table th.el-table__cell) {
  text-align: left;
}

/* 确保所有列左对齐 */
:deep(.el-table .cell) {
  text-align: left;
  display: flex;
  align-items: center;
}

/* 特定列的样式调整 */
:deep(.el-table .submission-status) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

:deep(.el-table .score-column .cell) {
  justify-content: flex-start;
}

:deep(.el-table .operation-column .cell) {
  justify-content: flex-start;
}

/* 分页器样式 */
.pagination {
  margin-top: 16px;
  padding: 16px 0;
  background: transparent;
  box-shadow: none;
  display: flex;
  justify-content: flex-end;
}

/* 评分对话框 */
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

/* 批量评分信息 */
.batch-grade-info {
  background: var(--bg-color);
  border-radius: 4px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.batch-grade-info p {
  margin: 4px 0;
  color: var(--text-regular);
  font-size: 13px;
}

/* 应式设计 */
@media screen and (max-width: 768px) {
  .submission-container {
    padding: 12px;
  }
  
  .header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .operation-buttons {
    flex-wrap: wrap;
  }
}
</style>
