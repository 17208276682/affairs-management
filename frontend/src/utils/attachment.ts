import type { UploadUserFile } from 'element-plus'
import type { Attachment } from '@/types'
import { uploadFileApi, buildDownloadUrl, buildPreviewUrl } from '@/api/file'

export async function uploadAttachments(fileList: UploadUserFile[]): Promise<Attachment[]> {
  const files = fileList.reduce<Blob[]>((acc, item) => {
    if (item.raw) acc.push(item.raw)
    return acc
  }, [])

  if (!files.length) return []

  const result: Attachment[] = []
  for (const file of files) {
    const res = await uploadFileApi(file)
    const uploaded = res.data
    result.push({
      id: uploaded.id,
      name: uploaded.name,
      url: uploaded.url || buildPreviewUrl(uploaded.id),
      size: uploaded.size,
      type: uploaded.type,
      uploadedAt: uploaded.uploadedAt,
    })
  }
  return result
}

export function openAttachmentPreview(fileId: string) {
  window.open(buildPreviewUrl(fileId), '_blank')
}

export function openAttachmentDownload(fileId: string) {
  window.open(buildDownloadUrl(fileId), '_blank')
}

export function getUploadFilePreview(file: UploadUserFile) {
  if (!file.raw) return null
  const objectUrl = URL.createObjectURL(file.raw)
  return {
    url: objectUrl,
    name: file.name || '未命名附件',
    type: file.raw.type || '',
    revoke: () => URL.revokeObjectURL(objectUrl),
  }
}

export function downloadUploadFile(file: UploadUserFile) {
  if (!file.raw) return
  const url = URL.createObjectURL(file.raw)
  const a = document.createElement('a')
  a.href = url
  a.download = file.name || 'attachment'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
