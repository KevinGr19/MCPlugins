package fr.kevingr19.syncro.events;

import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class TeamPlayerHitEvent extends TeamPlayerDamageEvent{

    public final Player damager;
    public final PlayerData damagerPlayerData;
    public final TeamData damagerTeamData;

    public TeamPlayerHitEvent(Player player, PlayerData playerData, TeamData teamData, double amount, Player damager, PlayerData damagerPlayerData, TeamData damagerTeamData) {
        super(player, playerData, teamData, amount);
        this.damager = damager;
        this.damagerPlayerData = damagerPlayerData;
        this.damagerTeamData = damagerTeamData;
    }

    public boolean isSelfHit(){
        return damagerTeamData.team.equals(teamData.team);
    }

    public boolean isSamePlayer(){
        return damager.equals(player);
    }
}
