<script setup>
import HistoryModal from "@/components/HistoryModal.vue";
import GalGameDialog from "@/components/GalGameDialog.vue";
import CharacterSelector from "@/components/CharacterSelector.vue";
</script>

<template>
  <div id="chatPage" class="page">
    <!-- 背景图片 -->
    <div class="background-container">
      <div
          v-if="backgroundImage"
          :style="{ backgroundImage: `url('${backgroundImage}')` }"
          class="background-image"
      ></div>
      <div v-else class="background-image default-background"></div>
      <div class="background-overlay"></div>
    </div>

    <!-- 标题栏 -->
    <div class="chat-header">
      <el-button plain round type="primary" @click="confirmBackToHome" size="small">返回首页</el-button>
      <h2>&nbsp;CABM</h2>
    </div>

    <!-- 角色区域 -->
    <div class="character-container">
      <div class="character-image">
        <img id="characterImage" :src="characterImage" alt="AI角色">
      </div>
    </div>

    <!-- 对话区域 -->
    <div class="dialog-container">
      <GalGameDialog :dialogues="messageList" :name-color="characterColor"></GalGameDialog>
    </div>

    <!-- 用户输入区域 -->
    <div class="user-input-container">
      <el-input
          v-model="messageInput"
          v-loading="isReplying"
          placeholder="输入消息..."
          type="textarea"
          :rows="2"
          resize="none"
          @keyup.enter.native="sendMessage"
      ></el-input>
      <el-button id="sendButton" class="btn primary-btn" @click="sendMessage" size="small">发送</el-button>
    </div>

    <!-- 控制按钮 - 在小屏幕上改为垂直布局 -->
    <div class="control-buttons" :class="{ 'vertical-layout': isMobile }">
      <button id="backgroundButton" class="btn secondary-btn" @click="changeBackground">更换背景</button>
      <button id="historyButton" class="btn secondary-btn" @click="showHistoryModal=true">历史</button>
      <button id="characterButton" class="btn secondary-btn" @click="showCharacterModal=true">角色</button>
      <button id="continueButton" class="btn secondary-btn" @click="continueDialog">继续</button>
      <button id="skipButton" class="btn secondary-btn" @click="skipDialog">跳过</button>
    </div>

    <HistoryModal v-if="showHistoryModal" :message-history="messageHistory" @close="showHistoryModal=false"/>
    <CharacterSelector v-if="showCharacterModal" :characters="availableCharacters" @close="showCharacterModal=false"
                       @character-selected="changeCharacter"/>
  </div>
</template>

<script>
import axios from "axios";
import {ElLoading, ElMessage} from "element-plus";
import {ref} from "vue";

