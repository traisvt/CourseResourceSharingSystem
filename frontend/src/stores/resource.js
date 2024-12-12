import { defineStore } from 'pinia'
import axios from '@/utils/axios'
import { ElMessage } from 'element-plus'

export const useResourceStore = defineStore('resource', {
  state: () => ({
    resources: [],
    total: 0,
    loading: false,
    currentPage: 1,
    pageSize: 10
  }),

  actions: {
    async fetchResources(page = 1, size = 10, query = '') {
      this.loading = true
      try {
        const url = query ? '/api/resources/search' : '/api/resources'
        const params = {
          page: Math.max(0, page - 1),  // 转换为后端的0-based页码
          size: size
        }
        if (query) {
          params.query = query
        }
        
        const response = await axios.get(url, { params })
        if (response.data.content) {
          this.resources = response.data.content.map(resource => ({
            ...resource,
            fileType: this.getFileType(resource.fileName || resource.title),
          }))
          this.total = response.data.totalElements || response.data.content.length
        } else {
          this.resources = []
          this.total = 0
        }
        this.currentPage = page
        this.pageSize = size
      } catch (error) {
        console.error('Error fetching resources:', error)
        ElMessage.error(error.response?.data?.message || '获取资源列表失败')
        this.resources = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    async uploadResource(formData) {
      try {
        const response = await axios.post('/api/resources', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          },
          onUploadProgress: (progressEvent) => {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            ElMessage({
              message: `上传进度: ${percentCompleted}%`,
              type: 'info',
              duration: 1000
            })
          }
        })
        ElMessage.success('上传成功')
        await this.fetchResources(this.currentPage, this.pageSize)
        return true
      } catch (error) {
        console.error('Error uploading resource:', error)
        ElMessage.error(error.response?.data?.message || '上传失败')
        return false
      }
    },

    async deleteResource(id) {
      try {
        await axios.delete(`/api/resources/${id}`)
        await this.fetchResources(this.currentPage, this.pageSize)
        return true
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '删除失败')
        return false
      }
    },

    async downloadResource(id, filename) {
      try {
        const response = await axios.get(`/api/resources/${id}/download`, {
          responseType: 'blob',
          onDownloadProgress: (progressEvent) => {
            const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            ElMessage({
              message: `下载进度: ${percentCompleted}%`,
              type: 'info',
              duration: 1000
            })
          }
        })
        
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', filename)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '下载失败')
      }
    },

    // 获取文件类型
    getFileType(fileName) {
      if (!fileName) return '未知'
      const extension = fileName.split('.').pop().toLowerCase()
      const typeMap = {
        pdf: 'PDF',
        doc: 'Word',
        docx: 'Word',
        xls: 'Excel',
        xlsx: 'Excel',
        ppt: 'PPT',
        pptx: 'PPT',
        txt: '文本',
        jpg: '图片',
        jpeg: '图片',
        png: '图片',
        gif: '图片',
        zip: '压缩包',
        rar: '压缩包',
        '7z': '压缩包'
      }
      return typeMap[extension] || extension.toUpperCase()
    }
  }
})
