package fr.kevingr19.mclib.events;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nullable;

public class EventUtils {

    @Nullable
    public static Player getPlayerDamager(EntityDamageEvent event){
        if(!(event instanceof EntityDamageByEntityEvent e)) return null;
        if(e.getDamager() instanceof Player player) return player;
        if(e.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) return shooter;
        return null;
    }

}
