package fr.kevingr19.mclib.commands;

import fr.kevingr19.mclib.commands.tab.PredicateMapTabNode;
import fr.kevingr19.mclib.commands.tab.TabNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

abstract public class MultiCommand extends TabCompletedCommand{

    private final Map<String, CommandExecutor> commands = new HashMap<>();

    public MultiCommand() {
        super(new PredicateMapTabNode("root"));
    }

    protected PredicateMapTabNode getRoot(){
        return ((PredicateMapTabNode) rootNode);
    }

    protected void addSubCommand(String name, CommandExecutor executor){
        getRoot().addEmptyNext(name);
        commands.put(name, executor);
    }

    protected void addSubCommand(String name, CommandExecutor executor, Predicate<CommandSender> condition){
        addSubCommand(name, executor);
        getRoot().setPredicate(condition, name);
    }

    protected void addSubCommand(TabNode node, CommandExecutor executor){
        getRoot().addNext(node);
        commands.put(node.getName(), executor);
    }

    protected void addSubCommand(TabNode node, CommandExecutor executor, Predicate<CommandSender> condition){
        addSubCommand(node, executor);
        getRoot().setPredicate(condition, node.getName());
    }

    abstract public boolean onHelp(CommandSender commandSender, Command command, String s, String[] strings);
    abstract public boolean onUnknownCommand(CommandSender commandSender, Command command, String s, String[] strings);

    @Override
    final public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length < 1) return onHelp(commandSender, command, s, strings);
        CommandExecutor subCommand = commands.getOrDefault(strings[0], null);
        Predicate<CommandSender> predicate = getRoot().getPredicate(strings[0]);

        if(subCommand == null || (predicate != null && !predicate.test(commandSender))) return onUnknownCommand(commandSender, command, s, strings);
        return subCommand.onCommand(commandSender, command, s, strings);
    }
}
