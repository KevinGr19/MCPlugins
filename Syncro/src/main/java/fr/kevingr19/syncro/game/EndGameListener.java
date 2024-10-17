package fr.kevingr19.syncro.game;

import fr.kevingr19.mclib.MCLib;
import fr.kevingr19.mclib.events.EventUtils;
import fr.kevingr19.mclib.miscellaneous.DragonFightHandler;
import fr.kevingr19.mclib.scheduler.RunnableUtils;
import fr.kevingr19.mcteams.MCTeams;
import fr.kevingr19.syncro.Syncro;
import fr.kevingr19.syncro.events.*;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.*;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class EndGameListener implements Listener {

    private DragonFightHandler _dragonFightHandler;
    private HungerManager _hungerManager;

    private PlayerSidebar _defaultSidebar;
    private final Map<UUID, PlayerSidebar> _sidebars = new HashMap<>();

    private boolean _stopped = true;

    public void start(){
        if(!_stopped) return;
        _stopped = false;

        _hungerManager = new HungerManager(Syncro.instance().settings().getFoodMultiplier());
        MCLib.registerListener(this, Syncro.instance());
    }
    public void stop(){
        if(_stopped) return;
        _stopped = true;

        HandlerList.unregisterAll(this);

        if(_dragonFightHandler != null) _dragonFightHandler.stop();
        _dragonFightHandler = null;
    }

    public void registerDefaultSidebar(){
        _defaultSidebar = new PlayerSidebar(null, MCTeams.instance().scoreboardPool().get("mcteams.main"));
        startSidebar(_defaultSidebar);
    }

    public Scoreboard registerSidebar(Player player){
        Scoreboard scoreboard = MCTeams.instance().scoreboardPool().create(player.getUniqueId().toString());
        PlayerSidebar sidebar = new PlayerSidebar(player, scoreboard);

        startSidebar(sidebar);
        _sidebars.put(player.getUniqueId(), sidebar);

        return scoreboard;
    }

    private void startSidebar(PlayerSidebar sidebar){
        sidebar.runTaskTimer(Syncro.instance(), 0, 20);
    }

    public void handlePlayer(Player player){
        final Syncro inst = Syncro.instance();
        if((inst.state() == GameState.END && !inst.winner().isWinner(player)) || !syncHealth(player)){
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e){
        PlayerSidebar sidebar = _sidebars.getOrDefault(e.getPlayer().getUniqueId(), null);
        if(sidebar != null) e.getPlayer().setScoreboard(sidebar.getScoreboard());

        handlePlayer(e.getPlayer());
    }

    //region Game mechanics
    private boolean syncHealth(Player player){
        final TeamData teamData = Syncro.instance().stats().getTeam(player);
        if(teamData == null) return false;

        teamData.setHealthToPlayer(player);
        return true;
    }

    public void onPlayerEvent(final EntityEvent e, final TriConsumer<Player, TeamData, PlayerData> action){
        if(!(e.getEntity() instanceof final Player player)) return;

        if(Syncro.instance().stats().has(player)){
            final PlayerData playerData = Syncro.instance().stats().get(player);
            action.accept(player, playerData.teamData, playerData);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        syncHealth(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent e){
        onPlayerEvent(e, (player, teamData, playerData) -> {
            final double heal = teamData.heal(e.getAmount());
            Bukkit.getPluginManager().callEvent(new TeamPlayerHealEvent(player, playerData, teamData, heal));

            teamData.syncronizeHealth(player);
        });
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e){
        onPlayerEvent(e, (player, teamData, playerData) -> {
            final double damage = teamData.damage(e.getFinalDamage());

            final Player damager = EventUtils.getPlayerDamager(e);
            final PlayerData damagerData = Syncro.instance().stats().get(damager);
            TeamPlayerDamageEvent event;

            if(damager == null || damagerData == null) event = new TeamPlayerDamageEvent(player, playerData, teamData, damage);
            else event = new TeamPlayerHitEvent(player, playerData, teamData, damage, damager, damagerData, damagerData.teamData);

            Bukkit.getPluginManager().callEvent(event);

            teamData.syncronizeHealth(player);

            if(teamData.isDead()){
                teamData.currentDeath = player;
                teamData.resetHealth();
            }
        });
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        onPlayerEvent(e, (player, teamData, playerData) -> {
            if(player.equals(teamData.currentDeath)){

                final Player killer = player.getKiller();
                final PlayerData killerData = Syncro.instance().stats().get(killer);

                TeamPlayerDeathEvent event;
                if(killer == null || killerData == null) event = new TeamPlayerDeathEvent(player, playerData, teamData, e);
                else event = new TeamPlayerKillEvent(player, playerData, teamData, e, killer, killerData, killerData.teamData);

                Bukkit.getPluginManager().callEvent(event);

                e.setKeepInventory(false);
                e.setKeepLevel(false);
                teamData.currentDeath = null;

                final Inventory teamInv = teamData.getInventory();
                if(teamInv != null){
                    e.getDrops().addAll(Arrays.stream(teamInv.getContents()).filter(Objects::nonNull).toList());
                    teamInv.clear();
                }
            }
            else{
                e.setKeepInventory(true);
                e.setKeepLevel(true);
                e.setDroppedExp(0);
                e.getDrops().clear();
            }
        });
    }

    @EventHandler
    public void onTeamDeath(TeamPlayerDeathEvent e){
        e.sourceEvent.setDeathMessage(null);
        String message;

        if(!(e instanceof TeamPlayerKillEvent event))
            message = "§fL'équipe %s§r est morte (%s§r).".formatted(e.teamData.team.getFullName(), e.player.getDisplayName());
        else if(!event.isSuicide())
            message = "§fL'équipe %s§r a été tuée par %s§r (%s§r).".formatted(e.teamData.team.getFullName(), event.killer.getDisplayName(), e.player.getDisplayName());
        else
            message = "§fL'équipe %s§r s'est suicidée (%s§r).".formatted(e.teamData.team.getFullName(), e.player.getDisplayName());

        Syncro.broadcast(message);
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent e){
        if(!(e.getEntity() instanceof Player player)) return;
        if(!MCTeams.instance().teamManager().hasPlayer(player.getUniqueId())) return;

        double basis = _hungerManager.has(player) ? _hungerManager.get(player) : player.getFoodLevel();
        double change = player.getFoodLevel() - e.getFoodLevel();

        _hungerManager.set(player, basis - change * (change > 0 ? _hungerManager.foodMultiplier : 1));
        e.setFoodLevel((int) Math.ceil(_hungerManager.get(player)));
    }
    //endregion

    //region Dragon fight
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEndTravel(PlayerPortalEvent e){
        if(e.getTo().getWorld().getEnvironment() != World.Environment.THE_END) return;
        if(_dragonFightHandler == null){
            RunnableUtils.Delay(() ->
            {
                final DragonBattle battle = e.getTo().getWorld().getEnderDragonBattle();
                if(battle == null) return;

                final EnderDragon dragon = battle.getEnderDragon();
                if(dragon != null) createDragonFightHandler(dragon);

            }, 1, Syncro.instance());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonRespawn(EntitySpawnEvent e){
        if(!(e.getEntity() instanceof EnderDragon dragon)) return;
        if(dragon.getDragonBattle() == null) return;

        if(dragon.getWorld().getEnvironment() == World.Environment.THE_END && _dragonFightHandler == null)
            createDragonFightHandler(dragon);
    }

    private void createDragonFightHandler(final EnderDragon dragon){
        _dragonFightHandler = new DragonFightHandler(Syncro.instance(), dragon, () -> Syncro.instance().endGame(dragon.getKiller()), this::victoryAnimation);
    }

    private void victoryAnimation(){
        final GameWinner winner = Syncro.instance().winner();
        if(winner == null) return;

        new BukkitRunnable(){
            private int fireworks = 20;
            @Override public void run(){
                Random rd = new Random();
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if(!winner.isWinner(player)) return;

                    Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder()
                            .trail(true)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(Color.fromRGB(rd.nextInt(16777216)))
                            .build());
                    firework.setFireworkMeta(meta);
                    firework.setMaxLife(30);
                });
                if(fireworks-- <= 0) cancel();
            }
        }.runTaskTimer(Syncro.instance(), 0, 10);
    }
    //endregion
}
