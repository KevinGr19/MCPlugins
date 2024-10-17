package fr.kevingr19.mcteams.gui;

import fr.kevingr19.mclib.gui.SimplePageGUI;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import fr.kevingr19.mclib.teams.MCTeam;
import fr.kevingr19.mcteams.MCTeams;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TeamSelectGUI extends SimplePageGUI {

    private static final String JOINTEAM_TAG = "mcteams.jointeam";

    private final BiConsumer<Player, String> _teamSelectCallback;

    public TeamSelectGUI(){
        this(null);
    }
    public TeamSelectGUI(BiConsumer<Player, String> teamSelectCallback) {
        super(MCTeams.instance(), "Choississez une équipe", 0, 8, 9, 26);

        _teamSelectCallback = teamSelectCallback;

        for(MCTeam team : MCTeams.instance().teamManager().getTeams().values()){
            if(!team.isEnabled()) continue;
            ItemHandle icon = new ItemHandle(team.getIcon(), true);
            String countColor;

            if(team.isFull()) countColor = "§c";
            else{
                countColor = "§a";
                icon.setGlowingEnchant();
            }

            List<String> lore = new ArrayList<>();
            lore.add("§fJoueurs : "  +  countColor + team.getPlayerCount() + (team.isUnlimited() ? "" : "/" + team.getMaxPlayers()));
            for(UUID uuid : team.getPlayers()){
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                lore.add("§7- " + player.getName());
            }

            icon.setLore(lore).addString(JOINTEAM_TAG, team.id);

            icon.setName(team.getFullName()).addHideFlags();
            items.add(icon.build());
        }

        ItemUtils.outlineInventory(inventory, 1, 1, 0, 0);

        addButton(inventory.getSize()-1,
            new ItemHandle(Material.BARRIER).setName("§cQuitter son équipe").addHideFlags().build(),
            (p, e) -> changeTeam(p, null));

        loadPage(page);

        prevPageItem = new ItemHandle(Material.RED_STAINED_GLASS_PANE).addHideFlags();
        nextPageItem = new ItemHandle(Material.GREEN_STAINED_GLASS_PANE).addHideFlags();
        updateNavButtons();
    }

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 36, s);
    }

    @Override
    protected void itemClicked(int i, Player player, InventoryClickEvent inventoryClickEvent) {
        CompoundTag tag = new ItemHandle(items.get(i)).tag();
        if(tag.contains(JOINTEAM_TAG)) changeTeam(player, tag.getString(JOINTEAM_TAG));
    }

    private void changeTeam(Player player, @Nullable String teamId){
        if(_teamSelectCallback != null) _teamSelectCallback.accept(player, teamId);
        else MCTeams.instance().changeTeamCommand(player, player.getUniqueId(), teamId);
        Close(player);
    }
}
