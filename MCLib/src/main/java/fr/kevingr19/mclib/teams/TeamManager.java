package fr.kevingr19.mclib.teams;

import fr.kevingr19.mclib.MCLib;
import fr.kevingr19.mclib.scoreboard.ScoreboardPool;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.management.openmbean.KeyAlreadyExistsException;
import java.nio.Buffer;
import java.security.KeyException;
import java.util.*;

public class TeamManager implements Listener {

    private static final String SCOREBOARD_TEAMNAME_HEADER = "TEAMMANAGER_";

    private final Plugin _plugin;

    private final ScoreboardPool _scoreboards;
    private final Map<String, MCTeam> _teams = new HashMap<>();
    private final Map<UUID, MCTeam> _players = new HashMap<>();

    public TeamManager(Plugin plugin, ScoreboardPool pool){
        _plugin = plugin;
        _scoreboards = pool;
        startListener();
        registerCreate();
    }

    public boolean hasPlayer(UUID uuid){
        return _players.containsKey(uuid);
    }
    @Nullable public MCTeam getPlayerTeam(UUID uuid){
        return _players.getOrDefault(uuid, null);
    }

    @Nullable public MCTeam setPlayerTeam(UUID uuid, @Nullable String teamId){
        final MCTeam currTeam = getPlayerTeam(uuid);
        final MCTeam newTeam = getTeam(teamId);
        if(currTeam == newTeam) return newTeam;

        if(newTeam != null){
            if(!newTeam.isEnabled()) throw new IllegalArgumentException("New team is disabled");
            if(newTeam.isFull()) throw new IllegalArgumentException("New team is full");
            newTeam.addPlayer(uuid);
        }

        if(currTeam != null){
            currTeam.removePlayer(uuid);
        }

        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if(newTeam == null){
            _players.remove(uuid);
            if(player.getName() != null){
                _scoreboards.execute((k, s) -> {
                    final Team team = s.getTeam(getScoreboardName(currTeam));
                    if(team != null) team.removeEntry(player.getName());
                });
            }
        }
        else{
            _players.put(uuid, newTeam);
            if(player.getName() != null){
                _scoreboards.execute((k, s) -> {
                    final Team team = s.getTeam(getScoreboardName(newTeam));
                    if(team != null) team.addEntry(player.getName());
                });
            }
        }

        PlayerChangeTeamEvent event = new PlayerChangeTeamEvent(this, uuid, currTeam, newTeam);
        Bukkit.getPluginManager().callEvent(event);

        return newTeam;
    }

    public boolean hasTeam(String teamId){
        return _teams.containsKey(teamId);
    }
    public MCTeam getTeam(String teamId){
        return _teams.get(teamId);
    }
    public Map<String, MCTeam> getTeams(){
        return Collections.unmodifiableMap(_teams);
    }

    public MCTeam addTeam(String teamId){
        if(_teams.containsKey(teamId)) throw new IllegalArgumentException("Team ID already registered");
        final MCTeam newTeam = new MCTeam(this, teamId);
        _teams.put(teamId, newTeam);

        _scoreboards.execute((k, s) -> addScoreboardTeam(s, newTeam));
        return newTeam;
    }

    public void removeTeam(String teamId){
        final MCTeam mcTeam = _teams.remove(teamId);
        if(mcTeam != null){
            mcTeam.disable();
            _scoreboards.execute((k, s) -> removeScoreboardTeam(s, mcTeam));
        }
    }

    public void lockTeam(String teamId){
        getTeam(teamId).setLocked(true);
    }

    public void unlockTeam(String teamId){
        getTeam(teamId).setLocked(false);
    }

    void updateTeamData(MCTeam mcTeam, boolean prefix, boolean suffix, boolean color){
        if(!(prefix || suffix || color)) return;
        _scoreboards.execute((k, s) -> {
            final Team team = s.getTeam(SCOREBOARD_TEAMNAME_HEADER + mcTeam.id);
            if(team != null){
                if(prefix) team.setPrefix(mcTeam.getPrefix());
                if(suffix) team.setSuffix(mcTeam.getSuffix());
                if(color) team.setColor(mcTeam.getColor());
            }
        });
    }

    private void addScoreboardTeam(Scoreboard s, MCTeam mcTeam){
        final Team team = s.registerNewTeam(getScoreboardName(mcTeam));
        team.setPrefix(mcTeam.getPrefix());
        team.setSuffix(mcTeam.getSuffix());
        team.setColor(mcTeam.getColor());

        for(UUID uuid : mcTeam.getPlayers()){
            final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if(player.getName() != null) team.addEntry(player.getName());
        }
    }

    private void removeScoreboardTeam(Scoreboard s, MCTeam mcTeam){
        final Team team = s.getTeam(getScoreboardName(mcTeam));
        if(team != null) team.unregister();
    }

    public void registerCreate(){
        _scoreboards.registerCreate(this::initNewScoreboard);
    }
    public void unregisterCreate(){
        _scoreboards.unregisterCreate(this::initNewScoreboard);
    }
    private void initNewScoreboard(String key, Scoreboard s){
        for(MCTeam mcTeam : _teams.values()) addScoreboardTeam(s, mcTeam);
    }

    private String getScoreboardName(MCTeam mcTeam){
        return SCOREBOARD_TEAMNAME_HEADER + mcTeam.id;
    }

    public void startListener(){
        MCLib.registerListener(this, _plugin);
    }
    public void stopListener(){
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e){
        final MCTeam mcTeam = getPlayerTeam(e.getPlayer().getUniqueId());

        if(mcTeam != null){
            _scoreboards.execute((k, s) -> {
                final Team team = s.getTeam(getScoreboardName(mcTeam));
                if(team != null) team.addEntry(e.getPlayer().getName());
            });
        }
    }

}