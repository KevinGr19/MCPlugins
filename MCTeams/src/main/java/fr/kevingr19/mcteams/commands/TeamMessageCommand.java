package fr.kevingr19.mcteams.commands;

import fr.kevingr19.mcteams.MCTeams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamMessageCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player player)) return false;
        if(strings.length < 1) return true;

        if(!MCTeams.instance().sendTeamMessage(player, String.join(" ", strings)))
            player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>(0);
    }
}
