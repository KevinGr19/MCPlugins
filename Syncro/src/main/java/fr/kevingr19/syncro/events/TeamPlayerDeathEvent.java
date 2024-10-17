package fr.kevingr19.syncro.events;

import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

public class TeamPlayerDeathEvent extends TeamPlayerEvent{

    public final PlayerDeathEvent sourceEvent;

    public TeamPlayerDeathEvent(Player player, PlayerData playerData, TeamData teamData, PlayerDeathEvent e) {
        super(player, playerData, teamData);
        sourceEvent = e;
    }
}
