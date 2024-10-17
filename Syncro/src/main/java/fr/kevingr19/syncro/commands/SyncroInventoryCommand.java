package fr.kevingr19.syncro.commands;

import fr.kevingr19.syncro.Syncro;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SyncroInventoryCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player player) Syncro.instance().teamInventoryCommand(player);
        else commandSender.sendMessage("§c> Cette commande est réservée aux joueurs.");
        return true;
    }
}
