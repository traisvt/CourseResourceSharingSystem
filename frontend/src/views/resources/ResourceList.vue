<template>
  <div class="resource-list-container">
    <div class="page-header">
      <div class="title-section">
        <h2>学习资源</h2>
        <span class="subtitle">发现 {{ total }} 个优质资源</span>
      </div>
      
      <div class="action-section">
        <div class="search-box">
          <el-input
            v-model="searchQuery"
            placeholder="搜索资源名称、描述..."
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #append>
              <el-button @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </template>
          </el-input>
          
          
        </div>
        <el-checkbox
            v-model="showMyResources"
            class="my-resources-checkbox"
            @change="handleSearch"
          >
            只看我的资源
          </el-checkbox>
        <div class="right-section">
          <el-radio-group v-model="viewMode" class="view-toggle">
            <el-radio-button label="table">
              <el-icon><List /></el-icon>
            </el-radio-button>
            <el-radio-button label="grid">
              <el-icon><Grid /></el-icon>
            </el-radio-button>
          </el-radio-group>

          <div class="button-group">
            <el-button
              type="danger"
              :icon="Delete"
              :disabled="!selectedResources.length"
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
            <el-button
              type="primary"
              :icon="Download"
              :disabled="!selectedResources.length"
              @click="handleBatchDownload"
            >
              批量下载
            </el-button>
            <el-button 
              type="success"
              :icon="Upload"
              @click="uploadDialogVisible = true"
            >
              上传资源
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-card class="resource-table-card" :body-style="{ padding: '0px' }" v-if="viewMode === 'table'">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="sortedResources"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        @row-click="handleRowClick"
        :header-cell-style="{
          background: '#f5f7fa',
          color: '#606266',
          fontWeight: 'bold'
        }"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="title" label="标题" min-width="90" max-width="100" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="resource-title">
              <el-icon><Document /></el-icon>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="90" max-width="150" show-overflow-tooltip/>
        <el-table-column prop="fileType" label="类型" width="130">
          <template #default="{ row }">
            <el-tag 
              :type="getFileTypeTag(row.fileType)"
              size="small"
              class="file-type-tag"
            >
              {{ getFileTypeDisplay(row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="120" sortable>
          <template #default="{ row }">
            <span class="file-size">{{ formatFileSize(row.fileSize) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="downloadCount" label="下载量" width="140" align="center" sortable>
          <template #default="{ row }">
            <div class="download-count">
              <el-icon class="download-icon"><Download /></el-icon>
              <span>{{ row.downloadCount }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="uploader" label="上传者" width="160">
          <template #default="{ row }">
            <div class="uploader-info">
              <el-avatar 
                :size="24"
                class="uploader-avatar"
                :style="{ backgroundColor: '#626aef' }"
              >
                {{ row.uploader?.name?.charAt(0) }}
              </el-avatar>
              <span>{{ row.uploader?.name || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="200" sortable>
          <template #default="{ row }">
            <div class="time-info">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="operation-buttons">
              <el-button
                type="primary"
                circle
                :icon="Download"
                size="small"
                @click.stop="handleDownload(row)"
              />
              <el-button
                v-if="canDelete(row)"
                type="danger"
                circle
                :icon="Delete"
                size="small"
                @click.stop="handleDelete(row)"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <div class="resource-grid" v-else>
      <el-row :gutter="20">
        <el-col 
          v-for="resource in sortedResources" 
          :key="resource.id"
          :xs="24"    
          :sm="12"   
          :md="8"     
          :lg="6"    
          :xl="6"  
          class="resource-grid-item"
    >
          <el-card 
            :body-style="{ padding: '0px' }" 
            shadow="hover"
            :class="{ 'is-selected': selectedResources.includes(resource) }"
            @click="handleGridItemClick(resource)"
          >
            <div class="grid-item-content">
              <div class="file-icon">
                <el-icon :size="80"><Document /></el-icon>
              </div>
              <h3 class="file-title" :title="resource.title">{{ resource.title }}</h3>
              <p class="file-desc" :title="resource.description">{{ resource.description }}</p>
              <div class="file-info">
                <span class="file-size">{{ formatFileSize(resource.fileSize) }}</span>
                <el-tag size="small" :type="getFileTypeTag(resource.fileType)" effect="dark">
                  {{ getFileTypeDisplay(resource.fileType) }}
                </el-tag>
              </div>
              <div class="file-footer">
                <span class="download-count">
                  <el-icon><Download /></el-icon>
                  {{ resource.downloadCount }}
                </span>
                <span class="upload-time">{{ formatDate(resource.createdAt) }}</span>
              </div>
              <div class="grid-operations">
                <el-button-group>
                  <el-button
                    type="primary"
                    :icon="Download"
                    size="small"
                    @click.stop="handleDownload(resource)"
                  />
                  <el-button
                    v-if="canDelete(resource)"
                    type="danger"
                    :icon="Delete"
                    size="small"
                    @click.stop="handleDelete(resource)"
                  />
                </el-button-group>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Upload Dialog -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传资源"
      width="500px"
      class="upload-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="uploadForm.title" placeholder="请输入资源标题"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入资源描述"
          />
        </el-form-item>
        <el-form-item label="文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-exceed="handleExceed"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept="*"
            class="upload-demo"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                文件大小不能超过100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { saveAs } from 'file-saver'
import JSZip from 'jszip'
import { 
  Delete, 
  Download, 
  Upload, 
  Search, 
  Document, 
  Calendar,
  UploadFilled,
  List,
  Grid
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 状态
const resources = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const uploadDialogVisible = ref(false)
const uploadForm = ref({
  title: '',
  description: '',
  file: null
})
const uploadRef = ref(null)
const showMyResources = ref(false)
const selectedResources = ref([])
const currentSort = ref({
  prop: '',
  order: ''
})
const tableRef = ref(null)
const viewMode = ref('table')

// 计算属性
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const sortedResources = computed(() => {
  if (!currentSort.value.prop || !currentSort.value.order) {
    return resources.value
  }

  return [...resources.value].sort((a, b) => {
    const prop = currentSort.value.prop
    const order = currentSort.value.order === 'ascending' ? 1 : -1
    
    if (prop === 'fileSize') {
      return (parseInt(a.fileSize) - parseInt(b.fileSize)) * order
    }
    if (prop === 'downloadCount') {
      return (a.downloadCount - b.downloadCount) * order
    }
    if (prop === 'createdAt') {
      return a.createdAt.localeCompare(b.createdAt) * order
    }
    return 0
  })
})

// 方法
const fetchResources = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    let url = '/api/resources'

    // 如果有搜索关键词，使用搜索接口
    if (searchQuery.value) {
      url = '/api/resources/search'
      params.query = searchQuery.value.trim()
    }

    // 如果选择了只看我的资源，添加 uploaderId 参数
    if (showMyResources.value && userStore.userInfo?.id) {
      params.uploaderId = userStore.userInfo.id
      console.log('在用户ID为', userStore.userInfo.id, '的资源')
    }

    // 如果有排序参数，添加 sort 参数
    if (currentSort.value.prop) {
      params.sort = `${currentSort.value.prop},${currentSort.value.order}`
    }

    console.log('Fetching resources with params:', params)
    const response = await axios.get(url, { params })
    console.log('Response status:', response.status)
    console.log('Response data:', response.data)
    
    // 处理响应数据
    if (response.data && Array.isArray(response.data)) {
      // 如果返回的是数组
      let filteredResources = [...response.data]
      if (showMyResources.value && userStore.userInfo?.id) {
        filteredResources = filteredResources.filter(
          resource => resource.uploader?.id === userStore.userInfo.id
        )
      }
      resources.value = filteredResources
      total.value = filteredResources.length
    } else if (response.data && response.data.content && Array.isArray(response.data.content)) {
      // 如果返回的是分页对象
      let filteredResources = [...response.data.content]
      if (showMyResources.value && userStore.userInfo?.id) {
        filteredResources = filteredResources.filter(
          resource => resource.uploader?.id === userStore.userInfo.id
        )
      }
      resources.value = filteredResources
      total.value = showMyResources.value ? filteredResources.length : response.data.totalElements
      currentPage.value = response.data.number + 1
      pageSize.value = response.data.size
    } else {
      console.error('Invalid response format:', response.data)
      resources.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取资源列表失败:', error)
    ElMessage.error('获取资源列表失败')
    resources.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchResources()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchResources()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchResources()
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件')
}

const handleFileChange = (file) => {
  // 确保获取原始文件对象
  if (file.raw) {
    uploadForm.value.file = file.raw
  } else {
    uploadForm.value.file = file
  }
}

const beforeUpload = (file) => {
  // 检查文件大小（100MB）
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过100MB')
    return false
  }
  return true
}

const submitUpload = async () => {
  // 验证表单
  if (!uploadForm.value.title?.trim()) {
    ElMessage.warning('请输入资源标题')
    return
  }
  
  if (!uploadForm.value.description?.trim()) {
    ElMessage.warning('请输入资源描述')
    return
  }

  if (!uploadForm.value.file) {
    ElMessage.warning('请选择文件')
    return
  }

  try {
    // 创建FormData对象
    const formData = new FormData()
    
    // 确保文件对象正确添加
    const file = uploadForm.value.file
    formData.append('file', file)
    formData.append('title', uploadForm.value.title.trim())
    formData.append('description', uploadForm.value.description.trim())

    // 打印请求信息（调试用）
    console.log('上传请求:', {
      file: file.name,
      title: uploadForm.value.title,
      description: uploadForm.value.description
    })

    const response = await axios.post('/api/resources', formData, {
      headers: {
        Authorization: `Bearer ${userStore.token}`,
        // 让浏览器自动设置正确的 Content-Type 和 boundary
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data) {
      ElMessage.success('上传成功')
      uploadDialogVisible.value = false
      // 重置表单
      uploadForm.value = {
        title: '',
        description: '',
        file: null
      }
      if (uploadRef.value) {
        uploadRef.value.clearFiles()
      }
      // 刷新资源列表以显示最新上传的资源（包含上传者信息）
      await fetchResources()
    }
  } catch (error) {
    // 详细的错误日志
    console.error('上传错误:', {
      status: error.response?.status,
      message: error.response?.data?.message,
      error: error.response?.data
    })

    if (error.response?.status === 413) {
      ElMessage.error('文件太大，请选择小于100MB的文件')
    } else if (error.response?.status === 415) {
      ElMessage.error('不支持的文件类型')
    } else if (error.response?.status === 401) {
      ElMessage.error('请先登录')
    } else if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('上传失败，请重试')
    }
  }
}

const handleDownload = async (resource) => {
  try {
    const response = await axios.get(`/api/resources/${resource.id}/download`, {
      responseType: 'blob'
    })

    // 根据 MIME 类型获取文件扩展名
    const extension = getFileExtensionFromType(resource.fileType)
    const fileName = `${resource.title}${extension}`

    // 使用 file-saver 保存文件
    saveAs(response.data, fileName)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请重试')
  }
}

const canDelete = (resource) => {
  const role = userStore.userInfo?.role
  const userId = userStore.userInfo?.id
  
  // 管理员可以删除所有资源
  if (role === 'ADMIN') return true
  
  // 教师和学生只能删除自己上传的资源
  return resource.uploader?.id === userId
}

const handleDelete = (resource) => {
  ElMessageBox.confirm(
    '确定要删除这个资源吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await axios.delete(`/api/resources/${resource.id}`)
      ElMessage.success('删除成功')
      fetchResources()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const getFileTypeTag = (fileType) => {
  if (fileType?.includes('pdf')) return 'danger'
  if (fileType?.includes('word')) return 'primary'
  if (fileType?.includes('presentation')) return 'warning'
  if (fileType?.includes('image')) return 'success'
  return 'info'
}

const getFileTypeDisplay = (fileType) => {
  const typeMap = {
    'application/pdf': 'PDF',
    'image/png': 'PNG图片',
    'image/jpeg': 'JPG图片',
    'application/msword': 'Word',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'Word',
    'application/vnd.ms-excel': 'Excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'Excel',
    'application/zip': 'ZIP压缩包',
    'text/plain': '文本文件'
  }
  return typeMap[fileType] || fileType
}

const formatFileSize = (size) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
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

const getFileExtensionFromType = (mimeType) => {
  switch (mimeType.toLowerCase()) {
    case 'application/pdf':
      return '.pdf'
    case 'application/msword':
      return '.doc'
    case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
      return '.docx'
    case 'application/vnd.ms-excel':
      return '.xls'
    case 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet':
      return '.xlsx'
    case 'application/vnd.ms-powerpoint':
      return '.ppt'
    case 'application/vnd.openxmlformats-officedocument.presentationml.presentation':
      return '.pptx'
    case 'image/jpeg':
      return '.jpg'
    case 'image/png':
      return '.png'
    case 'image/gif':
      return '.gif'
    case 'text/plain':
      return '.txt'
    default:
      // 如果找不到对应的扩展名，从 MIME 类型中提取
      const parts = mimeType.split('/')
      return parts.length > 1 ? '.' + parts[1] : ''
  }
}

const handleShowMyResourcesChange = () => {
  console.log('Show my resources changed:', showMyResources.value)
  console.log('Current user:', userStore.userInfo)
  if (showMyResources.value && !userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    showMyResources.value = false
    return
  }
  currentPage.value = 1
  fetchResources()
}

const handleSelectionChange = (selection) => {
  selectedResources.value = selection
}

const handleBatchDelete = async () => {
  if (!selectedResources.value.length) return

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedResources.value.length} 个资源吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const deletePromises = selectedResources.value.map(resource => 
      axios.delete(`/api/resources/${resource.id}`)
    )

    await Promise.all(deletePromises)
    ElMessage.success('批量删除成功')
    await fetchResources()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleBatchDownload = async () => {
  if (!selectedResources.value.length) return

  try {
    ElMessage.info('正在准备下载...')
    
    // 如果只选择了一个文件，直接下载
    if (selectedResources.value.length === 1) {
      await handleDownload(selectedResources.value[0])
      return
    }

    // 多个文件打包下载
    const zip = new JSZip()
    const downloadPromises = selectedResources.value.map(async resource => {
      try {
        const response = await axios.get(`/api/resources/${resource.id}/download`, {
          responseType: 'blob'
        })
        // 使用原始文件名或根据类型生成文件名
        const extension = getFileExtensionFromType(resource.fileType)
        const fileName = `${resource.title}${extension}`
        zip.file(fileName, response.data)
      } catch (error) {
        console.error(`下载文件失败: ${resource.title}`, error)
        throw error
      }
    })

    await Promise.all(downloadPromises)
    
    const content = await zip.generateAsync({ type: 'blob' })
    saveAs(content, '资源下载.zip')
    ElMessage.success('批量下载成功')
  } catch (error) {
    console.error('批量下载失败:', error)
    ElMessage.error('批量下载失败，请重试')
  }
}

const handleSortChange = ({ prop, order }) => {
  currentSort.value = { prop, order }
}

const handleRowClick = (row) => {
  if (!tableRef.value) return
  tableRef.value.toggleRowSelection(row)
}

const handleGridItemClick = (resource) => {
  const index = selectedResources.value.findIndex(item => item.id === resource.id)
  if (index === -1) {
    selectedResources.value.push(resource)
  } else {
    selectedResources.value.splice(index, 1)
  }
}

// 初始化
fetchResources()

// 添加请求拦截器
axios.interceptors.request.use((config) => {
  console.log('请求拦截器:', config)
  return config
}, (error) => {
  console.error('请求拦截器错误:', error)
  return Promise.reject(error)
})

axios.interceptors.response.use((response) => {
  console.log('响应拦截器:', response)
  return response
}, (error) => {
  console.error('响应拦截器错误:', error)
  return Promise.reject(error)
})
</script>

<style scoped>
.resource-list-container {
  padding: 16px 24px;
  background-color: white;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  padding: 12px 0 20px;
  margin-bottom: 0;
  background: transparent;
  box-shadow: none;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.title-section h2 {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
  margin: 0;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 13px;
  background: transparent;
  padding: 0;
}

/* 操作区域布局 */
.action-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
  flex-wrap: nowrap;
}

/* 搜索框样式 */
.search-box {
  flex: 1;
  min-width: 240px;
  max-width: 480px;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  background: var(--bg-color);
  border-radius: 8px !important;
  padding: 4px 12px;
  box-shadow: none !important;
  transition: all 0.2s ease;
}

.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1) !important;
}

.search-input :deep(.el-input__inner) {
  font-size: 14px;
  height: 40px;
  line-height: 40px;
}

.search-input :deep(.el-input-group__append) {
  display: none;
}

/* 右侧操作区域容器 */
.right-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto; /* 推到右边 */
}



.view-toggle :deep(.el-radio-button__inner) {
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: var(--text-regular);
  transition: all 0.3s ease;
  height: 36px;
  line-height: 20px;
}

.view-toggle :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--primary-color);
  color: white;
  box-shadow: none;
}

.view-toggle :deep(.el-radio-button__inner:hover) {
  color: var(--primary-color);
}

/* 表格视图 */
.resource-table-card {
  background: transparent;
  box-shadow: none;
}

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


/* 文件图标 */
.file-icon {
  color: var(--text-regular);
  font-size: 20px;
  margin-right: 8px;
}

/* 文件标题 */
.file-title {
  color: var(--text-regular);
  font-size: 14px;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 文件信息 */
.file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--text-secondary);
  font-size: 12px;
}

/* 操作按钮 */
.grid-operations {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid var(--border-light);
}

/* 复选框样式 */
.my-resources-checkbox {
  margin: 0;
  white-space: nowrap;
  display: flex;
  align-items: center;
}

/* 按钮组样式 */
.button-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 按钮样式 */
.button-group .el-button {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: normal;
  font-size: 14px;
}

.button-group .el-button--primary {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.button-group .el-button--primary:hover {
  background: var(--primary-color);
  opacity: 0.9;
  transform: none;
  box-shadow: none;
}

/* 分页器 */
.pagination-container {
  margin-top: 16px;
  padding: 16px 0;
  background: transparent;
  box-shadow: none;
}

/* 上传对话框 */
:deep(.upload-dialog .el-dialog) {
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
}

:deep(.upload-dialog .el-upload-dragger) {
  border: thin dashed var(--border-color);
  border-radius: 8px;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .resource-list-container {
    padding: 12px;
  }
  
  .action-section {
    gap: 12px;
  }
  
  .search-box {
    max-width: none;
  }
  
  .resource-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  }
}

/* 响应式布局优化 */
@media screen and (max-width: 1600px) {
  .resource-grid {
    grid-template-columns: repeat(4, 1fr); /* 4列 */
  }
}

@media screen and (max-width: 1200px) {
  .resource-grid {
    grid-template-columns: repeat(3, 1fr); /* 3列 */
  }
}

@media screen and (max-width: 768px) {
  .resource-grid {
    grid-template-columns: repeat(2, 1fr); /* 2列 */
  }
}

@media screen and (max-width: 480px) {
  .resource-grid {
    grid-template-columns: 1fr; /* 1列 */
  }
}

.resource-list-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 20px;
}

.resource-grid {
  margin: 0 -8px;
}

.resource-grid-item {
  padding: 8px;
  height: 100%;
}

.grid-item-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

/* 文件图标响应式 */
.file-icon {
  margin: 16px 0;
}

.file-icon .el-icon {
  font-size: 60px;
}

@media (min-width: 768px) {
  .file-icon .el-icon {
    font-size: 80px;
  }
}

/* 标题和描述响应式 */
.file-title {
  font-size: 15px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.file-desc {
  font-size: 13px;
  margin-bottom: 12px;
  line-height: 1.5;
  height: 40px;
  -webkit-line-clamp: 2;
}

@media (max-width: 767px) {
  .resource-list-container {
    padding: 12px;
  }

  .grid-item-content {
    padding: 12px;
  }

  .file-title {
    font-size: 14px;
  }

  .file-desc {
    font-size: 12px;
    height: 36px;
  }

  /* 移动端操作按钮始终显示 */
  .grid-operations {
    opacity: 1;
    position: static;
    margin-top: 12px;
    text-align: center;
  }
}

/* 文件信息和底部信息栏响应式 */
.file-info,
.file-footer {
  padding: 8px 0;
  font-size: 12px;
}

@media (min-width: 992px) {
  .file-info,
  .file-footer {
    padding: 10px 0;
    font-size: 13px;
  }
}

/* 操作按钮组响应式 */
.grid-operations .el-button-group {
  display: flex;
  gap: 8px;
  justify-content: center;
}

@media (max-width: 767px) {
  .grid-operations .el-button {
    padding: 8px 12px;
  }
}

/* 卡片悬浮效果 */
.el-card {
  transition: all 0.3s ease;
}

.el-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 选中状态 */
.el-card.is-selected {
  border: 2px solid var(--el-color-primary);
}
</style>
