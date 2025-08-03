<template>
  <div class="dialog-box" @click="handleClick">
    <!-- 角色名（从当前对话条目中获取） -->
    <div v-if="currentDialogue.role" class="character-name" :style="{ color: nameColor }">
      {{ currentDialogue.role }}
    </div>

    <!-- 对话内容 -->
    <div class="dialog-content">
      {{ displayedText }}
      <span v-if="!isTypingComplete" class="cursor">|</span>
    </div>

    <!-- 继续提示 -->
    <div v-if="showContinuePrompt && isTypingComplete" class="continue-prompt">
      ▽
    </div>
  </div>
</template>

<script>
export default {
  name: 'GalGameDialog',
  props: {
    // 对话文本数组（每个元素包含role和message）
    dialogues: {
      type: Array,
      required: true
    },
    // 默认角色名颜色
    defaultCharacterColor: {
      type: String,
      default: '#fff'
    },
    // 角色颜色配置（可选，格式：{ "角色名": "#颜色值" }）
    nameColor: {
      type:String
    },
    // 初始显示延迟(ms)
    initialDelay: {
      type: Number,
      default: 300
    },
    // 基础打字速度(字符/秒)
    baseTypingSpeed: {
      type: Number,
      default: 30
    },
    // 标点符号停顿时间(ms)
    punctuationPause: {
      type: Number,
      default: 300
    }
  },
  data() {
    return {
      // 当前对话索引
      currentDialogueIndex: 0,
      // 已显示的文本
      displayedText: '',
      // 是否完成打字
      isTypingComplete: false,
      // 打字定时器
      typingInterval: null,
      // Ctrl键是否按下
      ctrlPressed: false,
      // 是否全部显示
      allShown: false
    }
  },
  computed: {
    // 当前对话条目
    currentDialogue() {
      return this.dialogues[this.currentDialogueIndex] || { role: '', message: '' }
    },
    // 当前对话内容
    currentMessage() {
      return this.currentDialogue.message
    },
    // 是否显示继续提示
    showContinuePrompt() {
      return this.currentDialogueIndex < this.dialogues.length - 1
    },
    dialoguesLength() {
      return this.dialogues.length
    }
  },
  watch: {
    // 监听对话变化，重新开始打字效果
    currentMessage: {
      immediate: true,
      handler() {
        this.startTypingEffect()
      }
    },
    dialoguesLength: {
      immediate: true,
      handler() {
        if (this.allShown){
          this.currentDialogueIndex++
        }
      }
    }
  },
  mounted() {
    // 监听Ctrl键
    window.addEventListener('keydown', this.handleKeyDown)
    window.addEventListener('keyup', this.handleKeyUp)
  },
  beforeDestroy() {
    // 清除监听器和定时器
    window.removeEventListener('keydown', this.handleKeyDown)
    window.removeEventListener('keyup', this.handleKeyUp)
    this.clearTypingInterval()
  },
  methods: {
    // 开始打字效果
    startTypingEffect() {
      this.clearTypingInterval()
      this.displayedText = ''
      this.isTypingComplete = false
      this.allShown = false

      if (!this.currentMessage) {
        this.isTypingComplete = true
        return
      }

      // 初始延迟
      setTimeout(() => {
        this.typeNextCharacter()
      }, this.initialDelay)
    },

    // 输入下一个字符
    typeNextCharacter() {
      if (this.isTypingComplete) return

      const fullText = this.currentMessage
      const currentLength = this.displayedText.length

      // 如果已经打完所有字符
      if (currentLength >= fullText.length) {
        this.isTypingComplete = true
        if (this.currentDialogueIndex >= this.dialogues.length - 1) {
          this.allShown = true
        }
        return
      }

      // 获取下一个字符
      const nextChar = fullText[currentLength]
      this.displayedText += nextChar

      // 计算延迟时间
      let delay = this.calculateDelay(nextChar)

      // 如果Ctrl按下，加速5倍
      if (this.ctrlPressed) {
        delay = Math.max(20, delay / 5) // 设置最小延迟
      }

      // 设置下一个字符的定时器
      this.typingInterval = setTimeout(() => {
        this.typeNextCharacter()
      }, delay)
    },

    // 计算延迟时间（根据字符类型）
    calculateDelay(char) {
      // 标点符号停顿
      if (/[。！？.!?]/.test(char)) {
        return this.punctuationPause
      }
      // 逗号等中等停顿
      if (/[，,、;；]/.test(char)) {
        return this.punctuationPause * 0.6
      }
      // 默认按速度计算
      return 1000 / this.baseTypingSpeed
    },

    // 处理点击事件
    handleClick() {
      if (!this.isTypingComplete) {
        // 如果没打完，直接显示完整句子
        this.displayedText = this.currentMessage
        this.isTypingComplete = true
        this.clearTypingInterval()
      } else if (this.currentDialogueIndex < this.dialogues.length - 1) {
        // 如果打完了且还有下一条，切换到下一条
        this.currentDialogueIndex++
      }
      this.$emit('dialogue-finish', this.currentDialogueIndex)
    },

    // 处理Ctrl键按下
    handleKeyDown(e) {
      if (e.key === 'Control') {
        this.ctrlPressed = true
        // 如果正在打字，重新开始当前字符的计时以应用加速
        if (!this.isTypingComplete) {
          this.clearTypingInterval()
          this.typeNextCharacter()
        }
      }
    },

    // 处理Ctrl键释放
    handleKeyUp(e) {
      if (e.key === 'Control') {
        this.ctrlPressed = false
      }
    },

    // 清除打字定时器
    clearTypingInterval() {
      if (this.typingInterval) {
        clearTimeout(this.typingInterval)
        this.typingInterval = null
      }
    }
  }
}
</script>

<style scoped>
.dialog-box {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px 30px;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 10px;
  color: white;
  font-size: 18px;
  line-height: 1.6;
  cursor: pointer;
  min-height: 120px;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.dialog-box:hover {
  background-color: rgba(0, 0, 0, 0.8);
}

.character-name {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 10px;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
}

.dialog-content {
  white-space: pre-wrap;
  word-wrap: break-word;
  min-height: 60px;
}

.cursor {
  display: inline-block;
  width: 1px;
  height: 1em;
  background-color: white;
  animation: blink 1s step-end infinite;
  margin-left: 2px;
}

@keyframes blink {
  from, to { opacity: 1; }
  50% { opacity: 0; }
}

.continue-prompt {
  position: absolute;
  right: 20px;
  bottom: 10px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  animation: bounce 1.5s infinite;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-5px); }
  60% { transform: translateY(-3px); }
}
</style>