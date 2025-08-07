<script setup>
import ChatCard from "@/components/ChatCard.vue";

defineProps({
  messageHistory: {
    type: Array,
    required: true
  }
});
</script>

<template>
  <div class="history-modal-overlay" @click="$emit('close')">
    <div class="history-modal">
      <button class="close-button" @click="$emit('close')">&times;</button>
      <h3>对话历史</h3>
      <ul>
        <li v-for="(item, index) in messageHistory" :key="index">
          <ChatCard :role="item.role" :message="item.message" :timestamp="item.timestamp" />
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
/* 遮罩层，覆盖整个屏幕 */
.history-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色背景 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* 确保在最顶层 */
}

/* 模态框样式 */
.history-modal {
  position: relative;
  background: white;
  border-radius: 8px;
  padding: 20px;
  width: 80%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 关闭按钮样式 */
.close-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0 8px;
}

.close-button:hover {
  color: #333;
}

/* 消息列表样式 */
.history-modal ul {
  list-style: none;
  padding: 0;
  margin: 10px 0 0 0;
}

.history-modal li {
  margin-bottom: 15px;
}

.history-modal li:last-child {
  margin-bottom: 0;
}
</style>