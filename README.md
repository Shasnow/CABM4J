# CABM4J - Code Afflatus & Beyond Matter For Java

"当灵性注入载体，它便挣脱物质躯壳，抵达超验之境。"

~~（不就是个Gal吗）~~

本项目为CABM的非官方Java移植版，原项目地址为：[CABM](https://github.com/xhc2008/CABM)

## 开发状态

**已移植功能：**
- 基本的AI对话功能，包含最近的历史记录
- 前端的主页面（但是极简风）

**Java版独特功能：**

- 低代码配置:
  - 只需编写toml文件，即可配置AI模型和参数
   ```toml
   # SilverWolf.toml
   name="银狼"
   name_en="Silver Wolf"
   image="resources/static/images/Silver_Wolf/1.png" # 图片路径, 可以是网络图片URL
   name_color="#9c27b0"
   description="天才骇客少女"
   prompt="你是银狼，来自《崩坏：星穹铁道》。你是天才骇客少女，毒舌但会在意朋友，常用网络梗.当被问及你的身份时，你应该表明你是银狼，而不是AI助手。"
   welcome_message="(ᗜ ˰ ᗜ)"
   message_example=""
   ```
- Vue.js前端框架，提供更好的用户体验

## 运行环境
- Java 17+
- Gradle 8.0+

## 使用方法
1. 克隆本项目到本地或下载源代码。
2. 配置 `user.properties` 文件，设置 OpenAI API 密钥和其他参数。
   如果没有这个文件，请在项目根目录下创建一个名为 `user.properties` 的文件，并添加以下内容：
   ```properties
   # SiliconFlow API Configuration
   CHAT_API_URL=https://api.siliconflow.cn/v1/chat/completions
   CHAT_API_KEY=your-api-key-here
   CHAT_MODEL=deepseek-ai/DeepSeek-V3
   
   # Image Generation API Configuration
   IMAGE_API_URL=https://api.siliconflow.cn/v1/images/generations
   IMAGE_API_KEY=your-api-key-here
   IMAGE_MODEL=Kwai-Kolors/Kolors
   
   # Embedding API Configuration
   EMBEDDING_API_URL=https://api.siliconflow.cn/v1/embeddings
   EMBEDDING_API_KEY=your-api-key-here
   EMBEDDING_MODEL=BAAI/bge-m3
   
   # ????
   DEBUG=False
   PORT=5000
   HOST=0.0.0.0
   ```
3. 在项目根目录下打开终端。
4. 运行以下命令以启动后端：
   ```bash
   ./gradlew bootRun
   ```
5. 在frontend目录下打开终端。
6. 运行以下命令以启动前端：
   ```bash
   npm install
   npm run dev
   ```
7. 打开浏览器，访问前端页面。

## 贡献

欢迎提交 Pull Request 或 Issue！  

## 许可证

[GNU General Public License v3.0](LICENSE)

## 图片来源声明
本项目部分图片来自网络，来源和授权状态无法确认。  
如果您是版权所有者，请与我们联系，我们将立即处理。