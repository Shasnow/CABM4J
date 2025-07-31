<script setup>

</script>

<template>
  <div id="homePage" class="page active">
    <!-- 主页背景 -->
    <div class="home-background"></div>

    <!-- 标题 -->
    <header class="home-header">
      <h1>CABM - Java</h1>
      <p>Code Afflatus & Beyond Matter</p>
      <!-- 动态绑定的java变量 -->
      <p>{{ javaText }}</p>
    </header>

    <!-- 主页按钮 -->
    <div class="home-buttons">
      <button
          id="startButton"
          class="btn primary-btn"
          @click="showChatPage"
      >
        开始
      </button>
      <button
          id="exitButton"
          class="btn secondary-btn"
          @click="exitApplication"
      >
        退出
      </button>
    </div>
  </div>
</template>
<script>
import axios from "axios";
import {ElMessage} from "element-plus";

export default {
  name: 'HomePage',
  data() {
    return {
      javaText: '' // 对应Thymeleaf的${java}变量
    }
  },
  props: {
    // 也可以从父组件接收
    javaTextProp: {
      type: String,
      default: ''
    }
  },
  created() {
    // 模拟从API获取数据（替代Thymeleaf的服务器端渲染）
    this.fetchJavaText();
  },
  methods: {
    showChatPage() {
      // 使用Vue Router导航（推荐方式）
      this.$router.push('/chat');
    },
    exitApplication() {
      // 退出逻辑
      console.log('应用退出');
      // 实际项目中可能是：
      // window.close(); 或调用特定API
    },
    async fetchJavaText() {
      try {
        // 替换为实际的API调用
        axios.get('/api/java-version')
            .then(response => {
                this.javaText = response.data;
            }).catch(error => {
                console.error('获取Java信息失败:', error);
                ElMessage.error('获取Java信息失败');
                this.javaText = "无法加载Java信息";
            });
      } catch (error) {
        console.error('获取Java文本失败:', error);
        this.javaText = "无法加载Java信息";
      }
    }
  }
}
</script>
<style scoped>
.page {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.page.active {
  opacity: 1;
  visibility: visible;
}

.home-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: -1;
}

.home-header {
  text-align: center;
  color: white;
  margin-bottom: 2rem;
}

.home-header h1 {
  font-size: 3rem;
  margin-bottom: 0.5rem;
}

.home-header p {
  font-size: 1.2rem;
  opacity: 0.9;
}

.home-buttons {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.8rem 1.6rem;
  border: none;
  border-radius: 8px;
  font-size: 2rem;
  cursor: pointer;
  transition: all 0.2s;
}

.primary-btn {
  background-color: #5c89ed;
  color: white;
}

.primary-btn:hover {
  background-color: #62abe3;
}

.secondary-btn {
  background-color: #aaaaaa;
  color: white;
}

.secondary-btn:hover {
  background-color: #5c5c5c;
}
</style>