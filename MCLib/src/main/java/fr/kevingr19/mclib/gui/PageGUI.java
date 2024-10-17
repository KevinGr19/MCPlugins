package fr.kevingr19.mclib.gui;

import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

abstract public class PageGUI extends ClickGUI{

    protected final int prevPageSlot;
    protected final int nextPageSlot;
    protected ItemHandle prevPageItem;
    protected ItemHandle nextPageItem;

    protected final int xmin, xmax, ymin, ymax;
    protected final List<Integer> itemSlots;

    protected int page = 0;

    public PageGUI(Plugin plugin, String title, int prevPageSlot, int nextPageSlot, int startSlot, int endSlot) {
        super(plugin, title, false);

        this.prevPageSlot = prevPageSlot;
        this.nextPageSlot = nextPageSlot;

        int x1 = startSlot%9, x2 = endSlot%9;
        int y1 = startSlot/9, y2 = endSlot/9;

        xmin = Math.min(x1, x2);
        xmax = Math.max(x1, x2);
        ymin = Math.min(y1, y2);
        ymax = Math.max(y1, y2);

        itemSlots = new ArrayList<>();
        for(int y = ymin; y <= ymax; y++)
            for(int x = xmin; x <= xmax; x++)
                itemSlots.add(y*9 + x);

        prevPageItem = new ItemHandle(Material.RED_WOOL).addHideFlags();
        nextPageItem = new ItemHandle(Material.GREEN_WOOL).addHideFlags();

        addButton(prevPageSlot, null, (p, e) -> {
            if(canGoPrevious()){
                loadPage(--page);
                updateNavButtons();
            }
        });

        addButton(nextPageSlot, null, (p, e) -> {
            if(canGoNext()){
                loadPage(++page);
                updateNavButtons();
            }
        });
    }

    protected void updateNavButtons(){
        inventory.setItem(prevPageSlot, prevPageItem
                .setName(!canGoPrevious() ? "§7Page minimum" : "§cPage précédente")
                .build());

        inventory.setItem(nextPageSlot, nextPageItem
                .setName(!canGoNext() ? "§7Page maximum" : "§2Page suivante")
                .build());
    }

    protected int slotToItem(int slot){
        int x = slot%9, y = slot/9;
        return (y - ymin) * (xmax - xmin + 1) + x - xmin;
    }

    abstract protected boolean canGoPrevious();
    abstract protected boolean canGoNext();

    abstract public void loadPage(int page);
    abstract protected void clickOnSlot(int slot, Player player, InventoryClickEvent e);

    @Override
    protected boolean handleClick(int slot, Player player, InventoryClickEvent e) {
        if(super.handleClick(slot, player, e)) return true;
        if(itemSlots.contains(slot) && !ItemUtils.isNull(e.getCurrentItem())){
            clickOnSlot(slot, player, e);
            return true;
        }
        return false;
    }
}
