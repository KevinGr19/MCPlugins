package fr.kevingr19.mcteams.commands;

import fr.kevingr19.mcteams.MCTeams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamSendPositionCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player player))
            commandSender.sendMessage("§c> Cette commande est réservée aux joueurs.");

        else{
            String message = "§6§n%.0f %.0f %.0f".formatted(
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ());

            if(strings.length > 0) message += "§r §7§o(%s)".formatted(String.join(" ", strings));
            if(!MCTeams.instance().sendTeamMessage(player, message))
                player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>(0);
    }
}
