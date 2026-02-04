package game.player2.npc.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.player2.npc.config.Player2Config;
import game.player2.npc.dto.ChatCompletionRequest;
import game.player2.npc.dto.ChatCompletionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async HTTP client for the Player2 /v1/chat/completions endpoint.
 */
public class ChatCompletionsClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatCompletionsClient.class);
    private static final String GAME_KEY_HEADER = "player2-game-key";

    private final String baseUrl;
    private final String gameKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executor;

    public ChatCompletionsClient() {
        this.baseUrl = Player2Config.getApiBaseUrl();
        this.gameKey = Player2Config.getGameKey();

        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Player2-ChatCompletions");
            t.setDaemon(true);
            return t;
        });

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(60))
                .executor(executor)
                .build();

        this.gson = new GsonBuilder().create();
    }

    /**
     * Sends a chat completion request asynchronously.
     *
     * @param request The chat completion request
     * @return CompletableFuture with the parsed response
     */
    public CompletableFuture<ChatCompletionResponse> complete(ChatCompletionRequest request) {
        String url = baseUrl + "/v1/chat/completions";
        String body = gson.toJson(request);

        LOGGER.debug("Sending chat completion request to {}", url);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=utf-8")
                .header(GAME_KEY_HEADER, gameKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(60))
                .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        String errorMsg = "Chat completion failed: HTTP " + response.statusCode() + " - " + response.body();
                        LOGGER.error(errorMsg);
                        throw new RuntimeException(errorMsg);
                    }

                    ChatCompletionResponse parsed = gson.fromJson(response.body(), ChatCompletionResponse.class);
                    LOGGER.debug("Chat completion response received (finish_reason: {})", parsed.getFinishReason());
                    return parsed;
                })
                .exceptionally(ex -> {
                    LOGGER.error("Error in chat completion request", ex);
                    throw new RuntimeException("Chat completion request failed: " + ex.getMessage(), ex);
                });
    }

    /**
     * Shuts down the client's thread pool.
     */
    public void shutdown() {
        executor.shutdown();
        LOGGER.info("ChatCompletionsClient shut down");
    }
}
