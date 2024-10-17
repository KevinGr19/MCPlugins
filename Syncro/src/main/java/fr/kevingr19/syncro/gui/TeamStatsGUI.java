package fr.kevingr19.syncro.gui;

import fr.kevingr19.mclib.gui.SimplePageGUI;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import fr.kevingr19.syncro.Syncro;
import fr.kevingr19.syncro.game.PlayerData;
import fr.kevingr19.syncro.game.TeamData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class TeamStatsGUI extends SimplePageGUI {

    public TeamStatsGUI(final TeamData teamData, final List<PlayerData> playerDatas) {
        super(Syncro.instance(), "Statistiques d'équipe", 18, 26, 27, 44);
        ItemUtils.outlineInventory(inventory, 3, 0, 0, 0);

        inventory.setItem(13, teamData.getDataIcon().build());

        playerDatas.forEach(data -> items.add(data.getDataIcon().build()));

        prevPageItem.item().setType(Material.RED_STAINED_GLASS_PANE);
        nextPageItem.item().setType(Material.GREEN_STAINED_GLASS_PANE);

        loadPage(page);
        updateNavButtons();
    }

    @Override
    protected void itemClicked(int i, Player player, InventoryClickEvent inventoryClickEvent) {}

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 45, s);
    }
}
