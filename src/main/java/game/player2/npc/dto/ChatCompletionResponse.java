package game.player2.npc.dto;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Response from the /v1/chat/completions endpoint.
 */
public class ChatCompletionResponse {

    private String id;
    private String object;
    private String model;
    private long created;
    private List<Choice> choices;

    @Nullable
    private Usage usage;

    public String getId() {
        return id;
    }

    public String getObject() {
        return object;
    }

    public String getModel() {
        return model;
    }

    public long getCreated() {
        return created;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    @Nullable
    public Usage getUsage() {
        return usage;
    }

    /**
     * Returns the first choice's message content, or null if unavailable.
     */
    @Nullable
    public String getContent() {
        if (choices == null || choices.isEmpty()) return null;
        AssistantMessage msg = choices.get(0).getMessage();
        return msg != null ? msg.getContent() : null;
    }

    /**
     * Returns the first choice's tool calls, or null if none.
     */
    @Nullable
    public List<ChatCompletionRequest.ToolCall> getToolCalls() {
        if (choices == null || choices.isEmpty()) return null;
        AssistantMessage msg = choices.get(0).getMessage();
        return msg != null ? msg.getToolCalls() : null;
    }

    /**
     * Returns the finish reason of the first choice.
     */
    @Nullable
    public String getFinishReason() {
        if (choices == null || choices.isEmpty()) return null;
        return choices.get(0).getFinishReason();
    }

    // ── Inner classes ──

    public static class Choice {
        private int index;

        @SerializedName("finish_reason")
        private String finishReason;

        private AssistantMessage message;

        public int getIndex() {
            return index;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public AssistantMessage getMessage() {
            return message;
        }
    }

    public static class AssistantMessage {
        private String role;

        @Nullable
        private String content;

        @Nullable
        @SerializedName("tool_calls")
        private List<ChatCompletionRequest.ToolCall> toolCalls;

        public String getRole() {
            return role;
        }

        @Nullable
        public String getContent() {
            return content;
        }

        @Nullable
        public List<ChatCompletionRequest.ToolCall> getToolCalls() {
            return toolCalls;
        }
    }

    public static class Usage {
        @SerializedName("prompt_tokens")
        private long promptTokens;

        @SerializedName("completion_tokens")
        private long completionTokens;

        @SerializedName("total_tokens")
        private long totalTokens;

        public long getPromptTokens() {
            return promptTokens;
        }

        public long getCompletionTokens() {
            return completionTokens;
        }

        public long getTotalTokens() {
            return totalTokens;
        }
    }
}