const messageHistory = ref([]);
export default {
  name: 'ChatPage',
  props: {
    initialBackground: {type: String, default: ''},
    initialCharacterName: {type: String, default: 'AI助手'},
    initialMessage: {type: String, default: '你好！我是你的AI助手，有什么可以帮你的吗？'},
  },
  data() {
    return {
      backgroundImage: this.initialBackground,
      characterName: this.initialCharacterName,
      characterColor: '#409eff', // 默认颜色
      characterImage: '', // 确保在 data 中定义
      currentCharacter: null, // 改为 data 属性
      availableCharacters: [], // 改为 data 属性
      currentMessage: this.initialMessage,
      messageList: [],
      messageInput: '',
      showCharacterModal: false,
      showHistoryModal: false,
      isLoading: false,
      isReplying: false,
      isMobile: false,
    };
  },
  mounted() {
    this.checkMobile();
    window.addEventListener('resize', this.checkMobile);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile);
  },
  methods: {
    checkMobile() {
      this.isMobile = window.innerWidth <= 768;
    },
    confirmBackToHome() {
      if (confirm('确定要返回首页吗？当前对话将不会保存。')) {
        this.$router.push('/');
      }
    },
    sendMessage() {
      const message = this.messageInput.trim();
      if (message === '') return;
      if (this.isReplying) return; // 如果正在回复，则不允许发送新消息
      this.isReplying = true;
      this.messageInput = '';
      this.updateCurrentMessage('user', message);
      this.addToHistory('user', message);
      let sentence = "";
      const eventSource = new EventSource(`/api/chat/stream?message=${encodeURIComponent(message)}`);
      eventSource.onmessage = (event) => {
        if (event.data === '[DONE]') {
          eventSource.close();
          if (sentence !== '') {
            // 如果有未完成的句子，添加到消息行
            this.updateCurrentMessage('assistant', sentence);
            this.addToHistory('assistant', sentence);
          }
          this.isReplying = false;
          return;
        }
        const string = JSON.parse(event.data)["choices"][0]["delta"]["content"].trim();
        console.log(string)
        for (let i = 0; i < string.length; i++) {
          sentence += string[i];
          if (string[i] === '。' || string[i] === '！' || string[i] === '？' || string[i] === '；') {
            // 如果是标点符号，添加到消息行
            this.updateCurrentMessage('assistant', sentence);
            this.addToHistory('assistant', sentence);
            sentence = '';
          }
        }
      };
      eventSource.onerror = (error) => {
        console.error('SSE error:', error);
        ElMessage.error('消息发送失败，请尝试重启。');
        this.isReplying = false;
        eventSource.close();
      };
    },

    async loadBackground() {
      try {
        const response = await axios.get('/api/background');
        if (response.data) {
          this.backgroundImage = response.data;
        } else {
          console.error('获取背景图片失败，数据格式不正确');
        }
      } catch (error) {
        ElMessage.error('加载背景图片失败')
        console.error('加载背景图片失败:', error);
      }
    },
    async loadCharacter() { // 改为组件方法
      try {
        // 获取角色列表
        const charactersResponse = await axios.get('/api/characters');
        if (charactersResponse.data) {
          console.log(charactersResponse.data);
          this.availableCharacters = charactersResponse.data;
        } else {
          ElMessage.error('获取角色列表失败，数据格式不正确')
          console.error('获取角色列表失败，数据格式不正确');
        }

        // 获取当前角色
        const currentResponse = await axios.get('/api/current-character');
        if (currentResponse.data) {
          console.log(currentResponse.data);
          this.currentCharacter = currentResponse.data;
          // 更新当前角色名称和图片（如果需要）
          if (this.currentCharacter) {
            this.characterName = this.currentCharacter.name;
            this.characterImage = this.currentCharacter.image;
            this.characterColor = this.currentCharacter.nameColor || '#409eff'; // 使用角色颜色或默认颜色
            this.updateCurrentMessage('assistant', this.currentCharacter.welcomeMessage || '你好！我是你的AI助手，有什么可以帮你的吗？');
          }
        } else {
          ElMessage.error('获取当前角色失败，数据格式不正确');
          console.error('获取当前角色失败，数据格式不正确');
        }
      } catch (error) {
        console.error('加载角色数据失败:', error);
        ElMessage.error('加载角色数据失败。');
      }
    },
    changeCharacter(index) {
      if (index < 0 || index >= this.availableCharacters.length) {
        console.error('无效的角色索引:', index);
        return;
      }
      this.currentCharacter = this.availableCharacters[index];
      this.characterName = this.currentCharacter.name;
      this.characterImage = this.currentCharacter.image;
      this.characterColor = this.currentCharacter.nameColor || '#409eff'; // 使用角色颜色或默认颜色
      this.updateCurrentMessage('assistant', this.currentCharacter.welcomeMessage || '你好！我是你的AI助手，有什么可以帮你的吗？');
      this.isReplying = true;
      axios.post(`api/set-character?characterId=${this.currentCharacter.id}`).then(response => {
        if (response.data) {
          if (response.data === 'success') {
            ElMessage.success("切换成功");
          } else ElMessage.error("切换失败");
        }
      }).finally(() => {
        this.isReplying = false;
      })
    },
    async changeBackground() {
      // 检查是否正在处理请求
      const loading = ElLoading.service({
        lock: true,
        text: '正在更换背景...',
        background: 'rgba(255, 255, 255, 0.7)',
      })
      try {
        // 发送API请求
        const response = await axios.post('/api/change-background');
        if (response.data) {
          console.log(response.data);
          this.backgroundImage = response.data;
          this.updateCurrentMessage('system', '背景已更换。');
        }

        // 更新背景图片
      } catch (error) {
        console.error('更换背景失败:', error);
        ElMessage.error('更换背景失败，请稍后再试。');
      } finally {
        // 隐藏加载指示器
        loading.close();
      }
    },
    updateCurrentMessage(role, message) {
      if (role === 'user') {
        this.characterName = '你';
        this.characterColor = '#409eff';
      } else if (role === 'assistant') {
        this.characterName = this.currentCharacter.name || 'AI助手';
        this.characterColor = this.currentCharacter.nameColor || '#409eff'; // 使用角色颜色或默认颜色
      } else if (role === 'system') {
        this.characterName = '系统';
        this.characterColor = '#00550d'; // 系统消息颜色
      }
      this.messageList.push({role: this.characterName, message});
    },
    addToHistory(role, message) {
      const timestamp = new Date().toLocaleTimeString();
      messageHistory.value.push({role: this.characterName, message, timestamp});
    },
  },
  async created() {
    await this.loadBackground();
    await this.loadCharacter(); // 正确调用组件方法
  }
};
</script>

