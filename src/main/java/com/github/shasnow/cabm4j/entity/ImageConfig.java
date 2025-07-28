package com.github.shasnow.cabm4j.entity;


import java.util.Map;
import java.util.Random;

/**
 * 图像生成配置类，包含生成图像所需的各种参数
 */
public class ImageConfig {
    private Object model;
    private String prompt; // 图像生成的提示词
    private String imageSize; // 图像尺寸，格式为 "宽x高"，例如 "512x512"
    private int batchSize; // 每次生成的图像数量
    private int inferenceSteps; // 推理步骤数，影响生成图像的质量和细节
    private float guidanceScale; // 引导尺度，控制生成图像与提示词的相关性
    private String negativePrompt; // 负面提示词，用于指定不希望在图像中出现的内容
    private Long seed; // 随机种子，用于生成可重复的图像

    public ImageConfig(Map<String, Object> defaultConfig,String prompt,String negativePrompt) {
        // 获取默认配置
        model = defaultConfig.get("model");
        this.prompt = prompt;
        imageSize = defaultConfig.get("image_size").toString();
        batchSize = Integer.parseInt(defaultConfig.get("batch_size").toString());
        inferenceSteps = Integer.parseInt(defaultConfig.get("inference_steps").toString());
        guidanceScale = Float.parseFloat(defaultConfig.get("guidance_scale").toString());
        this.negativePrompt = negativePrompt;
        seed = new Random().nextLong(9999999999L);
    }
    public ImageConfig build(){
        return this;
    }
    public ImageConfig setModel(Object model) {
        this.model = model;
        return this;
    }
    public ImageConfig setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }
    public ImageConfig setImageSize(String imageSize) {
        this.imageSize = imageSize;
        return this;
    }
    public ImageConfig setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    public ImageConfig setInferenceSteps(int inferenceSteps) {
        this.inferenceSteps = inferenceSteps;
        return this;
    }
    public ImageConfig setGuidanceScale(float guidanceScale) {
        this.guidanceScale = guidanceScale;
        return this;
    }
    public ImageConfig setNegativePrompt(String negativePrompt) {
        this.negativePrompt = negativePrompt;
        return this;
    }
    public ImageConfig setSeed(Long seed) {
        this.seed = seed;
        return this;
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "model", model,
                "prompt", prompt,
                "image_size", imageSize,
                "batch_size", batchSize,
                "num_inference_steps", inferenceSteps,
                "guidance_scale", guidanceScale,
                "negative_prompt", negativePrompt,
                "seed", seed
        );
    }
}
