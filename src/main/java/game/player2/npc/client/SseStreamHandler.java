package game.player2.npc.client;

import com.google.gson.Gson;
import game.player2.npc.dto.CommandResponse;
import game.player2.npc.dto.NpcResponse;
import game.player2.npc.event.NpcCommandEvent;
import game.player2.npc.event.NpcConnectionEvent;
import game.player2.npc.event.NpcErrorEvent;
import game.player2.npc.event.NpcMessageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles SSE stream connection and fires NeoForge events for NPC responses.
 * Implements automatic reconnection with exponential backoff.
 */
public class SseStreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SseStreamHandler.class);
    private static final String GAME_KEY_HEADER = "player2-game-key";

    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long INITIAL_RECONNECT_DELAY_MS = 1000;
    private static final long MAX_RECONNECT_DELAY_MS = 60000;

    private final HttpClient httpClient;
    private final String url;
    private final String gameKey;
    private final String gameId;
    private final Gson gson;
    private final ExecutorService executor;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private volatile Thread streamThread;

    public SseStreamHandler(HttpClient httpClient, String url, String gameKey,
                           String gameId, Gson gson, ExecutorService executor) {
        this.httpClient = httpClient;
        this.url = url;
        this.gameKey = gameKey;
        this.gameId = gameId;
        this.gson = gson;
        this.executor = executor;
    }

    /**
     * Starts the SSE stream in a background thread.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            executor.submit(this::runStreamLoop);
        }
    }

    /**
     * Stops the SSE stream.
     */
    public void stop() {
        running.set(false);
        if (streamThread != null) {
            streamThread.interrupt();
        }
    }

    private void runStreamLoop() {
        streamThread = Thread.currentThread();

        while (running.get()) {
            try {
                connectAndProcess();
            } catch (InterruptedException e) {
                LOGGER.debug("SSE stream interrupted for game {}", gameId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (!running.get()) {
                    break;
                }

                handleDisconnect(e);

                if (!attemptReconnect()) {
                    break;
                }
            }
        }

        running.set(false);
        NeoForge.EVENT_BUS.post(new NpcConnectionEvent(
            gameId, NpcConnectionEvent.Status.DISCONNECTED, "Stream stopped"
        ));
    }

    private void connectAndProcess() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/x-json-stream")
            .header(GAME_KEY_HEADER, gameKey)
            .GET()
            .timeout(Duration.ofDays(1))
            .build();

        HttpResponse<java.io.InputStream> response = httpClient.send(
            request, HttpResponse.BodyHandlers.ofInputStream()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("SSE connection failed: HTTP " + response.statusCode());
        }

        reconnectAttempts.set(0);

        NeoForge.EVENT_BUS.post(new NpcConnectionEvent(
            gameId, NpcConnectionEvent.Status.CONNECTED, "Connected to SSE stream"
        ));

        LOGGER.info("SSE stream connected for game {}", gameId);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body()))) {

            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    processJsonLine(line.trim());
                }
            }
        }
    }

    /**
     * Strips the NPC name prefix (e.g., "<citizen_1_2>") from the message.
     */
    private String stripNpcPrefix(String message) {
        if (message == null) return null;
        // Pattern: <anything> at the start, followed by optional whitespace
        return message.replaceFirst("^<[^>]+>\\s*", "");
    }

    private void processJsonLine(String line) {
        try {
            NpcResponse response = gson.fromJson(line, NpcResponse.class);

            if (response == null || response.getNpcId() == null) {
                LOGGER.warn("Received invalid NPC response: {}", line);
                return;
            }

            UUID npcId = response.getNpcId();

            // Fire message event if there's a message
            String message = stripNpcPrefix(response.getMessage());
            if (message != null && !message.isEmpty()) {
                String audioData = null;
                if (response.getAudio() != null) {
                    audioData = response.getAudio().getData();
                }

                NpcMessageEvent messageEvent = new NpcMessageEvent(
                    npcId, gameId, message, audioData
                );
                NeoForge.EVENT_BUS.post(messageEvent);

                LOGGER.debug("Fired NpcMessageEvent for NPC {} in game {}", npcId, gameId);
            }

            // Fire command events if there are commands
            if (response.getCommands() != null && !response.getCommands().isEmpty()) {
                for (CommandResponse cmd : response.getCommands()) {
                    NpcCommandEvent commandEvent = new NpcCommandEvent(
                        npcId, gameId, cmd.getName(), cmd.getArgumentsJson()
                    );
                    NeoForge.EVENT_BUS.post(commandEvent);

                    LOGGER.debug("Fired NpcCommandEvent for command '{}' from NPC {} in game {}",
                        cmd.getName(), npcId, gameId);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error processing SSE line: {}", line, e);
            NeoForge.EVENT_BUS.post(new NpcErrorEvent(
                NpcErrorEvent.ErrorType.PARSE_ERROR,
                "Failed to parse NPC response: " + e.getMessage(),
                null, gameId, e
            ));
        }
    }

    private void handleDisconnect(Exception e) {
        LOGGER.warn("SSE stream disconnected for game {}: {}", gameId, e.getMessage());
        NeoForge.EVENT_BUS.post(new NpcConnectionEvent(
            gameId, NpcConnectionEvent.Status.DISCONNECTED, e.getMessage()
        ));
    }

    private boolean attemptReconnect() {
        int attempts = reconnectAttempts.incrementAndGet();

        if (attempts > MAX_RECONNECT_ATTEMPTS) {
            LOGGER.error("Max reconnection attempts ({}) reached for game {}",
                MAX_RECONNECT_ATTEMPTS, gameId);
            NeoForge.EVENT_BUS.post(new NpcConnectionEvent(
                gameId, NpcConnectionEvent.Status.RECONNECT_FAILED,
                "Exceeded maximum reconnection attempts"
            ));
            return false;
        }

        // Exponential backoff with jitter
        long delay = Math.min(
            INITIAL_RECONNECT_DELAY_MS * (1L << (attempts - 1)),
            MAX_RECONNECT_DELAY_MS
        );
        delay += (long) (Math.random() * delay * 0.1);

        LOGGER.info("Attempting reconnect {} of {} for game {} in {}ms",
            attempts, MAX_RECONNECT_ATTEMPTS, gameId, delay);

        NeoForge.EVENT_BUS.post(new NpcConnectionEvent(
            gameId, NpcConnectionEvent.Status.RECONNECTING,
            "Reconnecting (attempt " + attempts + ")"
        ));

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        return true;
    }
}
