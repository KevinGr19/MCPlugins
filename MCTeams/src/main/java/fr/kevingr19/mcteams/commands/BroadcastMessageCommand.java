package fr.kevingr19.mcteams.commands;

import fr.kevingr19.mcteams.BroadcastChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BroadcastMessageCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player player)) return false;
        if(strings.length < 1) return true;

        BroadcastChatEvent event = new BroadcastChatEvent(false, player, String.join(" ", strings), new HashSet<>(Bukkit.getOnlinePlayers()));
        Bukkit.getPluginManager().callEvent(event);

        Bukkit.broadcastMessage("<%s§r> %s".formatted(player.getDisplayName(), String.join(" ", strings)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>(0);
    }
}
