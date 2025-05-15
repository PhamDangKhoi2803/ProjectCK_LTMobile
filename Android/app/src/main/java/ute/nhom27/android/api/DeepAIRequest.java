package ute.nhom27.android.api;

import com.google.gson.annotations.SerializedName;

public class DeepAIRequest {
    @SerializedName("prompt")
    private String prompt;

    @SerializedName("max_tokens")
    private int maxTokens;

    @SerializedName("temperature")
    private float temperature;

    public DeepAIRequest(String prompt) {
        this.prompt = prompt;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }
}