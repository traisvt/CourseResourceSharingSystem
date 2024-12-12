<template>
  <div class="submission-detail">
    <div class="header">
      <h2>提交详情</h2>
      <div class="actions">
        <el-button type="primary" @click="handleDownload" :loading="downloading">
          下载文件
        </el-button>
        <el-button 
          v-if="canGrade && !submission?.score"
          type="success" 
          @click="handleGrade"
        >
          评分
        </el-button>
        <el-button 
          v-if="canDelete"
          type="danger" 
          @click="handleDelete"
          :loading="deleting"
        >
          删除提交
        </el-button>
      </div>
    </div>

    <el-descriptions
      v-loading="loading"
      class="submission-info"
      :column="2"
      border
    >
      <el-descriptions-item label="作业标题">
        <el-link type="primary" @click="router.push(`/assignments/${submission?.assignment?.id}`)">
          {{ submission?.assignment?.title }}
        </el-link>
      </el-descriptions-item>
      <el-descriptions-item label="学生姓名">
        {{ submission?.student?.name }}
      </el-descriptions-item>
      <el-descriptions-item label="文件名">
        {{ submission?.filename }}
      </el-descriptions-item>
      <el-descriptions-item label="提交时间">
        {{ formatDate(submission?.submittedAt) }}
      </el-descriptions-item>
      <el-descriptions-item label="分数">
        <el-tag :type="getScoreTagType(submission?.score)">
          {{ submission?.score ?? '未评分' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="反馈">
        {{ submission?.feedback || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <!-- 评分对话框 -->
    <el-dialog
      v-model="gradeDialogVisible"
      title="评分"
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

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
    >
      <p>确定要删除这个提交吗？此操作不可恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确认删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const downloading = ref(false)
const deleting = ref(false)
const submission = ref(null)
const deleteDialogVisible = ref(false)
const gradeDialogVisible = ref(false)
const gradeFormRef = ref(null)

const gradeForm = ref({
  score: null,
  feedback: ''
})

const gradeRules = {
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' }
  ],
  feedback: [
    { required: true, message: '请输入反馈', trigger: 'blur' }
  ]
}

// 计算属性
const canDelete = computed(() => {
  return userStore.userInfo?.role === 'TEACHER' || 
         userStore.userInfo?.id === submission.value?.student?.id
})

const canGrade = computed(() => {
  return userStore.userInfo?.role === 'TEACHER'
})

// 方法
const fetchSubmission = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/api/submissions/${route.params.id}`)
    submission.value = response.data
  } catch (error) {
    console.error('Fetch submission error:', error)
    ElMessage.error('获取提交详情失败')
    if (error.response?.status === 404) {
      router.push('/submissions')
    }
  } finally {
    loading.value = false
  }
}

const handleDownload = async () => {
  downloading.value = true
  try {
    const response = await axios.get(`/api/submissions/${submission.value.id}/download`, {
      responseType: 'blob'
    })
    
    const contentDisposition = response.headers['content-disposition']
    let filename = submission.value.filename
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
  } finally {
    downloading.value = false
  }
}

const handleGrade = () => {
  gradeForm.value = {
    score: null,
    feedback: ''
  }
  gradeDialogVisible.value = true
}

const submitGrade = async () => {
  if (!gradeFormRef.value) return

  await gradeFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.post(`/api/submissions/${submission.value.id}/grade`, {
          score: gradeForm.value.score,
          feedback: gradeForm.value.feedback
        })
        ElMessage.success('评分成功')
        gradeDialogVisible.value = false
        fetchSubmission()
      } catch (error) {
        console.error('Grade submission error:', error)
        ElMessage.error(error.response?.data?.message || '评分失败')
      }
    }
  })
}

const handleDelete = () => {
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  deleting.value = true
  try {
    await axios.delete(`/api/submissions/${submission.value.id}`)
    ElMessage.success('删除成功')
    router.push('/submissions')
  } catch (error) {
    console.error('Delete submission error:', error)
    ElMessage.error('删除失败')
  } finally {
    deleting.value = false
    deleteDialogVisible.value = false
  }
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
  fetchSubmission()
})
</script>

<style scoped>
.submission-detail {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.submission-info {
  margin-top: 20px;
}

.actions {
  display: flex;
  gap: 10px;
}
</style>
