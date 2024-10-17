package fr.kevingr19.mclib.gui;

import fr.kevingr19.mclib.MCLib;
import fr.kevingr19.mclib.scheduler.RunnableUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

abstract public class BaseGUI implements Listener {

    private final Plugin _plugin;
    protected final Inventory inventory;
    abstract protected Inventory createInventory(String title);

    public BaseGUI(Plugin plugin, String title){
        _plugin = plugin;
        inventory = createInventory(title);
        if(inventory == null) throw new RuntimeException("createInventory returned null");

        MCLib.registerListener(this, _plugin);
    }

    public void open(final Player player){
        player.openInventory(inventory);
    }

    abstract protected boolean handleClick(int slot, Player player, final InventoryClickEvent e);
    abstract protected boolean handleDrag(Player player, final InventoryDragEvent e);

    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(final InventoryClickEvent e){
        if(e.getClickedInventory() != inventory)
            e.setCancelled(e.getInventory() == inventory && e.getAction() == InventoryAction.COLLECT_TO_CURSOR);

        else if(e.getClickedInventory() == inventory)
            handleClick(e.getRawSlot(), (Player) e.getWhoClicked(), e);
    }

    @EventHandler (ignoreCancelled = true)
    public void onDrag(final InventoryDragEvent e){
        if(e.getInventory() == inventory){
            for(int slot : e.getRawSlots()) if(slot >= 0 && slot < inventory.getSize()){
                handleDrag((Player) e.getWhoClicked(), e);
                return;
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onClose(final InventoryCloseEvent e){
        if(e.getInventory() == inventory) HandlerList.unregisterAll(this);
    }

    protected final void Close(final Player p){
        RunnableUtils.Delay(p::closeInventory, 0, _plugin);
    }

}