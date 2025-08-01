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
      <el-button type="primary" plain @click="confirmBackToHome" round>返回首页</el-button>
      <h2>&nbsp;CABM</h2>
    </div>

    <!-- 角色区域 -->
    <div class="character-container">
      <div class="character-image">
        <img id="characterImage" :src="characterImage" alt="AI角色">
      </div>
    </div>

    <!-- 用户输入区域 -->

    <!-- 对话区域 -->
    <div class="dialog-container">
      <GalGameDialog :dialogues="messageList" :character-colors="characterColor"></GalGameDialog>


      <!-- 控制按钮 -->
      <div class="control-buttons">
        <button id="backgroundButton" class="btn secondary-btn" @click="changeBackground">更换背景</button>
        <button id="historyButton" class="btn secondary-btn" @click="showHistoryModal=true">历史</button>
        <button id="characterButton" class="btn secondary-btn" @click="showCharacterModal=true">角色</button>
        <button id="continueButton" class="btn secondary-btn" @click="continueDialog">继续</button>
        <button id="skipButton" class="btn secondary-btn" @click="skipDialog">跳过</button>
      </div>
    </div>

    <div class="user-input-container">
      <el-input v-model="messageInput" placeholder="输入消息..." type="textarea" v-loading="isReplying"
                @keyup.enter.native="sendMessage"></el-input>
      <el-button id="sendButton" class="btn primary-btn" @click="sendMessage">发送</el-button>
    </div>

    <HistoryModal v-if="showHistoryModal" :message-history="messageHistory" @close="showHistoryModal=false"/>
    <CharacterSelector v-if="showCharacterModal" :characters="availableCharacters" @close="showCharacterModal=false" @character-selected="changeCharacter"/>
  </div>
</template>

<script>
import axios from "axios";
import {ElLoading, ElMessage} from "element-plus";
import {ref} from "vue";
const messageHistory= ref([]);
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
    };
  },
  methods: {
    confirmBackToHome() {
      if (confirm('确定要返回首页吗？当前对话将不会保存。')) {
        this.$router.push('/');
      }
    },
    sendMessage() {
      if (this.messageInput.trim() === '') return;
      if (this.isReplying) return; // 如果正在回复，则不允许发送新消息
      this.isReplying = true;
      const message = this.messageInput;
      this.messageInput = '';
      this.updateCurrentMessage('user', message);
      this.addToHistory('user', message);
      let sentence="";
      const eventSource = new EventSource(`/api/chat/stream?message=${encodeURIComponent(message)}`);
      eventSource.onmessage = (event) => {
        if (event.data === '[DONE]') {
          eventSource.close();
          this.updateCurrentMessage('assistant', sentence);
          this.addToHistory('assistant', sentence);
          this.isReplying = false;
          return;
        }
        const string = JSON.parse(event.data)["choices"][0]["delta"]["content"];
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
          if (response.data==='success') {
            ElMessage.success("切换成功");
          }
          else ElMessage.error("切换失败");
        }
      }).finally(()=>{
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
      if (role==='user'){
        this.characterName = '你';
        this.characterColor = '#409eff';
      } else if (role==='assistant') {
        this.characterName = this.currentCharacter.name || 'AI助手';
        this.characterColor = this.currentCharacter.nameColor || '#409eff'; // 使用角色颜色或默认颜色
      } else if (role==='system') {
        this.characterName = '系统';
        this.characterColor = '#00550d'; // 系统消息颜色
      }
      this.messageList.push({role:this.characterName, message});
    },
    addToHistory(role, message) {
      const timestamp = new Date().toLocaleTimeString();
      messageHistory.value.push({ role:this.characterName, message, timestamp });
    },
  },
  async created() {
    await this.loadBackground();
    await this.loadCharacter(); // 正确调用组件方法
  }
};
</script>

<style scoped>
/* 原有样式可以直接移植过来，或者使用CSS模块化 */
.page {
  position: relative;
  width: 100%;
  height: 100vh;
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
  background-color: #f0f2f5;
  background-image: url('../assets/images/default.png');
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
  padding: 12px 16px;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

/* 角色区域样式 - 居中显示 */
.character-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 70%;
}

.character-image {
  max-width: 80%;
  max-height: 80%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.character-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.user-input-container {
  position: fixed;
  right: 20px;
  bottom: 120px;
  display: flex;
  flex-direction: column;
  width: 300px;
  gap: 8px;
}

.dialog-container {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  max-width: 600px;
}

@keyframes pulse {
  0% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.6;
  }
}

.control-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.btn {
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn {
  background-color: transparent;
  border: 1px solid #5c5c5c;
  margin-right: 12px;
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
</style>