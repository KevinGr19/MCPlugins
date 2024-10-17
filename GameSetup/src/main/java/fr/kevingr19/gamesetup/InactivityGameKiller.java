package fr.kevingr19.gamesetup;

import fr.kevingr19.mclib.scheduler.RunnableUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class InactivityGameKiller implements Listener {

    private final int _timeoutSeconds;
    private BukkitRunnable _killer;

    public InactivityGameKiller(int timeoutSeconds){
        if(timeoutSeconds < 0) throw new IllegalArgumentException("timeoutSeconds needs to be positive");
        _timeoutSeconds = timeoutSeconds;
    }

    public void start(){
        Bukkit.getPluginManager().registerEvents(this, GameSetup.instance());
        check();
    }

    public void stop(){
        HandlerList.unregisterAll(this);
        cancel();
    }

    private void check(){
        if(Bukkit.getOnlinePlayers().size() == 0 && !isRunning()){
            _killer = new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.spigot().restart();
                }
            };

            _killer.runTaskLater(GameSetup.instance(), _timeoutSeconds*20L);
            GameSetup.instance().getLogger().warning("No player online. Server will restart in %d seconds.".formatted(_timeoutSeconds));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        cancel();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        RunnableUtils.Delay(this::check, 0, GameSetup.instance());
    }

    private void cancel(){
        if(isRunning()){
            _killer.cancel();
            GameSetup.instance().getLogger().warning("Server restart cancelled.".formatted(_timeoutSeconds));
        }
    }
    private boolean isRunning(){
        return _killer != null && !_killer.isCancelled();
    }

}
