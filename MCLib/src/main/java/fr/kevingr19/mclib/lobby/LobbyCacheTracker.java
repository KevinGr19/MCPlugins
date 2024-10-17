package fr.kevingr19.mclib.lobby;

import fr.kevingr19.mclib.MCLib;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class LobbyCacheTracker implements Listener {

    private final Set<UUID> _cachedPlayers;
    private final ILobbyPlayerHandler _playerHandler;
    private boolean _running = false;

    LobbyCacheTracker(Collection<UUID> cachedPlayers, ILobbyPlayerHandler playerHandler){
        _cachedPlayers = new HashSet<>(cachedPlayers);
        _playerHandler = playerHandler;
    }

    public void start(Plugin plugin){
        if(_cachedPlayers.size() == 0) return;
        MCLib.registerListener(this, plugin);
        _running = true;
    }

    public void stop(){
        HandlerList.unregisterAll(this);
        _running = false;
    }

    public boolean isRunning(){
        return _running;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onJoin(final PlayerJoinEvent e){
        removePlayer(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onRespawn(final PlayerRespawnEvent e){
        removePlayer(e.getPlayer());
    }

    private void removePlayer(Player player){
        if(!_cachedPlayers.remove(player.getUniqueId())) return;
        _playerHandler.playerDespawned(player);

        // Stop when empty
        if(_cachedPlayers.size() == 0) stop();
    }

}
