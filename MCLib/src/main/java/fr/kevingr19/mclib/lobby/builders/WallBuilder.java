package fr.kevingr19.mclib.lobby.builders;

import fr.kevingr19.mclib.lobby.ILobbyBuilder;
import fr.kevingr19.mclib.lobby.builders.wallgenerators.*;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Random;

public class WallBuilder implements ILobbyBuilder {

    private static final WallGenerator[] GENERATORS = {
            new CastleWallGenerator(),
            new CaveWallGenerator(),
            new CheckerboardWallGenerator(),
            new NetherWallGenerator()
    };

    private final WallGenerator _generator;
    private final int _radius, _height;

    public static WallBuilder random(int radius, int wallHeight){
        return new WallBuilder(radius, wallHeight, GENERATORS[new Random().nextInt(GENERATORS.length)]);
    }

    public WallBuilder(int radius, int wallHeight, WallGenerator generator){
        if(radius <= 0 || wallHeight <= 0) throw new IllegalArgumentException("Radius and wall height need to be positive");
        _generator = generator;
        _radius = radius;
        _height = wallHeight;
    }

    private void setBlock(Location center, int x, int y, int z){
        setBlock(center, x, y, z, _generator.getBlock(x, y, z, _radius, _height));
    }

    private void setBlock(Location center, int x, int y, int z, Material material){
        center.getWorld().getBlockAt(
                center.getBlockX()+x,
                center.getBlockY()+y,
                center.getBlockZ()+z).setType(material);
    }

    @Override
    public void build(Location spawn) {
        for(int x = -_radius; x <= _radius; x++)
            for(int z = -_radius; z <= _radius; z++)
                setBlock(spawn, x, -1, z, Material.BARRIER);

        for(int y = 0; y < _height; y++){
            for(int x = -_radius; x <= _radius; x++){
                setBlock(spawn, x, y, -_radius);
                setBlock(spawn, x, y, _radius);
            }

            for(int z = -_radius+1; z < _radius; z++){
                setBlock(spawn, -_radius, y, z);
                setBlock(spawn, _radius, y, z);
            }
        }
    }

    @Override
    public void destroy(Location spawn) {
        for(int x = -_radius; x <= _radius; x++)
            for(int z = -_radius; z <= _radius; z++)
                setBlock(spawn, x, -1, z, Material.AIR);

        for(int y = 0; y < _height; y++){
            for(int x = -_radius; x <= _radius; x++){
                setBlock(spawn, x, y, -_radius, Material.AIR);
                setBlock(spawn, x, y, _radius, Material.AIR);
            }

            for(int z = -_radius+1; z < _radius; z++){
                setBlock(spawn, -_radius, y, z, Material.AIR);
                setBlock(spawn, _radius, y, z, Material.AIR);
            }
        }
    }
}
