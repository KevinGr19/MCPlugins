package fr.kevingr19.mclib.lobby;

import org.bukkit.Location;

public interface ILobbyBuilder {

    void build(Location spawn);
    void destroy(Location spawn);

}
