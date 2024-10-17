package fr.kevingr19.syncro.game;

import fr.kevingr19.mclib.display.BroadcastUtils;
import fr.kevingr19.mclib.events.PlayerDamagePlayerEvent;
import fr.kevingr19.mcteams.BroadcastChatEvent;
import fr.kevingr19.mcteams.MCTeams;
import fr.kevingr19.syncro.Syncro;
import fr.kevingr19.syncro.events.*;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class GameListener extends BukkitRunnable implements Listener {

    private int _gameTimer = -1;
    private int _pvpTimer;
    private String _textTime, _pvpText;

    public String getTextTime() {
        return _textTime;
    }
    public String getPvPText() {
        return _pvpText;
    }

    public void start(){
        if(_gameTimer > 0) return;

        _gameTimer = 1 + Syncro.instance().settings().getMaxTimeMinutes()*60;

        if(!Syncro.instance().settings().isPvpEnabled()){
            _pvpTimer = -1;
            _pvpText = "§4Off";
        }
        else _pvpTimer = 1 + Syncro.instance().settings().getNoPvPMinutes()*60;

        runTaskTimer(Syncro.instance(), 0, 20);
        Bukkit.getServer().getPluginManager().registerEvents(this, Syncro.instance());
    }
    public void stop(){
        if(_gameTimer < 0) return;
        _gameTimer = -1;

        cancel();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent e){
        e.setCancelled(!(e instanceof BroadcastChatEvent) && MCTeams.instance().sendTeamMessage(e.getPlayer(), e.getMessage()));
    }

    @EventHandler
    public void onDamage(TeamPlayerDamageEvent e){
        if(e instanceof TeamPlayerHitEvent event && event.isSelfHit() && !event.isSamePlayer()){
            event.damagerPlayerData.heals -= e.damage;
            event.damagerTeamData.heals -= e.damage;
        }
        else{
            e.teamData.damages += e.damage;
            e.playerData.damages += e.damage;
        }
    }

    @EventHandler
    public void onHeal(TeamPlayerHealEvent e){
        e.teamData.heals += e.heal;
        e.playerData.heals += e.heal;
    }

    @EventHandler
    public void onDeath(TeamPlayerDeathEvent e){
        e.teamData.deaths++;
        e.playerData.deaths++;
    }

    @EventHandler
    public void onHit(TeamPlayerHitEvent e){
        if(e.isSelfHit()) return;
        e.damagerPlayerData.inflictedDamages += e.damage;
        e.damagerTeamData.inflictedDamages += e.damage;
    }

    @EventHandler
    public void onKill(TeamPlayerKillEvent e){
        if(e.isSuicide()){
            e.killerPlayerData.suicides++;
            e.killerTeamData.suicides++;
        }
        else if(e.isSelfHit()){
            e.killerPlayerData.selfKills++;
            e.killerTeamData.suicides++;
        }
        else{
            e.killerPlayerData.kills++;
            e.killerTeamData.kills++;
        }
    }

    @Override
    public void run() {
        final Duration gameDuration = Duration.ofSeconds(--_gameTimer);
        if(_gameTimer >= 3600) _textTime = "%dh%02dm".formatted(gameDuration.toHours(), gameDuration.toMinutesPart());
        else _textTime = "%dm%02ds".formatted(gameDuration.toMinutes(), gameDuration.toSecondsPart());

        if(_gameTimer <= 0){
            Syncro.instance().endGame(null);
            return;
        }

        if(_pvpTimer > 0){
            if(--_pvpTimer == 0){
                _pvpText = "§2On";

                Syncro.broadcast("Le PvP est maintenant §2activé§r.");
                BroadcastUtils.sound(Sound.ENTITY_BLAZE_SHOOT, 1, 0.8f);
            }
            else{
                final Duration pvpDuration = Duration.ofSeconds(_pvpTimer);
                _pvpText = "§c%dm%02ds".formatted(pvpDuration.toMinutes(), pvpDuration.toSecondsPart());
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHit(PlayerDamagePlayerEvent e){
        e.setCancelled(_pvpTimer != 0);
    }
}
