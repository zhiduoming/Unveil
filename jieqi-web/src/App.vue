<script setup lang="ts">
import { onMounted } from 'vue'
import { useGameStore } from './stores/game'

const store = useGameStore()
onMounted(() => store.bind())
</script>

<template>
  <RouterView />
</template>
