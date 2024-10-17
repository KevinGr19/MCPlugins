package fr.kevingr19.mclib.commands;

import fr.kevingr19.mclib.commands.tab.PredicateMapTabNode;
import fr.kevingr19.mclib.commands.tab.TabNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

abstract public class TabCompletedCommand implements TabExecutor {

    protected TabNode rootNode;
    private TabNode currentNode;

    public TabCompletedCommand(TabNode rootNode){
        this.rootNode = this.currentNode = rootNode;
    }

    @Override
    final public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player) || args.length < 1) return null; // Error precaution, length should always be of at least 1
        currentNode = rootNode;

        for (int i = 0; i < args.length - 1; i++) {
            String arg = args[i];

            if (currentNode instanceof TabNode.SingleTabEnd)
                currentNode = ((TabNode.SingleTabEnd) currentNode).getNext();

            else if (currentNode instanceof TabNode.MappedTabEnd)
                currentNode = ((TabNode.MappedTabEnd) currentNode).getNext(arg.toLowerCase());

            else return new ArrayList<>();
            if(currentNode == null) return new ArrayList<>();
        }

        if(currentNode instanceof PredicateMapTabNode) return StringUtil.copyPartialMatches(args[args.length-1],
                ((PredicateMapTabNode) currentNode).getPossibleWords(player), new ArrayList<>());

        return StringUtil.copyPartialMatches(args[args.length-1], currentNode.getPossibleWords(), new ArrayList<>());

    }

}
