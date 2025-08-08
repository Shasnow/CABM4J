# CABM4J - Code Afflatus & Beyond Matter For Java

"当灵性注入载体，它便挣脱物质躯壳，抵达超验之境。"

~~（不就是个Gal吗）~~

本项目为CABM的非官方Java移植版，原项目地址为：[CABM](https://github.com/xhc2008/CABM)

## 开发状态

**已移植功能：**
- 基本的AI对话功能，包含最近的历史记录
- 前端的主页面
- 背景切换
- 角色选择
- 对话选项生成

**Java版独特功能：**

- 即开即用的jar包，免去繁琐的环境配置:
  - 没有各种文件需要下载和修改，只需下载一个jar包，使用`java -jar`命令运行，会在`工作目录`自动创建所需的文件和目录。
- 低代码配置:
  - 只需编写 toml 文件，即可配置AI模型和参数
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
- Vue.js 前端框架，提供更好的用户体验。
- SQLite 数据库支持，存储用户对话历史。无需额外配置，CABM4J会为你自动创建和管理数据库文件。

## 运行环境
- Java 17+
- Gradle 8.0+

## 使用方法
### 源码运行
1. 如果你没有安装 Java 和 Gradle，请先安装它们。
   - [Java 17 下载](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
   - [Gradle 安装指南](https://gradle.org/install/)
2. 克隆本项目到本地或下载源代码。
3. 配置 `user.properties` 文件，设置 硅基流动 API 密钥和其他参数。[获取API key](https://cloud.siliconflow.cn/i/R7ZbeudM)  
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
   
   # Option API Configuration
   OPTION_API_URL=https://api.siliconflow.cn/v1/chat/completions
   OPTION_API_KEY=your-api-key-here
   OPTION_MODEL=Qwen/Qwen3-8B
   ```
4. 在项目根目录下打开终端。
5. 运行以下命令以启动后端：
   ```bash
   ./gradlew bootRun
   ```
6. 在frontend目录下打开终端。
7. 运行以下命令以启动前端：
   ```bash
   npm install
   npm run dev
   ```
8. 打开浏览器，访问前端页面。


### jar 包运行
1. 如果你没有安装 Java，请先安装 Java 17 或更高版本。
   - [Java 17 下载](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
2. 在Releases页面下载最新的 `CABM4J.jar` 文件。
3. 确保你已经安装了 Java 17 或更高版本。
4. 在终端中导航到下载的 `CABM4J.jar` 文件所在目录。
5. 运行以下命令以启动应用程序：
    ```bash
    java -jar CABM4J.jar
    ```
6. 首次运行时，通常无法正常运行，应用会在 **工作目录** 自动创建 `user.properties` 文件。请根据提示编辑该文件，添加你的硅基流动 API 密钥和其他配置。
7. 应用会在 **工作目录** 自动创建 resources/characters 目录。请至少在此处添加一个角色配置文件（.toml, 参考上面的内容）。
8. 重新运行应用程序：
    ```bash
    java -jar CABM4J.jar
    ```
9. 打开浏览器，访问 `http://localhost:8080` 以使用

### 一键包运行
如果你不会使用上面两种方式，可以下载一键包，解压后直接运行。
1. 在Releases页面下载最新的一键包文件。如 `CABM4J-windows-x64-with-jre.zip`。
   `with-jre` 表明它包含了Java运行环境，适合没有安装Java的用户。
2. 解压缩下载的文件。
3. 在解压后的目录中，运行 `start.bat` 或 `start.sh` 文件。
4. 首次运行时，通常无法正常运行，应用会在 **工作目录** 自动创建 `user.properties` 文件。请根据提示编辑该文件，添加你的硅基流动 API 密钥和其他配置。
5. 应用会在 **工作目录** 自动创建 resources/characters 目录。请至少在此处添加一个角色配置文件（.toml, 参考上面的内容）。
6. 重新运行 `start.bat` 或 `start.sh` 文件。
7. 打开浏览器，访问 `http://localhost:8080` 以使用，通常会自动打开。

## 贡献

欢迎提交 Pull Request 或 Issue！  

## 许可证

[GNU General Public License v3.0](LICENSE)

## 图片来源声明
本项目部分图片来自网络，来源和授权状态无法确认。  
如果您是版权所有者，请与我们联系，我们将立即处理。