<template>
  <el-card class="chat-card" shadow="hover">
    <template #header>
      <div class="chat-card__header">
        <el-tag size="large">
          {{ role }}
        </el-tag>
      </div>
    </template>

    <div class="chat-card__content">
      {{ message }}
    </div>

    <template #footer>
      <div class="chat-card__footer">
        <el-text size="small" type="info">{{ formattedTimestamp }}</el-text>
      </div>
    </template>
  </el-card>
</template>

<script>
export default {
  name: 'ChatCard',
  props: {
    role: {
      type: String,
      required: true
    },
    message: {
      type: String,
      required: true
    },
    timestamp: {
      type: [String, Number],
      required: true
    }
  },
  computed: {
    formattedTimestamp() {
      if (typeof this.timestamp === 'number') {
        return new Date(this.timestamp).toLocaleTimeString();
      }
      return this.timestamp;
    }
  }
};
</script>

<style scoped>
.chat-card {
  margin-bottom: 8px; /* 减少卡片间距 */
  border-radius: 6px; /* 稍微缩小圆角 */
  transition: all 0.2s ease;
}

.chat-card :deep(.el-card__header) {
  padding: 8px 12px; /* 减少头部内边距 */
  border-bottom: none; /* 移除头部下边框 */
}

.chat-card :deep(.el-card__body) {
  padding: 0 12px 8px; /* 减少主体内边距 */
}

.chat-card :deep(.el-card__footer) {
  padding: 0 12px 8px; /* 减少底部内边距 */
  border-top: none; /* 移除底部上边框 */
}

.chat-card__header {
  display: flex;
  align-items: center;
}

.chat-card__content {
  padding: 4px 0; /* 减少内容内边距 */
  line-height: 1.4; /* 缩小行高 */
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 0.95em; /* 稍微缩小字体 */
}

.chat-card__footer {
  text-align: right;
  margin-top: 4px; /* 减少底部间距 */
}

/* 悬停效果微调 */
.chat-card:hover {
  box-shadow: 0 1px 8px 0 rgba(0, 0, 0, 0.1) !important;
}
</style>