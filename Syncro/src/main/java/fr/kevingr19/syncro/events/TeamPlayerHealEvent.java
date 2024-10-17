package fr.kevingr19.syncro.events;

import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import org.bukkit.entity.Player;

public class TeamPlayerHealEvent extends TeamPlayerEvent{

    public final double heal;

    public TeamPlayerHealEvent(Player player, PlayerData playerData, TeamData teamData, double amount) {
        super(player, playerData, teamData);
        heal = amount;
    }
}
