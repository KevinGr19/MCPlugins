package fr.kevingr19.mclib.lobby.builders.wallgenerators;

import org.bukkit.Material;

public interface WallGenerator {

    Material getBlock(int x, int y, int z, int radius, int height);

}
