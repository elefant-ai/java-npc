package game.player2.npc.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple thread-safe event bus for dispatching NPC events to registered listeners.
 */
public class Player2EventBus {
    private static final Player2EventBus INSTANCE = new Player2EventBus();

    private final List<Player2EventListener> listeners = new CopyOnWriteArrayList<>();

    public static Player2EventBus getInstance() {
        return INSTANCE;
    }

    public void addListener(Player2EventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(Player2EventListener listener) {
        listeners.remove(listener);
    }

    public void postConnectionEvent(NpcConnectionEvent event) {
        for (Player2EventListener listener : listeners) {
            listener.onConnectionEvent(event);
        }
    }

    /**
     * @return true if any listener consumed the event
     */
    public boolean postMessageEvent(NpcMessageEvent event) {
        for (Player2EventListener listener : listeners) {
            if (listener.onMessageEvent(event)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if any listener consumed the event
     */
    public boolean postCommandEvent(NpcCommandEvent event) {
        for (Player2EventListener listener : listeners) {
            if (listener.onCommandEvent(event)) {
                return true;
            }
        }
        return false;
    }

    public void postErrorEvent(NpcErrorEvent event) {
        for (Player2EventListener listener : listeners) {
            listener.onErrorEvent(event);
        }
    }

    public void clearListeners() {
        listeners.clear();
    }
}
