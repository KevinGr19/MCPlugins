package fr.kevingr19.mclib.commands.tab;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * {@link TabNode} that can give a Set of player usernames based on a {@link Predicate}.
 */

public class PlayerTabNode extends TabNode implements TabNode.SingleTabEnd {

    protected final Predicate<OfflinePlayer> predicate;
    protected TabNode next;

    public PlayerTabNode(String name, TabNode next) {
        super(name);
        this.next = next;
        this.predicate = offlinePlayer -> true;
    }

    public PlayerTabNode(String name, TabNode next, Predicate<OfflinePlayer> predicate) {
        super(name);
        this.next = next;
        this.predicate = predicate;
    }

    @Override
    public List<String> getPossibleWords() {
        return Arrays.stream(Bukkit.getOfflinePlayers()).filter(predicate).map(OfflinePlayer::getName).collect(Collectors.toList());
    }

    @Override
    public TabNode getNext() {
        return next;
    }

    @Override
    public void setNext(TabNode node) {
        this.next = node;
    }
}