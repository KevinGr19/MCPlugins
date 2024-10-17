package fr.kevingr19.gamesetup;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AutoRestarter extends BukkitRunnable {

    private static final List<Integer> MESSAGES = List.of(new Integer[]{0, 30, 20, 10, 5, 4, 3, 2, 1});
    private final int _initialSeconds;
    private int _remaningSeconds;

    public AutoRestarter(int seconds){
        _initialSeconds = Math.max(seconds, 0);
        _remaningSeconds = _initialSeconds;
    }

    @Override
    public void run() {
        if(_initialSeconds == _remaningSeconds) logRestartMessage();

        if(_remaningSeconds-- <= 0){
            Bukkit.spigot().restart();
            cancel();
            return;
        }

        if(MESSAGES.contains(_remaningSeconds%60 * (_remaningSeconds>60 ? -1 : 1))) logRestartMessage();
    }

    private void logRestartMessage(){
        GameSetup.instance().getLogger().warning("Game ended. Server will restart in %d seconds.".formatted(_remaningSeconds));
        Bukkit.broadcastMessage("§3§l> §3Le serveur va redémarrer dans %d secondes.".formatted(_remaningSeconds));
    }
}
