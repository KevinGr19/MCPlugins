package fr.kevingr19.gamesetup;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class NoGameOwnerEvent extends Event {

    private final UUID _oldOwner;

    public NoGameOwnerEvent(UUID oldOwner) {
        _oldOwner = oldOwner;
    }

    public UUID getOldOwner() {
        return _oldOwner;
    }

    private static HandlerList HANDLERS = new HandlerList();
    public static HandlerList getHandlerList(){ return HANDLERS; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
}
