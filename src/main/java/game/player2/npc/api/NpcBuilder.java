package game.player2.npc.api;

import game.player2.npc.client.Player2HttpClient;
import game.player2.npc.dto.Function;
import game.player2.npc.dto.SpawnNpcRequest;
import game.player2.npc.internal.NpcRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Builder pattern for creating NPCs with fluent API.
 * <p>
 * Use this to configure and spawn NPCs with custom personalities,
 * system prompts, and functions.
 * </p>
 *
 * <pre>{@code
 * Player2NpcLib.builder("shopkeeper")
 *     .name("Merchant Bob")
 *     .description("A friendly medieval shopkeeper who sells potions")
 *     .systemPrompt("You are a cheerful shopkeeper in a medieval fantasy world...")
 *     .voiceId("male_friendly")
 *     .withFunction(NpcFunction.minecraftCommand())
 *     .spawn("my_game_id")
 *     .thenAccept(npc -> {
 *         // NPC spawned successfully
 *         npc.chat("Steve", "Hello!");
 *     });
 * }</pre>
 */
public class NpcBuilder {
    private final String shortName;
    private final Player2HttpClient client;

    private String name;
    private String characterDescription = "";
    private String systemPrompt = "";
    private String voiceId = "";
    private final List<NpcFunction> functions = new ArrayList<>();
    private Boolean keepGameState = null;

    public NpcBuilder(String shortName, Player2HttpClient client) {
        this.shortName = Objects.requireNonNull(shortName, "shortName cannot be null");
        this.client = Objects.requireNonNull(client, "client cannot be null");
    }

    /**
     * Sets the display name of the NPC.
     * This is shown when the NPC speaks in chat.
     */
    public NpcBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the character description for the NPC.
     * This helps define the NPC's personality, background, and behavior.
     */
    public NpcBuilder description(String characterDescription) {
        this.characterDescription = characterDescription;
        return this;
    }

    /**
     * Sets the system prompt that guides the NPC's behavior.
     * This is the main instruction set for how the NPC should respond.
     */
    public NpcBuilder systemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }

    /**
     * Sets the voice ID for text-to-speech.
     * Voice IDs can be obtained from the Player2 App.
     */
    public NpcBuilder voiceId(String voiceId) {
        this.voiceId = voiceId;
        return this;
    }

    /**
     * Adds a function/command that the NPC can invoke.
     *
     * @param function The function to add
     */
    public NpcBuilder withFunction(NpcFunction function) {
        this.functions.add(function);
        return this;
    }

    /**
     * Adds the standard Minecraft command function.
     * This allows the NPC to execute any Minecraft command.
     */
    public NpcBuilder withMinecraftCommands() {
        return withFunction(NpcFunction.minecraftCommand());
    }

    /**
     * Adds multiple functions that the NPC can invoke.
     */
    public NpcBuilder withFunctions(List<NpcFunction> functions) {
        this.functions.addAll(functions);
        return this;
    }

    /**
     * If true, keeps existing game state when spawning.
     * If false, starts fresh without previous conversation history.
     */
    public NpcBuilder keepGameState(boolean keep) {
        this.keepGameState = keep;
        return this;
    }

    /**
     * Spawns the NPC asynchronously.
     * <p>
     * This will also start the SSE stream for the game if not already active.
     * </p>
     *
     * @param gameId The game session ID
     * @return CompletableFuture containing the NpcHandle for the spawned NPC
     */
    public CompletableFuture<NpcHandle> spawn(String gameId) {
        Objects.requireNonNull(gameId, "gameId cannot be null");

        // Use shortName as name if not specified
        String displayName = this.name != null ? this.name : this.shortName;

        List<Function> commandDtos = functions.isEmpty() ? null :
            functions.stream().map(NpcFunction::toDto).collect(Collectors.toList());

        SpawnNpcRequest request = new SpawnNpcRequest(
            shortName,
            displayName,
            characterDescription,
            systemPrompt,
            voiceId,
            commandDtos,
            keepGameState
        );

        return client.spawnNpc(gameId, request)
            .thenApply(npcId -> {
                NpcHandle handle = new NpcHandle(npcId, gameId, shortName, displayName, client);
                NpcRegistry.getInstance().register(handle);

                // Ensure SSE stream is started for this game
                client.startSseStream(gameId);

                return handle;
            });
    }
}
