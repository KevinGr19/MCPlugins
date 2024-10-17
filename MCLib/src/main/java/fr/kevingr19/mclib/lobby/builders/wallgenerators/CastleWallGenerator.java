package fr.kevingr19.mclib.lobby.builders.wallgenerators;

import org.bukkit.Material;

import java.util.Random;

public class CastleWallGenerator implements WallGenerator{

    private final Random _rd = new Random();

    @Override
    public Material getBlock(int x, int y, int z, int radius, int height) {
        if(y < height - 2){
            final double rd = _rd.nextDouble();
            if(rd < .1) return Material.CRACKED_STONE_BRICKS;
            if(rd < .25) return Material.MOSSY_STONE_BRICKS;
            return Material.STONE_BRICKS;
        }
        else if(y >= height - 1)
            return _rd.nextDouble() < .2 ? Material.MOSSY_COBBLESTONE_WALL : Material.COBBLESTONE_WALL;
        else
            return Material.SMOOTH_STONE;
    }
}
