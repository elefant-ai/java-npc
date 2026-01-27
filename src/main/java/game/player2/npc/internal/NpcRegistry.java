package game.player2.npc.internal;

import game.player2.npc.api.NpcHandle;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Thread-safe registry for tracking active NPCs.
 */
public class NpcRegistry {
    private static final NpcRegistry INSTANCE = new NpcRegistry();

    private final Map<UUID, NpcHandle> npcs = new ConcurrentHashMap<>();

    private NpcRegistry() {}

    public static NpcRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registers an NPC handle.
     */
    public void register(NpcHandle handle) {
        npcs.put(handle.getId(), handle);
    }

    /**
     * Gets an NPC handle by ID.
     */
    public Optional<NpcHandle> get(UUID npcId) {
        return Optional.ofNullable(npcs.get(npcId));
    }

    /**
     * Removes an NPC from the registry.
     */
    public void remove(UUID npcId) {
        NpcHandle handle = npcs.remove(npcId);
        if (handle != null) {
            handle.markDead();
        }
    }

    /**
     * Gets all NPCs for a specific game.
     */
    public Stream<NpcHandle> getByGameId(String gameId) {
        return npcs.values().stream()
            .filter(h -> h.getGameId().equals(gameId));
    }

    /**
     * Clears all NPCs from the registry.
     */
    public void clear() {
        npcs.values().forEach(NpcHandle::markDead);
        npcs.clear();
    }

    /**
     * Returns the number of registered NPCs.
     */
    public int count() {
        return npcs.size();
    }
}
