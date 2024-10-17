package fr.kevingr19.syncro.gui;

import fr.kevingr19.mclib.gui.SimplePageGUI;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import fr.kevingr19.syncro.Syncro;
import fr.kevingr19.syncro.game.TeamData;
import fr.kevingr19.syncro.utils.PresortedArray;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class TeamsStatsGUI extends SimplePageGUI {

    private static final String STATS_TEAMID = "syncro.stats.teamid";

    private final ItemStack[] icons;

    public TeamsStatsGUI() {
        super(Syncro.instance(), "Statistiques des équipes", 9, 17, 18, 35);

        final PresortedArray<TeamData> sortedTeams = Syncro.instance().stats().getSortedTeams();
        icons = new ItemStack[sortedTeams.length];

        for(int i = 0; i < icons.length; i++){
            final TeamData data = sortedTeams.get(i);
            icons[i] = data.getDataIcon().addString(STATS_TEAMID, data.team.id).build();
        }

        ItemUtils.outlineInventory(inventory, 2, 1, 0, 0);

        addButton(2, new ItemHandle(Material.GOLDEN_APPLE)
                .setName("§fTrier par §bBalance")
                .addHideFlags().build()
        , (p, e) -> sortTeams("balance"));

        addButton(3, new ItemHandle(Material.ARROW)
                        .setName("§fTrier par §bDégâts")
                        .addHideFlags().build()
                , (p, e) -> sortTeams("damages"));

        addButton(4, new ItemHandle(Material.SKELETON_SKULL)
                        .setName("§fTrier par §bMorts")
                        .addHideFlags().build()
                , (p, e) -> sortTeams("deaths"));

        addButton(5, new ItemHandle(Material.DIAMOND_SWORD)
                        .setName("§fTrier par §bDégâts Infligés")
                        .addHideFlags().build()
                , (p, e) -> sortTeams("inflicted_damages"));

        addButton(6, new ItemHandle(Material.WITHER_SKELETON_SKULL)
                        .setName("§fTrier par §bKills")
                        .addHideFlags().build()
                , (p, e) -> sortTeams("kills"));

        prevPageItem.item().setType(Material.RED_STAINED_GLASS_PANE);
        nextPageItem.item().setType(Material.GREEN_STAINED_GLASS_PANE);
        sortTeams("balance");
    }

    private void sortTeams(@Nullable String sortKey){
        items.clear();
        final PresortedArray<TeamData> sortedTeams = Syncro.instance().stats().getSortedTeams();

        for(int i = 0; i < icons.length; i++)
            items.add(i, icons[sortedTeams.getIndex(i, sortKey)]);

        loadPage(0);
        updateNavButtons();
    }

    @Override
    protected void itemClicked(int i, Player player, InventoryClickEvent e) {
        CompoundTag tag = new ItemHandle(items.get(i)).tag();
        if(tag.contains(STATS_TEAMID)) Syncro.instance().openTeamStats(player, tag.getString(STATS_TEAMID));
    }

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 36, s);
    }
}
