package fr.kevingr19.mclib.lobby.builders.wallgenerators;

import org.bukkit.Material;

public class CheckerboardWallGenerator implements WallGenerator{
    @Override
    public Material getBlock(int x, int y, int z, int radius, int height) {
        x += radius;
        z += radius;
        return (x%2 == y%2) == (z%2 == 0) ? Material.WHITE_CONCRETE : Material.BLACK_CONCRETE;
    }
}
