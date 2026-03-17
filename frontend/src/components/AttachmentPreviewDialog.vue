<template>
  <el-dialog
    :model-value="visible"
    :title="title || '附件预览'"
    width="80%"
    top="6vh"
    destroy-on-close
    @update:model-value="(v) => emit('update:visible', v)"
  >
    <div class="preview-wrap">
      <img v-if="previewType === 'image'" :src="url" class="preview-image" alt="preview" />
      <iframe v-else-if="previewType === 'pdf' || previewType === 'text'" :src="url" class="preview-frame" />
      <video v-else-if="previewType === 'video'" :src="url" class="preview-media" controls />
      <audio v-else-if="previewType === 'audio'" :src="url" class="preview-audio" controls />
      <el-empty v-else :description="unsupportedHint" :image-size="64" />
    </div>
    <template #footer>
      <el-button @click="emit('update:visible', false)">关闭</el-button>
      <el-button type="primary" @click="emit('download')">下载</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  visible: boolean
  url: string
  title?: string
  mimeType?: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'download'): void
}>()

const ext = computed(() => {
  const name = props.title || ''
  const idx = name.lastIndexOf('.')
  return idx >= 0 ? name.slice(idx + 1).toLowerCase() : ''
})

const previewType = computed<'image' | 'pdf' | 'text' | 'video' | 'audio' | 'unsupported'>(() => {
  if (!props.url) return 'unsupported'
  const mime = (props.mimeType || '').toLowerCase()

  if (mime.startsWith('image/') || ['png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp', 'svg'].includes(ext.value)) {
    return 'image'
  }
  if (mime === 'application/pdf' || ext.value === 'pdf') {
    return 'pdf'
  }
  if (
    mime.startsWith('text/')
    || mime.includes('json')
    || mime.includes('xml')
    || mime.includes('html')
    || ['txt', 'md', 'json', 'xml', 'csv', 'log', 'html', 'htm'].includes(ext.value)
  ) {
    return 'text'
  }
  if (mime.startsWith('video/') || ['mp4', 'webm', 'ogg', 'mov'].includes(ext.value)) {
    return 'video'
  }
  if (mime.startsWith('audio/') || ['mp3', 'wav', 'ogg', 'm4a', 'aac'].includes(ext.value)) {
    return 'audio'
  }
  return 'unsupported'
})

const unsupportedHint = computed(() => {
  if (!props.url) return '暂无可预览内容'
  const extLabel = ext.value ? `.${ext.value}` : '当前文件'
  return `${extLabel} 暂不支持在线预览，请点击下载`
})
</script>

<style scoped lang="scss">
.preview-wrap {
  min-height: 55vh;
}

.preview-image {
  width: 100%;
  max-height: 72vh;
  object-fit: contain;
}

.preview-frame {
  width: 100%;
  height: 72vh;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.preview-media {
  width: 100%;
  max-height: 72vh;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #000;
}

.preview-audio {
  width: 100%;
  margin-top: 24px;
}
</style>
