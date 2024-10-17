package fr.kevingr19.syncro.game;

import fr.kevingr19.mclib.scoreboard.sidebar.Sidebar;
import fr.kevingr19.syncro.Syncro;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nullable;

public class PlayerSidebar extends BukkitRunnable {

    private final Scoreboard _scoreboard;
    private final Sidebar _sidebar;

    private final Player _player;
    private final PlayerData _playerData;

    public PlayerSidebar(@Nullable Player player, Scoreboard scoreboard){
        _player = player;
        _playerData = player != null ? Syncro.instance().stats().get(player) : null;

        if(_playerData == null && _player != null)
            throw new IllegalArgumentException("The player needs to be registered in the StatisticsManager");

        _scoreboard = scoreboard;
        _sidebar = new Sidebar(_scoreboard, "syncro.sidebar", "§3§lSyncro");

        _sidebar.setLine(8, "§3----------------");

        if(_player != null){
            _sidebar.setLine(5, " ");
            _sidebar.setLine(4, player.getDisplayName());
        }
        _sidebar.setLine(0, "§3----------------");
        update();
    }

    public Scoreboard getScoreboard(){
        return _scoreboard;
    }

    private void update(){
        _sidebar.setLine(7, "§7- Temps : §b%s".formatted(Syncro.instance().gameListener().getTextTime()));
        _sidebar.setLine(6, "§7- PvP : §r%s".formatted(Syncro.instance().gameListener().getPvPText()));

        if(_player != null){
            _sidebar.setLine(3, "§7- Soins : §a%.1f".formatted(_playerData.heals));
            _sidebar.setLine(2, "§7- Dégâts : §c%.1f".formatted(_playerData.damages));
            _sidebar.setLine(1, "§7- Morts : §4%d".formatted(_playerData.deaths));
        }
    }

    @Override
    public void run() {
        update();
    }
}
