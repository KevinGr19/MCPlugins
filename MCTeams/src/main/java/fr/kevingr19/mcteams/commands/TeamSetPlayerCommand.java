package fr.kevingr19.mcteams.commands;

import fr.kevingr19.mcteams.MCTeams;
import fr.kevingr19.mcteams.gui.TeamPlayerSelectGUI;
import fr.kevingr19.mcteams.gui.TeamSelectGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamSetPlayerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player player)){
            commandSender.sendMessage("§c> Cette commande est réservée aux joueurs.");
            return true;
        }

        return MCTeams.instance().teamSetPlayerCommand(player);
    }
}
