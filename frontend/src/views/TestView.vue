<template>
  <div>
    <h1>测试页面</h1>
    <p>这是一个测试页面。</p>
  </div>
  <Options :options-array="list" @select="(o)=>{console.log(o)}"></Options>
</template>

<style scoped>

</style>
<script setup lang="ts">
const list=["选项1", "选项2", "选项3"];
import Options from "@/components/Options.vue";
</script>