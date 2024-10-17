package fr.kevingr19.syncro.events;

import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import org.bukkit.entity.Player;

public class TeamPlayerDamageEvent extends TeamPlayerEvent {

    public final double damage;

    public TeamPlayerDamageEvent(Player player, PlayerData playerData, TeamData teamData, double amount) {
        super(player, playerData, teamData);
        damage = amount;
    }
}
