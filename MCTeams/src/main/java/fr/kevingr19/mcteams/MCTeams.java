package fr.kevingr19.mcteams;

import fr.kevingr19.mclib.events.EventUtils;
import fr.kevingr19.mclib.scoreboard.ScoreboardPool;
import fr.kevingr19.mclib.teams.MCTeam;
import fr.kevingr19.mclib.teams.PlayerChangeTeamEvent;
import fr.kevingr19.mclib.teams.TeamManager;
import fr.kevingr19.mcteams.commands.BroadcastMessageCommand;
import fr.kevingr19.mcteams.commands.TeamCommand;
import fr.kevingr19.mcteams.commands.TeamMessageCommand;
import fr.kevingr19.mcteams.commands.TeamSendPositionCommand;
import fr.kevingr19.mcteams.gui.TeamPlayerSelectGUI;
import fr.kevingr19.mcteams.gui.TeamSelectGUI;
import fr.kevingr19.mcteams.gui.TeamSettingsGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;

public final class MCTeams extends JavaPlugin implements Listener {

    private static MCTeams _inst;
    public static MCTeams instance() {
        return _inst;
    }

    public static void info(String msg){
        _inst.getLogger().info(msg);
    }
    public static void warn(String msg){
        _inst.getLogger().warning(msg);
    }
    public static void error(String msg){
        _inst.getLogger().severe(msg);
    }
    
    private static final TeamData[] TEAMS_DATA = {
            new TeamData("red", "Rouge", ChatColor.RED, new ItemStack(Material.RED_BANNER), "", ""),
            new TeamData("orange", "Orange", ChatColor.GOLD, new ItemStack(Material.ORANGE_BANNER), "", ""),
            new TeamData("yellow", "Jaune", ChatColor.YELLOW, new ItemStack(Material.YELLOW_BANNER), "", ""),
            new TeamData("lime", "Verte", ChatColor.GREEN, new ItemStack(Material.LIME_BANNER), "", ""),
            new TeamData("green", "Verte Foncé", ChatColor.DARK_GREEN, new ItemStack(Material.GREEN_BANNER), "", ""),
            new TeamData("aqua", "Aqua", ChatColor.AQUA, new ItemStack(Material.LIGHT_BLUE_BANNER), "", ""),
            new TeamData("cyan", "Cyan", ChatColor.DARK_AQUA, new ItemStack(Material.CYAN_BANNER), "", ""),
            new TeamData("blue", "Bleue", ChatColor.BLUE, new ItemStack(Material.BLUE_BANNER), "", ""),
            new TeamData("purple", "Violette", ChatColor.DARK_PURPLE, new ItemStack(Material.PURPLE_BANNER), "", ""),
            new TeamData("pink", "Rose", ChatColor.LIGHT_PURPLE, new ItemStack(Material.PINK_BANNER), "", ""),
    };

    private ScoreboardPool _scoreboardPool;
    private TeamManager _teamManager;

    public boolean isLocked;
    private MCTeamsSettings _settings;

    public ScoreboardPool scoreboardPool(){
        return _scoreboardPool;
    }
    public TeamManager teamManager(){
        return _teamManager;
    }
    public MCTeamsSettings settings(){
        return _settings;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        _inst = this;

        buildTeamManager();
        getServer().getPluginManager().registerEvents(this, this);

        getServer().getPluginCommand("team").setExecutor(new TeamCommand());
        getServer().getPluginCommand("t").setExecutor(new TeamMessageCommand());
        getServer().getPluginCommand("g").setExecutor(new BroadcastMessageCommand());
        getServer().getPluginCommand("sendpos").setExecutor(new TeamSendPositionCommand());

        isLocked = false;
        applySettings(new MCTeamsSettings());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        _teamManager.getTeams().values().forEach(t -> {
            _teamManager.unlockTeam(t.id);
            t.clearPlayers();
        });

        Bukkit.getOnlinePlayers().forEach(p -> {
            p.setScoreboard(getServer().getScoreboardManager().getMainScoreboard());
            updateVisibleName(p);
        });
    }

    private void buildTeamManager(){
        _scoreboardPool = new ScoreboardPool();
        _scoreboardPool.create("mcteams.main");

        _teamManager = new TeamManager(this, _scoreboardPool);

        for(TeamData data : TEAMS_DATA){
            MCTeam team = _teamManager.addTeam(data.id());
            team.setColor(data.color());
            team.setName(data.name());
            team.setIcon(data.icon());
            team.setPrefix(data.prefix());
            team.setSuffix(data.suffix());
        }

        Bukkit.getOnlinePlayers().forEach(p -> p.setScoreboard(_scoreboardPool.get("mcteams.main")));
    }

