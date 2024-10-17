package fr.kevingr19.mcteams.commands;

import fr.kevingr19.mclib.commands.MultiCommand;
import fr.kevingr19.mcteams.MCTeams;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TeamCommand extends MultiCommand {

    public TeamCommand(){
        addSubCommand("select", new TeamSelectCommand(), cs -> !MCTeams.instance().isLocked);
        addSubCommand("settings", new TeamSettingsCommand(), cs -> !MCTeams.instance().isLocked && cs.hasPermission("mcteams.admin"));
        addSubCommand("setplayer", new TeamSetPlayerCommand(), cs -> !MCTeams.instance().isLocked && cs.hasPermission("mcteams.manager"));

        addSubCommand("help", this::onHelp);
    }

    @Override
    public boolean onHelp(CommandSender commandSender, Command command, String s, String[] strings) {
        return true;
    }

    @Override
    public boolean onUnknownCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        commandSender.sendMessage("§c> Sous-commande inconnue.");
        return false;
    }
}
