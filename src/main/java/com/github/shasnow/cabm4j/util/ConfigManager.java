package com.github.shasnow.cabm4j.util;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConfigManager {
    private static final Map<String, Object> CHAT_CONFIG = Map.of(
            "model", PropertiesManager.getProperty("CHAT_MODEL", "deepseek-ai/DeepSeek-V3"),
            "max_tokens", 4096,   // 最大生成令牌数
            "top_k", 5,           // Top-K采样
            "temperature", 1.0,   // 温度参数，控制创造性
            "stream", true      // 默认使用流式输出
    );
    private static final Map<String, Object> STREAM_CONFIG = Map.of(
            "enable_streaming", true
    );
    private static final Map<String, ?> MEMORY_CONFIG = Map.of(
            "top_k", 5,                   // 记忆检索返回的最相似结果数量
            "timeout", 10,                // 记忆检索超时时间（秒）
            "min_similarity", 0.3       // 最小相似度阈值
    );
    private static final Map<String, Object> IMAGE_CONFIG = Map.of(
            "model", PropertiesManager.getProperty("IMAGE_MODEL", "Kwai-Kolors/Kolors"),  // 默认模型
            "image_size", "1024x1024",      // 默认图像尺寸
            "batch_size", 1,                // 默认生成数量
            "inference_steps", 20,      // 推理步数
            "guidance_scale", 7.5            // 引导比例
    );
    private static final Map<String, String> SYSTEM_PROMPTS = Map.of(
            "default", "你需要用一段话（1到5句话）回复用户，禁止换行，禁止使用markdown。每**句**话的开头需要用【】加上当前的心情，且必须是其中之一，**只写序号**：1.平静 2.兴奋 3.愤怒 4.失落 "
    );
    private static final List<String> IMAGE_PROMPTS = List.of(
            "繁星点缀的夜空下，一片宁静的湖泊倒映着群山和森林，远处有篝火和小屋",
            "阳光透过云层，照耀在广阔的草原上，野花盛开，远处有山脉和小溪",
            "雪花飘落的冬日森林，松树覆盖着白雪，小路蜿蜒，远处有小木屋和炊烟",
            "雨后的城市街道，霓虹灯反射在湿润的路面上，行人撑着伞，远处是城市天际线",
            "一间温馨的二次元风格卧室，阳光透过薄纱窗帘洒在木地板上,床上散落着卡通抱枕，墙边有摆满书籍和手办的原木色书架.书桌上亮着一盏小台灯，电脑屏幕泛着微光，窗外隐约可见樱花树。画面线条柔和，色彩清新，带有动画般的细腻阴影和高光。"
    );
    private static final String NEGATIVE_PROMPTS = "模糊, 扭曲, 变形, 低质量, 像素化, 低分辨率, 不完整";
    private static final Map<String, Object> APP_CONFIG = Map.of(
            "debug", PropertiesManager.getProperty("DEBUG", "false").equalsIgnoreCase("true"),
            "port", Integer.parseInt(PropertiesManager.getProperty("PORT", "5000")),
            "host", PropertiesManager.getProperty("HOST", "0.0.0.0"),  // 服务器监听地址，
            "character_folder", "resources/characters",  // 角色配置文件夹
            "image_cache_dir", "resources/images/cache",
            "max_history_length", 10,  // 最大对话历史长度（发送给AI的上下文长度）
            "show_scene_name", true,  // 是否在前端显示场景名称
            "auto_open_browser", PropertiesManager.getProperty("AUTO_OPEN_BROWSER", "True").equalsIgnoreCase("true")  // 是否自动打开浏览器（会自动使用本地IP地址）
    );

    public static Map<String, Object> getChatConfig() {
        return CHAT_CONFIG;
    }

    public static Map<String, Object> getStreamConfig() {
        return STREAM_CONFIG;
    }

    public static Map<String, ?> getMemoryConfig() {
        return MEMORY_CONFIG;
    }

    public static Map<String, Object> getImageConfig() {
        return IMAGE_CONFIG;
    }

    public static Map<String, String> getSystemPrompts() {
        return SYSTEM_PROMPTS;
    }

    public static List<String> getImagePrompts() {
        return IMAGE_PROMPTS;
    }

    public static String getNegativePrompts() {
        return NEGATIVE_PROMPTS;
    }

    public static Map<String, Object> getAppConfig() {
        return APP_CONFIG;
    }

    public static String getRandomImagePrompt() {
        Random random = new Random();
        return IMAGE_PROMPTS.get(random.nextInt(IMAGE_PROMPTS.size()));
    }

    public static String getSystemPrompt(String key) {
        return SYSTEM_PROMPTS.get(key);
    }
}
