<template>
  <div>
    <h1>测试页面</h1>
    <p>如果你能看到这句话，说明Vue正常工作</p>
    <button @click="count++">计数: {{ count }}</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const count = ref(0)
</script>
