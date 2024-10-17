package fr.kevingr19.mclib.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

abstract public class SimplePageGUI extends PageGUI {

    protected List<ItemStack> items = new ArrayList<>();

    public SimplePageGUI(Plugin plugin, String title, int prevPageSlot, int nextPageSlot, int startSlot, int endSlot) {
        super(plugin, title, prevPageSlot, nextPageSlot, startSlot, endSlot);
        updateNavButtons();
    }

    @Override
    public void loadPage(int page){
        int i = page*itemSlots.size();
        for(final int slot : itemSlots){
            inventory.setItem(slot, i < items.size() ? items.get(i) : null);
            i++;
        }
    }

    @Override
    public boolean canGoPrevious(){
        return page-1 >= 0;
    }

    @Override
    public boolean canGoNext(){
        return (page+1)*itemSlots.size() < items.size();
    }

    @Override
    protected void clickOnSlot(int slot, Player player, InventoryClickEvent e){
        itemClicked(slotToItem(slot) + page*itemSlots.size(), player, e);
    }

    abstract protected void itemClicked(int index, Player player, InventoryClickEvent e);
}
