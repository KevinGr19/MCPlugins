package fr.kevingr19.syncro.game;

import fr.kevingr19.mclib.teams.MCTeam;
import fr.kevingr19.mcteams.MCTeams;
import fr.kevingr19.syncro.utils.PresortedArray;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

public class StatisticsManager {

    private final Map<String, TeamData> _teamDatas;
    private final Map<UUID, PlayerData> _playerDatas;

    private PresortedArray<TeamData> _sortedTeams;
    private PresortedArray<PlayerData> _sortedPlayers;

    public StatisticsManager(){
        _teamDatas = new HashMap<>();
        _playerDatas = new HashMap<>();
    }

    public void add(Player player) { add(player.getUniqueId()); }
    public void add(UUID uuid){
        final TeamData teamData = getTeam(uuid);
        if(teamData == null) throw new IllegalArgumentException("Player has no team data");
        _playerDatas.put(uuid, new PlayerData(uuid, teamData));
    }

    @Nullable public PlayerData get(@Nullable Player player) { return get(player == null ? null : player.getUniqueId()); }
    @Nullable public PlayerData get(@Nullable UUID uuid){
        return _playerDatas.getOrDefault(uuid, null);
    }

    public boolean has(@Nullable Player player) { return has(player.getUniqueId()); }
    public boolean has(@Nullable UUID uuid){
        return _playerDatas.containsKey(uuid);
    }

    public void addTeam(TeamData teamData){
        _teamDatas.put(teamData.team.id, teamData);
    }
    public TeamData getTeam(String teamId){
        return _teamDatas.get(teamId);
    }
    public boolean hasTeam(String teamId){
        return _teamDatas.containsKey(teamId);
    }

    @Nullable
    public TeamData getTeam(Player player){
        return getTeam(player.getUniqueId());
    }

    @Nullable
    public TeamData getTeam(UUID uuid){
        final MCTeam team = MCTeams.instance().teamManager().getPlayerTeam(uuid);
        return team == null || !hasTeam(team.id) ? null : getTeam(team.id);
    }

    public Map<String, TeamData> getTeams(){
        return Collections.unmodifiableMap(_teamDatas);
    }
    public Map<UUID, PlayerData> getPlayers(){
        return Collections.unmodifiableMap(_playerDatas);
    }

    public PresortedArray<TeamData> getSortedTeams(){
        return _sortedTeams;
    }
    public PresortedArray<PlayerData> getSortedPlayers(){
        return _sortedPlayers;
    }

    public void sortEverything(){
        _sortedTeams = new PresortedArray<>(_teamDatas.values().toArray(TeamData[]::new));
        _sortedTeams.addSort("balance", (t1, t2) -> Double.compare(t2.balance(), t1.balance()));
        _sortedTeams.addSort("heals", (t1, t2) -> Double.compare(t2.heals, t1.heals));
        _sortedTeams.addSort("damages", Comparator.comparingDouble(t -> t.damages));
        _sortedTeams.addSort("deaths", Comparator.comparingInt(t -> t.deaths));
        _sortedTeams.addSort("inflicted_damages", (t1, t2) -> Double.compare(t2.inflictedDamages, t1.inflictedDamages));
        _sortedTeams.addSort("kills", (t1, t2) -> Integer.compare(t2.kills, t1.kills));

        _sortedPlayers = new PresortedArray<>(_playerDatas.values().toArray(PlayerData[]::new));
        _sortedPlayers.addSort("balance", (t1, t2) -> Double.compare(t2.balance(), t1.balance()));
        _sortedPlayers.addSort("heals", (t1, t2) -> Double.compare(t2.heals, t1.heals));
        _sortedPlayers.addSort("damages", Comparator.comparingDouble(t -> t.damages));
        _sortedPlayers.addSort("deaths", Comparator.comparingInt(t -> t.deaths));
        _sortedPlayers.addSort("inflicted_damages", (t1, t2) -> Double.compare(t2.inflictedDamages, t1.inflictedDamages));
        _sortedPlayers.addSort("kills", (t1, t2) -> Integer.compare(t2.kills, t1.kills));
    }

    @Nullable
    public PlayerData getBestPlayer(Comparator<PlayerData> comparator, boolean getFirst){
        int count = 1;
        PlayerData current = null;
        for(PlayerData player : _playerDatas.values()){
            if(current == null){
                current = player;
                continue;
            }

            final int compare = comparator.compare(player, current);
            if(compare > 0) continue;
            if(compare < 0){
                current = player;
                count = 0;
            }
            count++;
        }

        return !getFirst && count > 1 ? null : current;
    }
}
