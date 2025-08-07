<template>
  <div class="options-container">
    <ul class="options-list">
      <li
          v-for="option in optionsArray"
          :key="option"
          class="option-item"
          @click="$emit('select', option)"
      >
        <span class="option-text">{{ option }}</span>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'Options',
  props: {
    optionsArray: {
      type: Array,
      required: true
    }
  }
}
</script>

<style scoped>
/* 基础容器样式 */
.options-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Arial', sans-serif;
}

/* 选项列表样式 */
.options-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 单个选项样式 */
.option-item {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  border-radius: 50px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border: 2px solid #fff;
  text-align: center;
}

/* 悬停效果 */
.option-item:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 6px 12px rgba(255, 154, 158, 0.4);
  background: linear-gradient(135deg, #ff758c 0%, #ff7eb3 100%);
}

/* 点击效果 */
.option-item:active {
  transform: translateY(1px) scale(0.98);
  box-shadow: 0 2px 3px rgba(0, 0, 0, 0.2);
}

/* 选项文字样式 */
.option-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  display: block;
  user-select: none;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .option-item {
    padding: 10px 18px;
  }
  .option-text {
    font-size: 16px;
  }
}
</style>