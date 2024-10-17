package fr.kevingr19.syncro.game;

import fr.kevingr19.mclib.item.ItemHandle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData extends Datable {

    public final UUID uuid;
    public final TeamData teamData;

    public int selfKills = 0;

    public PlayerData(UUID uuid, TeamData teamData){
        this.uuid = uuid;
        this.teamData = teamData;
    }

    @Override
    public String[] getDataText(){
        return new String[]{
                "§f- Morts : §4%d§r §7(dont %d suicides)".formatted(deaths, suicides),
                "§f- Fratricides : §4%d".formatted(selfKills),
                "",
                "§f- Soins : §a%.1f".formatted(heals),
                "§f- Dégâts : §c%.1f".formatted(damages),
                "§f- Balance : %s".formatted(getBalanceText()),
                "",
                "§f- Dégâts infligés : §c%.1f".formatted(inflictedDamages),
                "§f- Kills : §4%d".formatted(kills)
        };
    }

    public ItemHandle getDataIcon(){
        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        final ItemHandle handle = new ItemHandle(Material.PLAYER_HEAD)
                .setName(teamData.team.getPlayerName(player.getName()))
                .setLore(getDataText()).addHideFlags();

        ((SkullMeta) handle.meta()).setOwningPlayer(player);
        return handle;
    }

    public String getFullName(){
        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return teamData.team.getPlayerName(player.getName());
    }

}
