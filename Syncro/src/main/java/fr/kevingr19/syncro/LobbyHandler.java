package fr.kevingr19.syncro;

import fr.kevingr19.gamesetup.GameSetup;
import fr.kevingr19.gamesetup.NewGameOwnerEvent;
import fr.kevingr19.gamesetup.NoGameOwnerEvent;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.lobby.*;
import fr.kevingr19.mclib.lobby.builders.WallBuilder;
import fr.kevingr19.mclib.scheduler.RunnableUtils;
import fr.kevingr19.mcteams.MCTeams;
import fr.kevingr19.syncro.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

public class LobbyHandler implements Listener {

    private final LobbyHotbar _hotbar;
    private final LobbyHotbar _ownerHotbar;
    private final Lobby _lobby;

    public LobbyHandler(){
        final LobbySettings settings = new LobbySettings();
        settings.spawnMobs = true;
        settings.setTimeOnWait = true;
        settings.setTimeOnWaitValue = 4000;
        settings.freezeTime = true;
        settings.noWeather = true;

        _hotbar = new LobbyHotbar();
        _ownerHotbar = new LobbyHotbar();
        buildLobbyHotbars();

        _lobby = new Lobby(
                Syncro.instance(),
                WallBuilder.random(15, 5),
                new ILobbyPlayerHandler() {
                    @Override
                    public void playerAdded(LPlayer lPlayer) {
                        lPlayer.setHotbar(GameSetup.instance().isOwner(lPlayer.getUuid()) ? _ownerHotbar : _hotbar);
                    }

                    @Override public void playerSpawned(Player player, LPlayer lPlayer) {
                        Lobby.ResetPlayer(player);
                        player.setHealth(player.getMaxHealth()-0.001); // Update health visuals
                    }
                    @Override public void playerDespawned(Player player) {
                        registerPlayerInGame(player);
                    }
                },
                settings);
    }

    private void buildLobbyHotbars(){
        final ItemStack selectTeam = new ItemHandle(Material.WHITE_BANNER)
                .setName("§bChoisir une équipe")
                .addHideFlags().build();

        final ItemStack rulesBook = new ItemHandle(Material.BOOK)
                .setName("§bRègles du jeu")
                .setGlowingEnchant()
                .addHideFlags().build();

        _hotbar.setButton(3, rulesBook, Syncro.instance()::openRules);
        _hotbar.setButton(5, selectTeam, MCTeams.instance()::teamSelectCommand);

        _ownerHotbar.setButton(0, new ItemHandle(Material.ENDER_EYE)
                .setName("§6Paramètres des équipes")
                .addHideFlags().build(), MCTeams.instance()::teamSettingsCommand);

        _ownerHotbar.setButton(1, new ItemHandle(Material.PLAYER_HEAD)
                .setName("§6Placer un joueur")
                .addHideFlags().build(), MCTeams.instance()::teamSetPlayerCommand);

        _ownerHotbar.setButton(2, selectTeam, MCTeams.instance()::teamSelectCommand);

        _ownerHotbar.setButton(4, rulesBook, Syncro.instance()::openRules);
        _ownerHotbar.setButton(5, new ItemHandle(Material.IRON_SWORD)
                .setName("§6Paramètres de jeu")
                .setGlowingEnchant().addHideFlags().build(), Syncro.instance()::settingsCommand);

        _ownerHotbar.setButton(8, new ItemHandle(Material.FEATHER)
                .setName("§6Choisir un nouveau maître du jeu")
                .addHideFlags().build(), GameSetup.instance()::chooseNewOwner);
    }

    public void startLobby(Location spawn){
        Bukkit.getPluginManager().registerEvents(this, Syncro.instance());
        _lobby.start(spawn);

        for(Player player : Bukkit.getOnlinePlayers()) _lobby.addPlayer(player.getUniqueId());
    }

    public void stopLobby(boolean kill){
        if(kill) _lobby.kill();
        else _lobby.stop();

        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        _lobby.addPlayer(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onNewOwner(NewGameOwnerEvent e){
        setHotbar(e.getOldOwner(), _hotbar);
        setHotbar(e.getNewOwner().getUniqueId(), _ownerHotbar);
    }

    @EventHandler
    public void onNoOwner(NoGameOwnerEvent e){
        setHotbar(e.getOldOwner(), _hotbar);
    }

    private void setHotbar(UUID uuid, LobbyHotbar hotbar){
        if(_lobby.hasPlayer(uuid)) _lobby.getPlayerData(uuid).setHotbar(hotbar);
    }

    private void registerPlayerInGame(Player player){
        Lobby.ResetPlayer(player);
        player.teleport(_lobby.getSpawn().getWorld().getSpawnLocation());

        final Syncro inst = Syncro.instance();
        if(inst.state() != GameState.START && !inst.hasGameStarted()) return;

        if(MCTeams.instance().teamManager().hasPlayer(player.getUniqueId())){
            player.setGameMode(GameMode.SURVIVAL);
            Lobby.ResetAdvancements(player);

            Syncro.instance().stats().add(player);
            Scoreboard s = inst.endGameListener().registerSidebar(player);
            player.setScoreboard(s);

            if(inst.state() == GameState.START){
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 10, false, false));

                final int timeInvic = inst.settings().getInvincibilitySeconds();
                if(timeInvic > 0){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, timeInvic*20, 255, false, false));
                    player.sendMessage("§7§l>§7 Vous avez reçu %d secondes d'invincibilité.".formatted(timeInvic));
                }
            }

            final Inventory inv = inst.settings().getStartingInventory();
            for(int i = 0; i < inv.getSize(); i++) player.getInventory().setItem(i, inv.getItem(i));
        }
        else{
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

}
