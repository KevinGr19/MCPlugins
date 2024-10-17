package fr.kevingr19.mclib.lobby;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class LobbyHotbar {

    private final ItemStack[] _items = new ItemStack[9];
    private final Consumer<Player>[] _actions = new Consumer[9];

    public LobbyHotbar setButton(int slot, ItemStack item, Consumer<Player> action){
        _items[slot] = item;
        _actions[slot] = action;
        return this;
    }

    public void setToPlayer(Player player){
        for(int i = 0; i < 9; i++) player.getInventory().setItem(i, _items[i]);
    }

    public boolean hasAction(int slot){
        return slot >= 0 && slot < 9 && _actions[slot] != null;
    }

    public boolean execute(int slot, Player player){
        if(hasAction(slot)){
            _actions[slot].accept(player);
            return true;
        }
        return false;
    }

}
