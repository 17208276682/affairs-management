import { post } from './request'
import type { UploadResponse } from '@/types'

export function uploadFileApi(file: Blob) {
  const formData = new FormData()
  formData.append('file', file)
  return post<UploadResponse>('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function buildPreviewUrl(fileId: string) {
  return `/api/files/${fileId}/preview`
}

export function buildDownloadUrl(fileId: string) {
  return `/api/files/${fileId}/download`
}
