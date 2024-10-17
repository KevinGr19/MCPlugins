package fr.kevingr19.mclib.lobby.builders.wallgenerators;

import org.bukkit.Material;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import java.util.Random;

public class CaveWallGenerator implements WallGenerator{

    private final int _offX = new Random().nextInt(255) - 126;
    private final int _offY = new Random().nextInt(255) - 126;
    private final int _offZ = new Random().nextInt(255) - 126;

    @Override
    public Material getBlock(int x, int y, int z, int radius, int height) {
        final double value = PerlinNoiseGenerator.getNoise(x*.3 + _offX, y*.3 + _offY, z*.3 + _offZ
                , 2, .5, .7);

        if(value < -.9 || (value > -.8 && value < -.75)) return Material.DEEPSLATE_DIAMOND_ORE;
        if(value > -.55 && value < -.45) return Material.DEEPSLATE_GOLD_ORE;
        if(value > -.22 && value < -.15) return Material.DEEPSLATE_IRON_ORE;
        if(value > .45) return Material.TUFF;
        return Material.DEEPSLATE;
    }
}