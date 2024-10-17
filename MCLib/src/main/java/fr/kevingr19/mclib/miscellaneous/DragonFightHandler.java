package fr.kevingr19.mclib.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

public class DragonFightHandler extends BukkitRunnable implements Listener {

    private final Plugin _plugin;
    private final World _world;
    private final Runnable _onDragonKill;
    private final Runnable _onDragonDespawn;

    private DragonBattle _battle;
    private EnderDragon _dragon;
    private BossBar _bossBar;
    private boolean _isDead;

    public DragonFightHandler(Plugin plugin, EnderDragon dragon, Runnable onDragonKill, @Nullable Runnable onDragonDespawn){
        _plugin = plugin;
        _dragon = dragon;
        _battle = _dragon.getDragonBattle();
        _world = _dragon.getWorld();
        _bossBar = Bukkit.createBossBar(_dragon.isCustomNameVisible() ? _dragon.getCustomName() : _dragon.getName(),
                BarColor.PINK, BarStyle.SOLID, BarFlag.DARKEN_SKY);

        _onDragonKill = onDragonKill;
        _onDragonDespawn = onDragonDespawn;
        start();
    }

    public EnderDragon getDragon() {
        return _dragon;
    }
    public World getWorld() {
        return _world;
    }

    private void start(){
        _isDead = _dragon.isDead();
        Bukkit.getPluginManager().registerEvents(this, _plugin);
        runTaskTimer(_plugin, 0, 1);

        updateBar();
        Bukkit.getOnlinePlayers().forEach(this::updatePlayerBar);
    }

    public void stop(){
        cancel();
        HandlerList.unregisterAll(this);
        _bossBar.removeAll();
    }

    private void updateBar(){
        _bossBar.setProgress(_dragon.isDead() ? 0 : _dragon.getHealth() / _dragon.getMaxHealth());
    }

    private void updatePlayerBar(Player player){ updatePlayerBar(player, _world.equals(player.getWorld())); }
    private void updatePlayerBar(Player player, boolean inTheEnd){
        if(inTheEnd) _bossBar.removePlayer(player);
        else _bossBar.addPlayer(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        updatePlayerBar(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        updatePlayerBar(e.getPlayer(), _world.equals(e.getRespawnLocation().getWorld()));
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDimensionChange(PlayerTeleportEvent e){
        updatePlayerBar(e.getPlayer(), _world.equals(e.getTo().getWorld()));
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonKill(EntityDeathEvent e){
        if(!e.getEntity().equals(_dragon)) return;

        _isDead = true;
        if(_onDragonKill != null) _onDragonKill.run();
        updateBar();
    }

    @Override
    public void run() {
        if(!_isDead) updateBar();
        else if(_battle == null || _battle.getEnderDragon() == null){
            if(_onDragonDespawn != null) _onDragonDespawn.run();
            stop();
        }
    }
}
