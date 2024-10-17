package fr.kevingr19.mclib.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

abstract public class ClickGUI extends BaseGUI {

    protected Map<Integer, BiConsumer<Player, InventoryClickEvent>> buttons;
    protected boolean closeOnClick;

    public ClickGUI(Plugin plugin, String title, boolean closeOnClick) {
        super(plugin, title);
        buttons = new HashMap<>();
        this.closeOnClick = closeOnClick;
    }

    public void addButton(int slot, ItemStack item, BiConsumer<Player, InventoryClickEvent> action){
        buttons.put(slot, action);
        if(item != null) inventory.setItem(slot, item);
    }

    public void removeButton(int slot){
        buttons.remove(slot);
        inventory.setItem(slot, null);
    }

    @Override
    protected boolean handleClick(int slot, Player player, InventoryClickEvent e) {
        e.setCancelled(true);
        if(buttons.containsKey(slot)){
            buttons.get(slot).accept(player, e);
            if(closeOnClick) Close(player);
            return true;
        }
        return false;
    }

    @Override
    protected boolean handleDrag(Player player, InventoryDragEvent e) {
        e.setCancelled(true);
        return true;
    }
}
