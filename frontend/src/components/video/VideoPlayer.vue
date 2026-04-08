<template>
  <video v-if="src" :src="src" controls class="player"></video>
</template>

<script setup lang="ts">
defineProps<{
  src?: string;
}>();
</script>

<style scoped>
.player {
  width: 100%;
  border-radius: 16px;
  background: #000;
}
</style>
