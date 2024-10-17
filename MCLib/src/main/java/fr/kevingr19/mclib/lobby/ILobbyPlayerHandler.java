package fr.kevingr19.mclib.lobby;

import org.bukkit.entity.Player;

public interface ILobbyPlayerHandler {

    void playerAdded(LPlayer lPlayer);
    void playerSpawned(Player player, LPlayer lPlayer);
    void playerDespawned(Player player);

}
