package fr.kevingr19.mclib.teams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerChangeTeamEvent extends Event {

    private static HandlerList HANDLER_LIST = new HandlerList();
    public static HandlerList getHandlerList(){
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    private final TeamManager _manager;
    private final UUID _uuid;
    private final Player _player;
    private final MCTeam _oldTeam;
    private final MCTeam _newTeam;

    public PlayerChangeTeamEvent(TeamManager manager, UUID uuid, @Nullable MCTeam oldTeam, @Nullable MCTeam newTeam){
        _manager = manager;
        _uuid = uuid;
        _oldTeam = oldTeam;
        _newTeam = newTeam;

        _player = Bukkit.getPlayer(uuid);
    }

    public TeamManager getManager(){
        return _manager;
    }
    public UUID getUuid() {
        return _uuid;
    }
    @Nullable public Player getPlayer() {
        return _player;
    }
    @Nullable public MCTeam getNewTeam() {
        return _newTeam;
    }
    @Nullable public MCTeam getOldTeam() {
        return _oldTeam;
    }
}