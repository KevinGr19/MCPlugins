package fr.kevingr19.mclib.lobby.builders.wallgenerators;

import org.bukkit.Material;

public class HouseWallGenerator implements WallGenerator{
    @Override
    public Material getBlock(int x, int y, int z, int radius, int height) {
        if(y == height - 1) return Material.COBBLESTONE;
        if(Math.abs(x) >= radius-1 && Math.abs(z) >= radius-1)
            return y == 0 ? Material.CRAFTING_TABLE : Material.DARK_OAK_LOG;
        if(y >= 1 && y <= 2 && (Math.abs(x) <= 3 || Math.abs(z) <= 3)) return Material.GLASS;
        return Material.DARK_OAK_PLANKS;
    }
}
