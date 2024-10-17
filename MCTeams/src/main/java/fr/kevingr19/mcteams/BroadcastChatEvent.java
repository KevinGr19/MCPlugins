package fr.kevingr19.mcteams;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class BroadcastChatEvent extends AsyncPlayerChatEvent {
    public BroadcastChatEvent(boolean async, Player who, String message, Set<Player> players) {
        super(async, who, message, players);
    }
}
