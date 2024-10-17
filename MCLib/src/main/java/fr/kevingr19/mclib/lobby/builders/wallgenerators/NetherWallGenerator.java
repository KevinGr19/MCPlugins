package fr.kevingr19.mclib.lobby.builders.wallgenerators;

import org.bukkit.Material;
import org.bukkit.util.noise.PerlinNoiseGenerator;

import java.util.Random;

public class NetherWallGenerator implements WallGenerator {

    private final int _offX = new Random().nextInt(255) - 126;
    private final int _offY = new Random().nextInt(255) - 126;
    private final int _offZ = new Random().nextInt(255) - 126;

    @Override
    public Material getBlock(int x, int y, int z, int radius, int height) {
        final double value = PerlinNoiseGenerator.getNoise(x*.3 + _offX, y + _offY, z*.3 + _offZ
                , 2, .5, .7);
        if(value < -0.7) return Material.ANCIENT_DEBRIS;
        if(value > 0.35) return Material.SOUL_SAND;
        if(value > 0 && value < 0.05) return Material.NETHER_GOLD_ORE;
        return Material.NETHERRACK;
    }
}
