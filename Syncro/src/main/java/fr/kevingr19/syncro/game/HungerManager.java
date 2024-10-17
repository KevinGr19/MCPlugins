package fr.kevingr19.syncro.game;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HungerManager {

    public final double foodMultiplier;
    private final Map<UUID, Double> _hungerBars = new HashMap<>();

    public HungerManager(double foodMultiplier){
        this.foodMultiplier = foodMultiplier;
    }

    public boolean has(Player player){
        return _hungerBars.containsKey(player.getUniqueId());
    }

    public double get(Player player){
        return _hungerBars.get(player.getUniqueId());
    }

    public void set(Player player, double foodLevel){
        _hungerBars.put(player.getUniqueId(), foodLevel);
    }
}
