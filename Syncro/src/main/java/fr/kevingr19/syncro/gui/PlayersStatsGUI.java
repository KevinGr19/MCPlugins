package fr.kevingr19.syncro.gui;

import fr.kevingr19.mclib.gui.SimplePageGUI;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import fr.kevingr19.syncro.Syncro;
import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import fr.kevingr19.syncro.utils.PresortedArray;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PlayersStatsGUI extends SimplePageGUI {

    private final ItemStack[] icons;

    public PlayersStatsGUI() {
        super(Syncro.instance(), "Statistiques des joueurs", 9, 17, 18, 53);

        final PresortedArray<PlayerData> sortedPlayers = Syncro.instance().stats().getSortedPlayers();
        icons = new ItemStack[sortedPlayers.length];

        for(int i = 0; i < icons.length; i++){
            final PlayerData data = sortedPlayers.get(i);
            icons[i] = data.getDataIcon().build();
        }

        ItemUtils.outlineInventory(inventory, 2, 1, 0, 0);

        addButton(2, new ItemHandle(Material.GOLDEN_APPLE)
                .setName("§fTrier par §bBalance")
                .addHideFlags().build()
        , (p, e) -> sortPlayers("balance"));

        addButton(3, new ItemHandle(Material.ARROW)
                        .setName("§fTrier par §bDégâts")
                        .addHideFlags().build()
                , (p, e) -> sortPlayers("damages"));

        addButton(4, new ItemHandle(Material.SKELETON_SKULL)
                        .setName("§fTrier par §bMorts")
                        .addHideFlags().build()
                , (p, e) -> sortPlayers("deaths"));

        addButton(5, new ItemHandle(Material.DIAMOND_SWORD)
                        .setName("§fTrier par §bDégâts Infligés")
                        .addHideFlags().build()
                , (p, e) -> sortPlayers("inflicted_damages"));

        addButton(6, new ItemHandle(Material.WITHER_SKELETON_SKULL)
                        .setName("§fTrier par §bKills")
                        .addHideFlags().build()
                , (p, e) -> sortPlayers("kills"));

        prevPageItem.item().setType(Material.RED_STAINED_GLASS_PANE);
        nextPageItem.item().setType(Material.GREEN_STAINED_GLASS_PANE);
        sortPlayers("balance");
    }

    private void sortPlayers(@Nullable String sortKey){
        items.clear();
        final PresortedArray<PlayerData> sortedPlayers = Syncro.instance().stats().getSortedPlayers();

        for(int i = 0; i < icons.length; i++)
            items.add(i, icons[sortedPlayers.getIndex(i, sortKey)]);

        loadPage(0);
        updateNavButtons();
    }

    @Override
    protected void itemClicked(int i, Player player, InventoryClickEvent e) {}

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 54, s);
    }
}