<style scoped>
.page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
}

.background-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.background-image {
  position: absolute;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  transition: background-image 0.5s ease;
}

.default-background {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(156, 156, 156, 0.5);
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.chat-header h2 {
  margin: 0;
  font-size: 1.2rem;
}

/* 角色区域样式 - 响应式调整 */
.character-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 60%;
  max-height: 500px;
  transition: all 0.3s ease;
}

.character-image {
  max-width: 80%;
  max-height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.character-image img {
  /*max-width: 100%;
  max-height: 100%;*/
  object-fit: contain;
}

/* 用户输入区域 - 响应式调整 */
.user-input-container {
  position: fixed;
  right: 15px;
  bottom: 15px;
  display: flex;
  flex-direction: column;
  width: 280px;
  gap: 8px;
  z-index: 100;
  transition: all 0.3s ease;
}

/* 对话区域 - 响应式调整 */
.dialog-container {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 600px;
  max-height: 40vh;
  overflow-y: auto;
  padding-bottom: 20px;
  transition: all 0.3s ease;
}

/* 控制按钮 - 响应式设计 */
.control-buttons {
  position: fixed;
  bottom: 15px;
  left: 15px;
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  max-width: calc(100% - 320px);
  z-index: 100;
  transition: all 0.3s ease;
}

.control-buttons.vertical-layout {
  flex-direction: row;
  width: auto;
  max-width: none;
  right: 15px;
  bottom: 100px;
  align-items: flex-start;
}

.control-buttons.vertical-layout .btn {
  width: auto;
}

.btn {
  padding: 6px 12px;
  border-radius: 16px;
  border: none;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
  white-space: nowrap;
}

.primary-btn {
  background-color: #409eff;
  color: white;
}

.primary-btn:hover {
  background-color: #66b1ff;
}

.secondary-btn {
  background-color: rgba(255, 255, 255, 0.8);
  border: 1px solid #ddd;
}

.secondary-btn:hover {
  background-color: white;
}

/* 小屏幕优化 */
@media (max-width: 768px) {
  .character-container {
    height: 100%;
    top: 55%;
  }

  .dialog-container {
    bottom: 120px;
    width: 95%;
    max-height: 35vh;
  }

  .user-input-container {
    width: calc(100% - 30px);
    right: 15px;
    bottom: 15px;
  }

  .control-buttons {
    flex-direction: column;
    width: auto;
    max-width: none;
    right: 15px;
    bottom: 100px;
    align-items: flex-end;
  }

  .control-buttons .btn {
    width: auto;
  }
}

@media (max-width: 480px) {
  .chat-header h2 {
    font-size: 1rem;
  }

  .character-container {
    height: 45%;
    top: 60%;
  }

  .dialog-container {
    bottom: 130px;
    max-height: 30vh;
  }

  .control-buttons {
    bottom: 90px;
  }

  .btn {
    padding: 4px 8px;
    font-size: 11px;
  }
}
</style>