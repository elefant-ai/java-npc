package game.player2.npc.dto;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Request body for the /v1/chat/completions endpoint.
 * Follows the OpenAI-compatible format used by the Player2 Developer API.
 */
public class ChatCompletionRequest {

    private final List<ChatMessage> messages;

    @Nullable
    private List<Tool> tools;

    @Nullable
    @SerializedName("tool_choice")
    private String toolChoice;

    @Nullable
    private Float temperature;

    @Nullable
    @SerializedName("max_tokens")
    private Integer maxTokens;

    @Nullable
    private Boolean stream;

    private ChatCompletionRequest(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    @Nullable
    public List<Tool> getTools() {
        return tools;
    }

    @Nullable
    public String getToolChoice() {
        return toolChoice;
    }

    @Nullable
    public Float getTemperature() {
        return temperature;
    }

    @Nullable
    public Integer getMaxTokens() {
        return maxTokens;
    }

    @Nullable
    public Boolean getStream() {
        return stream;
    }

    // ── Inner classes ──

    /**
     * A message in the conversation.
     */
    public static class ChatMessage {
        private final String role;
        private final String content;

        @Nullable
        @SerializedName("tool_calls")
        private List<ToolCall> toolCalls;

        @Nullable
        @SerializedName("tool_call_id")
        private String toolCallId;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public static ChatMessage system(String content) {
            return new ChatMessage("system", content);
        }

        public static ChatMessage user(String content) {
            return new ChatMessage("user", content);
        }

        public static ChatMessage assistant(String content) {
            return new ChatMessage("assistant", content);
        }

        public static ChatMessage assistantWithToolCalls(String content, List<ToolCall> toolCalls) {
            ChatMessage msg = new ChatMessage("assistant", content);
            msg.toolCalls = toolCalls;
            return msg;
        }

        public static ChatMessage toolResult(String toolCallId, String content) {
            ChatMessage msg = new ChatMessage("tool", content);
            msg.toolCallId = toolCallId;
            return msg;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }

        @Nullable
        public List<ToolCall> getToolCalls() {
            return toolCalls;
        }

        @Nullable
        public String getToolCallId() {
            return toolCallId;
        }
    }

    /**
     * A tool available to the model.
     */
    public static class Tool {
        private final String type;
        private final ToolFunction function;

        public Tool(ToolFunction function) {
            this.type = "function";
            this.function = function;
        }

        public String getType() {
            return type;
        }

        public ToolFunction getFunction() {
            return function;
        }
    }

    /**
     * Function definition within a tool.
     */
    public static class ToolFunction {
        private final String name;
        private final String description;

        @Nullable
        private final JsonObject parameters;

        public ToolFunction(String name, String description, @Nullable JsonObject parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Nullable
        public JsonObject getParameters() {
            return parameters;
        }
    }

    /**
     * A tool call made by the model.
     */
    public static class ToolCall {
        @Nullable
        private final String id;

        @Nullable
        private final String type;

        private final FunctionCall function;

        public ToolCall(@Nullable String id, @Nullable String type, FunctionCall function) {
            this.id = id;
            this.type = type;
            this.function = function;
        }

        @Nullable
        public String getId() {
            return id;
        }

        @Nullable
        public String getType() {
            return type;
        }

        public FunctionCall getFunction() {
            return function;
        }
    }

    /**
     * Function call details within a tool call.
     */
    public static class FunctionCall {
        private final String name;
        private final String arguments;

        public FunctionCall(String name, String arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        /**
         * Returns the arguments as a JSON-encoded string.
         */
        public String getArguments() {
            return arguments;
        }
    }

    // ── Builder ──

    public static class Builder {
        private final List<ChatMessage> messages = new ArrayList<>();
        private List<Tool> tools;
        private String toolChoice;
        private Float temperature;
        private Integer maxTokens;
        private Boolean stream;

        public Builder addMessage(ChatMessage message) {
            this.messages.add(message);
            return this;
        }

        public Builder messages(List<ChatMessage> messages) {
            this.messages.addAll(messages);
            return this;
        }

        public Builder tools(List<Tool> tools) {
            this.tools = tools;
            return this;
        }

        public Builder toolChoice(String toolChoice) {
            this.toolChoice = toolChoice;
            return this;
        }

        public Builder temperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public Builder stream(boolean stream) {
            this.stream = stream;
            return this;
        }

        public ChatCompletionRequest build() {
            if (messages.isEmpty()) {
                throw new IllegalStateException("At least one message is required");
            }
            ChatCompletionRequest request = new ChatCompletionRequest(new ArrayList<>(messages));
            request.tools = tools;
            request.toolChoice = toolChoice;
            request.temperature = temperature;
            request.maxTokens = maxTokens;
            request.stream = stream;
            return request;
        }
    }
}
