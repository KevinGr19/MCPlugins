package fr.kevingr19.mclib.lobby;

import fr.kevingr19.mclib.MCLib;
import fr.kevingr19.mclib.scheduler.RunnableUtils;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class Lobby implements Listener {

    private final Plugin _plugin;

    private Location _spawn;
    private final ILobbyBuilder _lobbyBuilder;
    private final ILobbyPlayerHandler _playerHandler;
    private LobbySettings _settings;

    private boolean _running = false;
    private LobbyCacheTracker _tracker;
    private final Map<UUID, LPlayer> _players = new HashMap<>();
    private final Set<UUID> _removeCachedPlayers = new HashSet<>();

    public Lobby(Plugin plugin, ILobbyBuilder builder, @Nullable ILobbyPlayerHandler playerHandler, @Nullable LobbySettings settings){
        _plugin = plugin;
        _lobbyBuilder = builder;
        _playerHandler = playerHandler;

        if(settings != null) setSettings(settings);
        else setSettings(new LobbySettings());
    }

    public Location getSpawn() {
        return _spawn;
    }
    public LobbySettings getSettings() {
        return _settings;
    }
    public void setSettings(LobbySettings _settings) {
        this._settings = _settings;
    }

    public boolean isRunning(){
        return _running;
    }

    public void start(Location spawn){
        if(isRunning()) return;
        _spawn = spawn;
        _removeCachedPlayers.clear();

        _lobbyBuilder.build(_spawn);
        applySettings();

        MCLib.registerListener(this, _plugin);
        _running = true;
    }

    private void applySettings(){
        final World world = _spawn.getWorld();
        if(world == null) return;

        if(_settings.freezeTime){
            _settings.cache("daylightCycle", world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }
        if(!_settings.spawnMobs){
            _settings.cache("mobSpawn", world.getGameRuleValue(GameRule.DO_MOB_SPAWNING));
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        }
        if(_settings.setTimeOnWait) {
            _settings.cache("worldFullTime", world.getFullTime());
            world.setFullTime(_settings.setTimeOnWaitValue);
        }
        if(_settings.noWeather){
            _settings.cache("weather", world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE));
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        }
    }

    public void stop(){
        if(!isRunning()) return;
        _lobbyBuilder.destroy(_spawn);

        List<UUID> players = _players.keySet().stream().toList();
        for(UUID uuid : players) removePlayer(uuid);

        HandlerList.unregisterAll(this);

        // Track players
        if(_playerHandler != null && _removeCachedPlayers.size() > 0){
            _tracker = new LobbyCacheTracker(_removeCachedPlayers, _playerHandler);
            _tracker.start(_plugin);
        }

        undoSettings();
        _running = false;
    }

    private void undoSettings(){
        final World world = _spawn.getWorld();
        if(world == null) return;

        if(_settings.hasCached("daylightCycle")) world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, (boolean)_settings.getCached("daylightCycle"));
        if(_settings.hasCached("weather")) world.setGameRule(GameRule.DO_WEATHER_CYCLE, (boolean)_settings.getCached("weather"));
        if(_settings.hasCached("mobSpawn")) world.setGameRule(GameRule.DO_MOB_SPAWNING, (boolean)_settings.getCached("mobSpawn"));
        if(_settings.hasCached("worldFullTime")) world.setTime((long)_settings.getCached("worldFullTime"));
    }

    public void kill(){
        if(isRunning()) stop();
        if(_tracker != null && _tracker.isRunning())
            _tracker.stop();
    }

    public LPlayer getPlayerData(UUID uuid){
        return _players.get(uuid);
    }
    public Set<UUID> getPlayers(){
        return Collections.unmodifiableSet(_players.keySet());
    }
    public boolean hasPlayer(UUID uuid){
        return _players.containsKey(uuid);
    }
    public boolean hasPlayer(Player player) {
        return hasPlayer(player.getUniqueId());
    }

    public Set<UUID> getRemoveCachedPlayers() { return Collections.unmodifiableSet(_removeCachedPlayers); }
    public boolean isRemoveCached(UUID uuid) { return _removeCachedPlayers.contains(uuid); }
    public boolean isRemoveCached(Player player){
        return isRemoveCached(player.getUniqueId());
    }

    public void addPlayer(UUID uuid){
        if(!isRunning() || hasPlayer(uuid)) return;

        final LPlayer lPlayer = new LPlayer(this, uuid);
        _players.put(uuid, lPlayer);
        _removeCachedPlayers.remove(uuid);

        if(_playerHandler != null) _playerHandler.playerAdded(lPlayer);

        final Player player = Bukkit.getPlayer(uuid);
        if(player != null) spawnPlayer(player);
    }

    public void removePlayer(UUID uuid){
        if(!isRunning() || !hasPlayer(uuid)) return;
        _players.remove(uuid);

        final Player player = Bukkit.getPlayer(uuid);
        if(player != null && !player.isDead()) despawnPlayer(player);
        else _removeCachedPlayers.add(uuid);
    }

    private void spawnPlayer(Player player){
        player.setGameMode(GameMode.ADVENTURE);

        final LPlayer lPlayer = _players.get(player.getUniqueId());
        player.teleport(Optional.ofNullable(lPlayer.spawnPoint).orElse(_spawn));
        if(_playerHandler != null) _playerHandler.playerSpawned(player, lPlayer);
        if(lPlayer.getHotbar() != null) lPlayer.getHotbar().setToPlayer(player);
    }

    private void despawnPlayer(Player player){
        if(_playerHandler != null) _playerHandler.playerDespawned(player);
    }

    // SPAWNING

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e){
        onPlayerSpawn(e.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e){
        RunnableUtils.Delay(() -> onPlayerSpawn(e.getPlayer()), 0, _plugin);
    }

    private void onPlayerSpawn(Player player){
        if(hasPlayer(player)) spawnPlayer(player);
        else if(isRemoveCached(player)){
            despawnPlayer(player);
            _removeCachedPlayers.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent e){
        if(!hasPlayer(e.getEntity())) return;
        e.getDrops().clear();
        e.setNewExp(0);
        e.setNewLevel(0);
        e.setNewTotalExp(0);
        e.setDroppedExp(0);
    }

    // INTERACTIONS

    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onClick(final InventoryClickEvent e)
    {
        final Player player = (Player) e.getWhoClicked();
        if(!hasPlayer(player.getUniqueId())) return;

        final LobbyHotbar hotbar = getPlayerHotbar(player);
        if(hotbar == null) return;

        final int slot = e.getHotbarButton();
        if(hotbar.hasAction(slot))
            e.setCancelled(true);

        else if(e.getClickedInventory() == player.getInventory() && hotbar.hasAction(e.getSlot()))
            e.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onInteract(final PlayerInteractEvent e){
        final Player player = e.getPlayer();
        if(!hasPlayer(player.getUniqueId())) return;

        boolean cancel = defaultCancel(player);
        e.setCancelled(cancel);

        if(e.getHand() != EquipmentSlot.HAND) return;
        if(e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final LobbyHotbar hotbar = getPlayerHotbar(player);
        cancel |= hotbar != null && hotbar.execute(player.getInventory().getHeldItemSlot(), player);
        e.setCancelled(cancel);
    }

    @EventHandler (ignoreCancelled = true)
    public void onSwap(final PlayerSwapHandItemsEvent e){
        final Player player = e.getPlayer();
        if(!hasPlayer(player.getUniqueId())) return;

        final LobbyHotbar hotbar = getPlayerHotbar(player);
        if(hotbar != null && hotbar.hasAction(player.getInventory().getHeldItemSlot())) e.setCancelled(true);
    }

    @Nullable
    private LobbyHotbar getPlayerHotbar(final Player player){
        return _players.get(player.getUniqueId()).getHotbar();
    }

    // ALWAYS CANCELLED

    @EventHandler (priority = EventPriority.LOW)
    public void onFoodChange(final FoodLevelChangeEvent e) {
        if(!hasPlayer(e.getEntity().getUniqueId())) return;
        e.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onHit(final EntityDamageByEntityEvent e){
        if(hasPlayer(e.getEntity().getUniqueId())) e.setCancelled(true);
    }

    // CANCELLED ON NON-OP

    @EventHandler (ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent e){
        e.setCancelled(defaultCancel(e.getPlayer()));
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent e){
        e.setCancelled(defaultCancel(e.getPlayer()));
    }

    @EventHandler (ignoreCancelled = true)
    public void onDrop(final PlayerDropItemEvent e){
        e.setCancelled(defaultCancel(e.getPlayer()));
    }

    private boolean defaultCancel(Player player){
        return hasPlayer(player) && player.getGameMode() != GameMode.CREATIVE || !player.isOp();
    }

    public static void ResetPlayer(final Player player){
        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setLevel(0);
        player.setExp(0);
        player.setInvisible(false);
        player.setArrowsInBody(0);
        player.setFireTicks(0);
        player.setBedSpawnLocation(null, true);
        player.getActivePotionEffects().forEach(p -> player.removePotionEffect(p.getType()));
        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
    }

    public static void ResetAdvancements(final Player player){
        final Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
        while(iterator.hasNext()){
            final AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
            for(String s : progress.getAwardedCriteria()) progress.revokeCriteria(s);
        }
    }

}
