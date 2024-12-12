<template>
  <div class="assignment-statistics">
    <div class="header">
      <h2>作业统计 - {{ assignment?.title }}</h2>
    </div>

    <el-row :gutter="20" class="statistics-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总提交数</span>
            </div>
          </template>
          <div class="card-content">
            {{ statistics?.totalSubmissions || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>已评分</span>
            </div>
          </template>
          <div class="card-content">
            {{ statistics?.gradedCount || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>未评分</span>
            </div>
          </template>
          <div class="card-content warning">
            {{ statistics?.ungradedCount || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>平均分</span>
            </div>
          </template>
          <div class="card-content">
            {{ statistics?.averageScore?.toFixed(1) || '-' }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="score-details">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>分数分布</span>
            </div>
          </template>
          <div class="score-distribution">
            <div v-for="(count, range) in statistics?.scoreDistribution" :key="range" class="distribution-item">
              <span class="range">{{ range }}</span>
              <el-progress 
                :percentage="(count / statistics?.totalSubmissions * 100) || 0"
                :format="() => count"
                :color="getProgressColor(range)"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>分数范围</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="最高分">
              {{ statistics?.highestScore?.toFixed(1) || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="最低分">
              {{ statistics?.lowestScore?.toFixed(1) || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="平均分">
              {{ statistics?.averageScore?.toFixed(1) || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 提交列表 -->
    <el-card shadow="hover" class="submissions-list">
      <template #header>
        <div class="card-header">
          <span>提交列表</span>
          <el-button 
            v-if="hasUngradedSubmissions" 
            type="primary"
            @click="handleBatchGrade"
          >
            批量评分
          </el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="submissions"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="student.name" label="学生" width="120" />
        <el-table-column prop="filename" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="submittedAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getScoreTagType(row.score)" v-if="row.score !== null">
              {{ row.score }}
            </el-tag>
            <el-tag type="info" v-else>未评分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="feedback" label="反馈" min-width="200" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="router.push(`/submissions/${row.id}`)"
            >
              查看
            </el-button>
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
    </el-card>

    <!-- 批量评分对话框 -->
    <el-dialog
      v-model="batchGradeDialogVisible"
      title="批量评分"
      width="600px"
    >
      <el-form
        ref="batchGradeFormRef"
        :model="batchGradeForm"
        :rules="gradeRules"
        label-width="100px"
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
import { useRoute, useRouter } from 'vue-router'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(false)
const assignment = ref(null)
const statistics = ref(null)
const submissions = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const batchGradeDialogVisible = ref(false)
const batchGradeFormRef = ref(null)
const selectedSubmissions = ref([])

const batchGradeForm = ref({
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
const hasUngradedSubmissions = computed(() => {
  return submissions.value.some(s => s.score === null)
})

// 方法
const fetchStatistics = async () => {
  try {
    const response = await axios.get(`/api/assignments/${route.params.id}/statistics`)
    statistics.value = response.data
  } catch (error) {
    console.error('Fetch statistics error:', error)
    ElMessage.error('获取统计信息失败')
  }
}

const fetchSubmissions = async () => {
  loading.value = true
  try {
    const response = await axios.get(`/api/assignments/${route.params.id}/submissions`, {
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

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchSubmissions()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSubmissions()
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
        fetchStatistics()
      } catch (error) {
        console.error('Batch grade error:', error)
        ElMessage.error('批量评分失败')
      }
    }
  })
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

const getProgressColor = (range) => {
  const ranges = {
    '90-100': '#67C23A',
    '80-89': '#409EFF',
    '70-79': '#E6A23C',
    '60-69': '#F56C6C',
    '0-59': '#909399'
  }
  return ranges[range] || '#409EFF'
}

// 初始化
onMounted(() => {
  fetchStatistics()
  fetchSubmissions()
})
</script>

<style scoped>
.assignment-statistics {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.statistics-cards {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

.card-content.warning {
  color: #E6A23C;
}

.score-details {
  margin-bottom: 20px;
}

.score-distribution {
  padding: 10px;
}

.distribution-item {
  margin-bottom: 15px;
}

.range {
  display: inline-block;
  width: 80px;
  margin-right: 10px;
}

.submissions-list {
  margin-top: 20px;
}

.batch-grade-info {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.actions {
  gap: 10px;
}
</style>
