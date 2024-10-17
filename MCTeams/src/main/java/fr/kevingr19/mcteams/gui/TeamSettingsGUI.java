package fr.kevingr19.mcteams.gui;

import fr.kevingr19.mclib.gui.SimplePageGUI;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import fr.kevingr19.mclib.teams.MCTeam;
import fr.kevingr19.mcteams.MCTeams;
import fr.kevingr19.mcteams.MCTeamsSettings;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TeamSettingsGUI extends SimplePageGUI {

    private static final String TEAMID_TAG = "mcteams.teamid";
    private static final int TEAMPVP_SLOT = 38;
    private static final int PLAYERCOUNT_SLOT = 40;
    private static final int PLAYERCHOICE_SLOT = 42;

    private final Map<String, Boolean> _teamEnabling = new HashMap<>();
    private MCTeamsSettings _settings;

    public TeamSettingsGUI() {
        super(MCTeams.instance(), "Paramètres d'équipe", 0, 8, 9, 26);

        _settings = MCTeams.instance().settings().clone();

        for(MCTeam team : MCTeams.instance().teamManager().getTeams().values()){
            _teamEnabling.put(team.id, team.isEnabled());
            items.add(generateTeamIcon(team));
        }

        ItemUtils.outlineInventory(inventory, 1, 3, 0, 0);

        addButton(TEAMPVP_SLOT, null, (p, e) -> {
            if(e != null) _settings.setTeamPvP(!_settings.isTeamPvP());
            inventory.setItem(TEAMPVP_SLOT, generateBooleanSettings(Material.IRON_SWORD, "§6Team PvP", _settings.isTeamPvP()));
        });
        addButton(PLAYERCOUNT_SLOT, null, (p, e) -> {
            if(e != null){
                if(e.isRightClick()) _settings.setMaxPlayers(_settings.getMaxPlayers()-1);
                else _settings.setMaxPlayers(_settings.getMaxPlayers()+1);
            }
            inventory.setItem(PLAYERCOUNT_SLOT, new ItemHandle(Material.PLAYER_HEAD, Math.max(1, _settings.getMaxPlayers()))
                    .setName("§6Joueurs par équipe")
                    .setLore("§fMax : §3" + (_settings.getMaxPlayers() > 0 ? _settings.getMaxPlayers() : "Illimité"))
                    .setGlowingEnchant().addHideFlags().build());
        });
        addButton(PLAYERCHOICE_SLOT, null, (p, e) -> {
            if(e != null) _settings.setPlayerTeamChoice(!_settings.isPlayerTeamChoice());
            inventory.setItem(PLAYERCHOICE_SLOT, generateBooleanSettings(Material.WHITE_BANNER, "§6Choix des équipes par les joueurs", _settings.isPlayerTeamChoice()));
        });

        // Update first time to generate item buttons
        buttons.get(TEAMPVP_SLOT).accept(null, null);
        buttons.get(PLAYERCOUNT_SLOT).accept(null, null);
        buttons.get(PLAYERCHOICE_SLOT).accept(null, null);

        addButton(45, new ItemHandle(Material.RED_WOOL).setName("§cAnnuler").addHideFlags().build(), (p, e) -> Close(p));
        addButton(53, new ItemHandle(Material.GREEN_WOOL).setName("§aSauvegarder").addHideFlags().build(), (p, e) -> {
            if(p.hasPermission("mcteams.admin")) applyChanges();
            Close(p);
        });

        loadPage(page);

        prevPageItem = new ItemHandle(Material.RED_STAINED_GLASS_PANE).addHideFlags();
        nextPageItem = new ItemHandle(Material.GREEN_STAINED_GLASS_PANE).addHideFlags();
        updateNavButtons();
    }

    private ItemStack generateTeamIcon(MCTeam team){
        final ItemHandle handle = new ItemHandle(team.getIcon(), true)
                .setName(team.getFullName())
                .addString(TEAMID_TAG, team.id)
                .addHideFlags();

        if(_teamEnabling.get(team.id)) handle.setLore("§aActivée");
        else handle.setLore("§4Désactivée").item().setType(Material.BARRIER);

        return handle.build();
    }

    private ItemStack generateBooleanSettings(Material material, String name, boolean value){
        final ItemHandle handle = new ItemHandle(material).setName(name).addHideFlags();
        if(value) handle.setLore("§aActivé").setGlowingEnchant();
        else handle.setLore("§7Désactivé");
        return handle.build();
    }

    private void applyChanges(){
        MCTeams.instance().applySettings(_settings);
        MCTeams.instance().applyTeamEnabling(_teamEnabling);
    }

    @Override
    protected void itemClicked(int i, Player player, InventoryClickEvent e) {
        CompoundTag tag = new ItemHandle(items.get(i)).tag();
        if(tag.contains(TEAMID_TAG)){
            final String teamId = tag.getString(TEAMID_TAG);
            final MCTeam team = MCTeams.instance().teamManager().getTeam(teamId);
            if(team.isLocked()) return;

            _teamEnabling.put(teamId, !_teamEnabling.get(teamId));
            items.set(i, generateTeamIcon(team));
            loadPage(page);
        }
    }

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 54, s);
    }
}
