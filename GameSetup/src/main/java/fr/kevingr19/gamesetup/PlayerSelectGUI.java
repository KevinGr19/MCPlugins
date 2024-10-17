package fr.kevingr19.gamesetup;

import fr.kevingr19.mclib.gui.SimplePageGUI;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PlayerSelectGUI extends SimplePageGUI {

    private static final String PLAYER_TAG = "gamesetup.target";

    public PlayerSelectGUI() {
        super(GameSetup.instance(), "Maître du jeu", 0, 8, 9, 35);

        for(Player player : Bukkit.getOnlinePlayers()) items.add(generateHead(player));

        ItemUtils.outlineInventory(inventory, 1, 1, 0, 0);
        loadPage(page);

        prevPageItem = new ItemHandle(Material.RED_STAINED_GLASS_PANE).addHideFlags();
        nextPageItem = new ItemHandle(Material.GREEN_STAINED_GLASS_PANE).addHideFlags();
        updateNavButtons();
    }

    private static ItemStack generateHead(Player player){
        final ItemHandle handle = new ItemHandle(Material.PLAYER_HEAD)
                .setName("§f" + player.getName())
                .addUUID(PLAYER_TAG, player.getUniqueId())
                .addHideFlags();

        ((SkullMeta) handle.meta()).setOwningPlayer(player);
        return handle.build();
    }

    @Override
    protected void itemClicked(int i, Player player, InventoryClickEvent inventoryClickEvent) {
        if(!player.hasPermission("gamesetup.owner")) {
            Close(player);
            return;
        }

        CompoundTag tag = new ItemHandle(items.get(i)).tag();
        if (tag.hasUUID(PLAYER_TAG)) {
            final Player target = Bukkit.getPlayer(tag.getUUID(PLAYER_TAG));
            if (target == null) player.sendMessage("§c> Ce joueur est hors-ligne");
            else GameSetup.instance().setOwner(target);
        }
        Close(player);
    }

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 45, s);
    }
}
