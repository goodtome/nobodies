<template>
  <div class="dropzone" @dragover.prevent @drop.prevent="onDrop">
    <input ref="inputRef" type="file" hidden multiple accept=".mp4,.mov,.avi,.mkv" @change="onChange" />
    <el-icon size="32"><UploadFilled /></el-icon>
    <p>&#23558;&#35270;&#39057;&#25991;&#20214;&#25302;&#21040;&#36825;&#37324;&#65292;&#25110;&#28857;&#20987;&#19979;&#26041;&#25353;&#38062;&#36873;&#25321;&#25991;&#20214;</p>
    <el-button @click="inputRef?.click()">&#36873;&#25321;&#25991;&#20214;</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { UploadFilled } from '@element-plus/icons-vue';

const emit = defineEmits<{
  (event: 'select', files: File[]): void;
}>();

const inputRef = ref<HTMLInputElement>();

function emitFiles(files?: FileList | File[]) {
  if (!files) {
    return;
  }
  emit('select', Array.from(files));
}

function onChange(event: Event) {
  const target = event.target as HTMLInputElement;
  emitFiles(target.files ?? undefined);
}

function onDrop(event: DragEvent) {
  emitFiles(event.dataTransfer?.files ?? undefined);
}
</script>

<style scoped>
.dropzone {
  border: 2px dashed #7c90c8;
  background: rgba(124, 144, 200, 0.08);
  border-radius: 20px;
  min-height: 240px;
  display: grid;
  place-items: center;
  text-align: center;
  gap: 8px;
  padding: 24px;
}
</style>