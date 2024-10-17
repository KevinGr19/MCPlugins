package fr.kevingr19.gamesetup;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

public interface LobbyCommandExecutor {

    boolean onCommand(Player commandSender, Command command, String s, String[] strings);

}
