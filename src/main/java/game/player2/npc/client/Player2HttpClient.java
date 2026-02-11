package game.player2.npc.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import game.player2.npc.dto.ChatRequest;
import game.player2.npc.dto.SpawnNpcRequest;
import game.player2.npc.dto.TtsSpeakRequest;
import game.player2.npc.dto.TtsSpeakResponse;
import game.player2.npc.dto.TtsVoice;
import game.player2.npc.dto.TtsVoicesResponse;
import game.player2.npc.event.NpcErrorEvent;
import game.player2.npc.event.Player2EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async HTTP client for the Player2 NPC API.
 */
public class Player2HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(Player2HttpClient.class);
    private static final String GAME_KEY_HEADER = "player2-game-key";

    private final String baseUrl;
    private final String gameKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private final ExecutorService executor;

    private final Map<String, SseStreamHandler> activeStreams = new ConcurrentHashMap<>();

    public Player2HttpClient(String baseUrl, String gameKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.gameKey = gameKey;

        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Player2-HTTP");
            t.setDaemon(true);
            return t;
        });

        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(30))
            .executor(executor)
            .build();

        this.gson = new GsonBuilder().create();
    }

    /**
     * Spawns a new NPC asynchronously.
     *
     * @param gameId The game session ID
     * @param request The spawn request
     * @return CompletableFuture with the new NPC's UUID
     */
    public CompletableFuture<UUID> spawnNpc(String gameId, SpawnNpcRequest request) {
        String url = baseUrl + "/v1/npc/games/" + gameId + "/npcs/spawn";
        String body = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json; charset=utf-8")
            .header("accept", "text/plain")
            .header(GAME_KEY_HEADER, gameKey)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    handleHttpError(response, "spawn NPC", null, gameId);
                    throw new RuntimeException("Failed to spawn NPC: HTTP " +
                        response.statusCode() + " - " + response.body());
                }
                String uuidStr = response.body().replace("\"", "").trim();
                LOGGER.info("Spawned NPC {} in game {}", uuidStr, gameId);
                return UUID.fromString(uuidStr);
            })
            .exceptionally(ex -> {
                LOGGER.error("Error spawning NPC in game {}", gameId, ex);
                Player2EventBus.getInstance().postErrorEvent(new NpcErrorEvent(
                    NpcErrorEvent.ErrorType.HTTP_ERROR,
                    "Failed to spawn NPC: " + ex.getMessage(),
                    null, gameId, ex
                ));
                throw new RuntimeException(ex);
            });
    }

    /**
     * Sends a chat message to an NPC asynchronously.
     *
     * @param gameId The game session ID
     * @param npcId The NPC's UUID
     * @param request The chat request
     * @return CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<Void> sendChat(String gameId, UUID npcId, ChatRequest request) {
        String url = baseUrl + "/v1/npc/games/" + gameId + "/npcs/" + npcId + "/chat";
        String body = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json; charset=utf-8")
            .header(GAME_KEY_HEADER, gameKey)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
            .thenAccept(response -> {
                if (response.statusCode() != 200) {
                    handleHttpError(response, "send chat", npcId, gameId);
                    throw new RuntimeException("Failed to send chat: HTTP " + response.statusCode());
                }
                LOGGER.debug("Chat sent to NPC {} in game {}", npcId, gameId);
            })
            .exceptionally(ex -> {
                LOGGER.error("Error sending chat to NPC {} in game {}", npcId, gameId, ex);
                Player2EventBus.getInstance().postErrorEvent(new NpcErrorEvent(
                    NpcErrorEvent.ErrorType.HTTP_ERROR,
                    "Failed to send chat: " + ex.getMessage(),
                    npcId, gameId, ex
                ));
                return null;
            });
    }

    /**
     * Kills an NPC asynchronously.
     *
     * @param gameId The game session ID
     * @param npcId The NPC's UUID
     * @return CompletableFuture that completes when the NPC is killed
     */
    public CompletableFuture<Void> killNpc(String gameId, UUID npcId) {
        String url = baseUrl + "/v1/npc/games/" + gameId + "/npcs/" + npcId + "/kill";

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header(GAME_KEY_HEADER, gameKey)
            .POST(HttpRequest.BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(30))
            .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
            .thenAccept(response -> {
                if (response.statusCode() != 200 && response.statusCode() != 404) {
                    handleHttpError(response, "kill NPC", npcId, gameId);
                    throw new RuntimeException("Failed to kill NPC: HTTP " + response.statusCode());
                }
                LOGGER.info("Killed NPC {} in game {}", npcId, gameId);
            })
            .exceptionally(ex -> {
                LOGGER.error("Error killing NPC {} in game {}", npcId, gameId, ex);
                Player2EventBus.getInstance().postErrorEvent(new NpcErrorEvent(
                    NpcErrorEvent.ErrorType.HTTP_ERROR,
                    "Failed to kill NPC: " + ex.getMessage(),
                    npcId, gameId, ex
                ));
                return null;
            });
    }

    /**
     * Sends a health check/heartbeat to the API.
     *
     * @return CompletableFuture with true if healthy
     */
    public CompletableFuture<Boolean> checkHealth() {
        String url = baseUrl + "/v1/health";

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header(GAME_KEY_HEADER, gameKey)
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                boolean healthy = response.statusCode() == 200;
                LOGGER.debug("Health check: {}", healthy ? "OK" : "FAILED");
                return healthy;
            })
            .exceptionally(ex -> {
                LOGGER.warn("Health check failed: {}", ex.getMessage());
                return false;
            });
    }

    /**
     * Starts the SSE stream for receiving NPC responses.
     *
     * @param gameId The game session ID
     */
    public void startSseStream(String gameId) {
        String url = baseUrl + "/v1/npc/games/" + gameId + "/npcs/responses";

        SseStreamHandler handler = new SseStreamHandler(
            httpClient, url, gameKey, gameId, gson, executor
        );

        // Atomic check-and-put: only start if we're the first to add
        if (activeStreams.putIfAbsent(gameId, handler) == null) {
            handler.start();
            LOGGER.info("Started SSE stream for game {}", gameId);
        } else {
            LOGGER.debug("SSE stream already active for game {}", gameId);
        }
    }

    /**
     * Stops the SSE stream for a game.
     *
     * @param gameId The game session ID
     */
    public void stopSseStream(String gameId) {
        SseStreamHandler handler = activeStreams.remove(gameId);
        if (handler != null) {
            handler.stop();
            LOGGER.info("Stopped SSE stream for game {}", gameId);
        }
    }

    /**
     * Checks if an SSE stream is active for the given game.
     *
     * @param gameId The game session ID
     * @return true if stream is active
     */
    public boolean isStreamActive(String gameId) {
        return activeStreams.containsKey(gameId);
    }

    // ==================== TTS API ====================

    /**
     * Speaks text aloud via the Player2 app's TTS engine.
     *
     * @param request The TTS speak request
     * @return CompletableFuture with the TTS response
     */
    public CompletableFuture<TtsSpeakResponse> ttsSpeak(TtsSpeakRequest request) {
        String url = baseUrl + "/v1/tts/speak";
        String body = gson.toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json; charset=utf-8")
            .header(GAME_KEY_HEADER, gameKey)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofSeconds(30))
            .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    handleHttpError(response, "TTS speak", null, null);
                    throw new RuntimeException("Failed TTS speak: HTTP " +
                        response.statusCode() + " - " + response.body());
                }
                return gson.fromJson(response.body(), TtsSpeakResponse.class);
            })
            .exceptionally(ex -> {
                LOGGER.error("Error calling TTS speak", ex);
                Player2EventBus.getInstance().postErrorEvent(new NpcErrorEvent(
                    NpcErrorEvent.ErrorType.HTTP_ERROR,
                    "Failed TTS speak: " + ex.getMessage(),
                    null, null, ex
                ));
                return null;
            });
    }

    /**
     * Stops any currently playing TTS audio.
     *
     * @return CompletableFuture that completes when playback is stopped
     */
    public CompletableFuture<Void> ttsStop() {
        String url = baseUrl + "/v1/tts/stop";

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header(GAME_KEY_HEADER, gameKey)
            .POST(HttpRequest.BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(10))
            .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.discarding())
            .thenAccept(response -> {
                if (response.statusCode() != 200) {
                    LOGGER.warn("TTS stop returned HTTP {}", response.statusCode());
                }
            })
            .exceptionally(ex -> {
                LOGGER.warn("Error stopping TTS: {}", ex.getMessage());
                return null;
            });
    }

    /**
     * Lists available TTS voices.
     *
     * @return CompletableFuture with the list of available voices
     */
    public CompletableFuture<List<TtsVoice>> ttsGetVoices() {
        String url = baseUrl + "/v1/tts/voices";

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header(GAME_KEY_HEADER, gameKey)
            .GET()
            .timeout(Duration.ofSeconds(10))
            .build();

        return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    handleHttpError(response, "TTS get voices", null, null);
                    throw new RuntimeException("Failed to get voices: HTTP " + response.statusCode());
                }
                TtsVoicesResponse parsed = gson.fromJson(response.body(), TtsVoicesResponse.class);
                return parsed.getVoices();
            })
            .exceptionally(ex -> {
                LOGGER.error("Error getting TTS voices", ex);
                return List.of();
            });
    }

    /**
     * Shuts down the client and all active streams.
     */
    public void shutdown() {
        activeStreams.values().forEach(SseStreamHandler::stop);
        activeStreams.clear();
        executor.shutdown();
        LOGGER.info("Player2HttpClient shut down");
    }

    private void handleHttpError(HttpResponse<?> response, String operation, UUID npcId, String gameId) {
        NpcErrorEvent.ErrorType errorType;
        switch (response.statusCode()) {
            case 401 -> errorType = NpcErrorEvent.ErrorType.AUTH_ERROR;
            case 402 -> errorType = NpcErrorEvent.ErrorType.INSUFFICIENT_CREDITS;
            case 404 -> errorType = NpcErrorEvent.ErrorType.NPC_NOT_FOUND;
            default -> errorType = NpcErrorEvent.ErrorType.HTTP_ERROR;
        }

        Player2EventBus.getInstance().postErrorEvent(new NpcErrorEvent(
            errorType,
            "Failed to " + operation + ": HTTP " + response.statusCode(),
            npcId, gameId, null
        ));
    }
}
