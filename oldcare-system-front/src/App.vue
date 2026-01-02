<template>
  <RouterView />
</template>

<script setup>
import { useAuthStore } from './store/auth'

// 在app初始化时hydrate auth store
const auth = useAuthStore()
if (!auth.initialized) {
  auth.hydrate()
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  width: 100%;
  height: 100%;
}
</style>
