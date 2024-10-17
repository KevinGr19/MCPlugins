package fr.kevingr19.syncro.events;

import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

abstract public class TeamPlayerEvent extends Event {

    public final Player player;
    public final PlayerData playerData;
    public final TeamData teamData;

    public TeamPlayerEvent(Player player, PlayerData playerData, TeamData teamData){
        this.player = player;
        this.playerData = playerData;
        this.teamData = teamData;
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
    @Override public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
