package ute.nhom27.android.api;

import com.google.gson.annotations.SerializedName;

public class DeepAIResponse {
    @SerializedName("generations")
    private Generation[] generations;

    public String getResponse() {
        if (generations != null && generations.length > 0) {
            return generations[0].text;
        }
        return "";
    }

    public DeepAIResponse(Generation[] generations) {
        this.generations = generations;
    }

    public Generation[] getGenerations() {
        return generations;
    }

    public void setGenerations(Generation[] generations) {
        this.generations = generations;
    }

    public static class Generation {
        @SerializedName("text")
        public String text;
    }
}