    public void applySettings(MCTeamsSettings settings){
        if(isLocked) return;

        _settings = settings;
        for(MCTeam team : _teamManager.getTeams().values()){
            if(team.isLocked()) continue;
            team.setMaxPlayers(_settings.getMaxPlayers());
        }

        _settings.lock();
    }

    public void applyTeamEnabling(Map<String, Boolean> teamEnabling){
        if(isLocked) return;

        for(String teamId : teamEnabling.keySet()){
            if(!_teamManager.hasTeam(teamId)) continue;
            final MCTeam team = _teamManager.getTeam(teamId);

            if(team.isLocked()) continue;

            if(teamEnabling.get(teamId)) team.enable();
            else team.disable();
        }
    }

    //region Commands
    public void changeTeamCommand(CommandSender commandSender, UUID target, @Nullable String teamId){
        if(isLocked){
            commandSender.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
            return;
        }

        if(!commandSender.hasPermission("mcteams.manager") && (!(commandSender instanceof Player p) || !p.getUniqueId().equals(target))){
            commandSender.sendMessage("§c> Vous n'avez pas la permission requise pour faire cette action.");
            return;
        }

        if(teamId != null){
            if(!_teamManager.hasTeam(teamId)) {
                commandSender.sendMessage("§c> Cette équipe n'existe pas.");
                return;
            }

            MCTeam team = _teamManager.getTeam(teamId);
            if(!team.isEnabled()) {
                commandSender.sendMessage("§c> Cette équipe n'est pas activée.");
                return;
            }

            if(team.isFull()) {
                commandSender.sendMessage("§c> Cette équipe est au complet.");
                return;
            }
        }

        try{
            _teamManager.setPlayerTeam(target, teamId);
        }
        catch (IllegalArgumentException e){
            commandSender.sendMessage("§c> Une erreur est survenue. Réessayez ultérieurement.");
        }
    }

    public boolean teamSelectCommand(Player player){
        if(isLocked || !_settings.isPlayerTeamChoice()) player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
        else new TeamSelectGUI().open(player);
        return true;
    }

    public boolean teamSetPlayerCommand(Player player){
        if(!player.hasPermission("mcteams.manager")){
            player.sendMessage("§c> Vous n'avez pas la permission de faire cette action");
            return true;
        }

        if(isLocked) player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
        else new TeamPlayerSelectGUI().open(player);
        return true;
    }

    public boolean teamSettingsCommand(Player player){
        if(!player.hasPermission("mcteams.admin")){
            player.sendMessage("§c> Vous n'avez pas la permission de faire cette action");
            return true;
        }

        if(isLocked) player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
        else new TeamSettingsGUI().open(player);
        return true;
    }
    //endregion

    public boolean sendTeamMessage(Player player, String message){
        final MCTeam team = _teamManager.getPlayerTeam(player.getUniqueId());
        if(team != null){
            team.sendTeamMessage("[Équipe] [%s§r] %s".formatted(player.getDisplayName(), message));
            return true;
        }
        return false;
    }

    public List<MCTeam> getActiveTeams(){
        return _teamManager.getTeams().values().stream().filter(t -> t.isEnabled() && t.getPlayerCount() > 0).toList();
    }

    private void updateVisibleName(Player player){
        String name = player.getName();
        final MCTeam team = _teamManager.getPlayerTeam(player.getUniqueId());

        if(team != null) name = team.getColor() + team.getPrefix() + name + team.getSuffix();
        player.setDisplayName(name);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        e.getPlayer().setScoreboard(_scoreboardPool.get("mcteams.main"));
        updateVisibleName(e.getPlayer());
    }

    @EventHandler
    public void onTeamChange(PlayerChangeTeamEvent e){
        if(e.getManager() != _teamManager || e.getPlayer() == null) return;

        if(e.getNewTeam() == null) e.getPlayer().sendMessage("> Vous avez été retiré de votre équipe.");
        else e.getPlayer().sendMessage("> Vous avez été ajouté à l'équipe " + e.getNewTeam().getFullName() + "§r.");

        updateVisibleName(e.getPlayer());
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof Player target)) return;

        final Player damager = EventUtils.getPlayerDamager(e);
        if(damager == null) return;

        if(_settings.isTeamPvP() || damager.equals(target)) return;

        MCTeam targetTeam = _teamManager.getPlayerTeam(target.getUniqueId());
        e.setCancelled(targetTeam != null && targetTeam == _teamManager.getPlayerTeam(damager.getUniqueId()));
    }

}
