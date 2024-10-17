package fr.kevingr19.syncro.commands;

import fr.kevingr19.syncro.Syncro;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SyncroStatsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player player)){
            commandSender.sendMessage("§c> Cette commande est réservée aux joueurs.");
            return true;
        }

        if(strings.length < 2) player.sendMessage("§c> Choississez un mode.");
        else if(strings[1].equalsIgnoreCase("team")) Syncro.instance().openTeamStats(player);
        else if(strings[1].equalsIgnoreCase("teams")) Syncro.instance().openTeamsStats(player);
        else if(strings[1].equalsIgnoreCase("players")) Syncro.instance().openPlayersStats(player);
        return true;
    }
}
