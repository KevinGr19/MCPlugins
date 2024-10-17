package fr.kevingr19.mclib.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class LPlayer {

    private final Lobby _lobby;
    private final UUID _uuid;
    private LobbyHotbar _hotbar = null;
    public Location spawnPoint = null;

    public LPlayer(Lobby lobby, UUID uuid){
        _lobby = lobby;
        _uuid = uuid;
    }

    public Lobby getLobby() {
        return _lobby;
    }

    public UUID getUuid(){
        return _uuid;
    }

    public LobbyHotbar getHotbar(){
        return _hotbar;
    }

    public void setHotbar(LobbyHotbar hotbar){
        _hotbar = hotbar;
        if(_lobby.isRunning() && _lobby.hasPlayer(_uuid)){
            final Player player = Bukkit.getPlayer(_uuid);
            if(player != null) hotbar.setToPlayer(player);
        }
    }

}
