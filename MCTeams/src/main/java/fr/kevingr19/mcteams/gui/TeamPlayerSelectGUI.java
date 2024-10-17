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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamPlayerSelectGUI extends SimplePageGUI {

    private static final String TEAMTARGET_TAG = "mcteams.teamtarget";

    public TeamPlayerSelectGUI() {
        super(MCTeams.instance(), "Joueur à placer", 0, 8, 9, 35);

        final Set<UUID> noTeamOnlinePlayers = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toSet());
        final List<ItemStack> teamPlayerHeads = new ArrayList<>();

        for(MCTeam team : MCTeams.instance().teamManager().getTeams().values()){
            final String bottomLine = "§fÉquipe : " + team.getFullName();
            for(UUID uuid : team.getPlayers()){
                noTeamOnlinePlayers.remove(uuid);
                teamPlayerHeads.add(generateHead(uuid, bottomLine));
            }
        }

        for(UUID uuid : noTeamOnlinePlayers) items.add(generateHead(uuid, "§7§oPas d'équipe"));
        items.addAll(teamPlayerHeads);

        ItemUtils.outlineInventory(inventory, 1, 1, 0, 0);
        loadPage(page);

        prevPageItem = new ItemHandle(Material.RED_STAINED_GLASS_PANE).addHideFlags();
        nextPageItem = new ItemHandle(Material.GREEN_STAINED_GLASS_PANE).addHideFlags();
        updateNavButtons();
    }

    private static ItemStack generateHead(UUID uuid, String bottomLine){
        final OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(uuid);
        final ItemHandle handle = new ItemHandle(Material.PLAYER_HEAD)
                .setName((offPlayer.isOnline() ? "§a" : "§7") + offPlayer.getName())
                .setLore(bottomLine)
                .addUUID(TEAMTARGET_TAG, uuid)
                .addHideFlags();

        ((SkullMeta) handle.meta()).setOwningPlayer(offPlayer);
        return handle.build();
    }

    @Override
    protected void itemClicked(int i, Player player, InventoryClickEvent inventoryClickEvent) {
        CompoundTag tag = new ItemHandle(items.get(i)).tag();
        if(tag.hasUUID(TEAMTARGET_TAG)){
            final UUID target = tag.getUUID(TEAMTARGET_TAG);
            new TeamSelectGUI((p, teamId) -> MCTeams.instance().changeTeamCommand(player, target, teamId)).open(player);
        }
    }

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 45, s);
    }
}
