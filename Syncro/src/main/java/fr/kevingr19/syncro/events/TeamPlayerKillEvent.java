package fr.kevingr19.syncro.events;

import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public class TeamPlayerKillEvent extends TeamPlayerDeathEvent{

    public final Player killer;
    public final PlayerData killerPlayerData;
    public final TeamData killerTeamData;

    public TeamPlayerKillEvent(Player player, PlayerData playerData, TeamData teamData, PlayerDeathEvent e, Player killer, PlayerData killerPlayerData, TeamData killerTeamData) {
        super(player, playerData, teamData, e);
        this.killer = killer;
        this.killerPlayerData = killerPlayerData;
        this.killerTeamData = killerTeamData;
    }

    public boolean isSelfHit(){
        return killerTeamData.team.equals(teamData.team);
    }

    public boolean isSuicide(){
        return killer.equals(player);
    }
}
