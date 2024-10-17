package fr.kevingr19.syncro.commands;

import fr.kevingr19.mclib.commands.MultiCommand;
import fr.kevingr19.mclib.commands.tab.ListTabNode;
import fr.kevingr19.syncro.Syncro;
import fr.kevingr19.syncro.game.GameState;
import fr.kevingr19.syncro.game.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;

import java.util.function.Predicate;

public class SyncroCommand extends MultiCommand {

    public SyncroCommand(){
        final Predicate<CommandSender> isOwner = cs -> cs.hasPermission("syncro.owner") && Syncro.instance().state() == GameState.LOBBY;

        addSubCommand("rules", new SyncroRulesCommand());
        if(Syncro.instance().isDebug()){
            addSubCommand("endgame", (commandSender, command, s, strings) -> {
                if(commandSender instanceof Player player) Syncro.instance().endGame(player);
                return true;
            }, ServerOperator::isOp);
        }

        addSubCommand("start", new SyncroStartCommand(), isOwner);
        addSubCommand("settings", new SyncroSettingsCommand(), isOwner);

        addSubCommand("inventory", new SyncroInventoryCommand(), cs -> Syncro.instance().hasGameStarted());
        addSubCommand(new ListTabNode("stats", null, "players", "teams", "team"), new SyncroStatsCommand(), cs -> Syncro.instance().hasGameStarted());

        addSubCommand("help", this::onHelp);
    }

    @Override
    public boolean onHelp(CommandSender commandSender, Command command, String s, String[] strings) {
        commandSender.sendMessage(
                """
                §bAide pour la commande /syncro §7(Alias : /s)
                §3---------------------------------
                §7- §b/syncro rules §r: Ouvre le livre des règles du jeu.
                §7- §b/syncro inventory §r: Ouvre l'inventaire d'équipe.
                §r
                §7- §b/syncro stats team§r: Statistiques de l'équipe.
                §7- §b/syncro stats players §r: Statistiques de tous les joueurs.
                §7- §b/syncro stats teams §r: Statistiques de toutes les équipes.
                §3---------------------------------
                """
        );
        return true;
    }

    @Override
    public boolean onUnknownCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        commandSender.sendMessage("§c> Sous-commande inconnue.");
        return true;
    }
}
