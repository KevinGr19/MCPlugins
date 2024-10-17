package fr.kevingr19.gamesetup;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class GameSetup extends JavaPlugin implements Listener {

    private static GameSetup _inst;
    public static GameSetup instance(){
        return _inst;
    }

    private UUID _ownerUuid;
    private PermissionAttachment _ownerPermissions;

    private boolean _automatic;
    private InactivityGameKiller _inactivityGameKiller;
    private AutoRestarter _autoRestarter;

    private LobbyCommandExecutor _commandLobbyExecutor;
    private Runnable _automatedLobbyExecutor;
    public boolean changeOwnerIfDisconnect = false;

    public void setLobbyCommandExecutor(LobbyCommandExecutor executor){
        _commandLobbyExecutor = executor;
    }
    public void setAutomatedLobbyExecutor(Runnable executor){
        _automatedLobbyExecutor = executor;
    }

    public UUID getOwner(){
        return _ownerUuid;
    }
    public boolean isOwner(UUID uuid){
        return uuid.equals(_ownerUuid);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        _inst = this;
        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);
        new BukkitRunnable(){ @Override public void run(){ initialise(); } }.runTaskLater(this, 1);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfig(){
        this.saveDefaultConfig();

        final FileConfiguration config = getConfig();
        final ConfigurationSection auto_section = config.getConfigurationSection("automatic");
        if(auto_section == null){
            _automatic = false;
            _inactivityGameKiller = null;
            _autoRestarter = null;
        }
        else{
            _automatic = auto_section.getBoolean("enabled", true);

            final int restartSeconds = auto_section.getInt("inactivity_timeout", 120);
            _inactivityGameKiller = restartSeconds >= 0 ? new InactivityGameKiller(restartSeconds) : null;
            _autoRestarter = new AutoRestarter(auto_section.getInt("game_end_timeout", 240));
        }
    }

    private void initialise(){
        if(_automatic) {
            changeOwnerIfDisconnect = true;
            if (_automatedLobbyExecutor != null){
                _automatedLobbyExecutor.run();
                setFirstPlayerOwner(null);
            }
            else{
                getLogger().warning("No automated lobby executor was provided !");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
        else getServer().getPluginCommand("lobby").setExecutor(((commandSender, command, s, strings) -> {
            if(!(commandSender instanceof Player player)) return false;
            if(_commandLobbyExecutor.onCommand(player, command, s, strings)) setOwner(player);
            return true;
        }));

        getServer().getPluginCommand("setowner").setExecutor(new TabExecutor() {
            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
                if(strings.length < 1){
                    if(commandSender instanceof Player player) chooseNewOwner(player);
                    else commandSender.sendMessage("§c> Entrez le nom du joueur à devenir Owner.");
                    return true;
                }

                final Player player = Bukkit.getPlayer(strings[0]);
                if(player == null){
                    commandSender.sendMessage("§c> Ce joueur n'existe pas.");
                    return true;
                }

                setOwner(player);
                return true;
            }

            @Override
            public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
                return null;
            }
        });
    }

    void setOwner(@Nullable Player player){
        if(_ownerPermissions != null){
            _ownerPermissions.remove();
            final Player oldOwner = Bukkit.getPlayer(_ownerUuid);
            if(oldOwner != null) oldOwner.recalculatePermissions();
        }

        if(player == null){
            Bukkit.getPluginManager().callEvent(new NoGameOwnerEvent(_ownerUuid));
            _ownerPermissions = null;
            _ownerUuid = null;
            getLogger().info("Set Owner to null");
        }
        else{
            PermissionAttachment newAttachement = player.addAttachment(this);

            NewGameOwnerEvent event = new NewGameOwnerEvent(_ownerUuid, player, newAttachement);
            Bukkit.getPluginManager().callEvent(event);

            _ownerPermissions = newAttachement;
            _ownerPermissions.setPermission("gamesetup.owner", true);
            _ownerUuid = player.getUniqueId();
            getLogger().info("Set Owner to %s (%s)".formatted(player.getName(), player.getUniqueId()));
        }
    }

    public void gameStarted(){
        if(_automatic && _inactivityGameKiller != null) _inactivityGameKiller.start();
    }

    public void gameEnded(){
        if(_automatic){
            if(_inactivityGameKiller != null) _inactivityGameKiller.stop();
            if(_autoRestarter != null) _autoRestarter.runTaskTimer(this, 0, 20);
        }
    }

    // region Commands
    public void chooseNewOwner(Player player){
        if(player.hasPermission("gamesetup.owner")) new PlayerSelectGUI().open(player);
        else player.sendMessage("§c> Vous n'avez pas la permission de faire cette action.");
    }
    //endregion

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(isOwner(e.getPlayer().getUniqueId())){
            PermissionAttachment newAttachement = e.getPlayer().addAttachment(this);
            _ownerPermissions.getPermissions().forEach(newAttachement::setPermission);
            _ownerPermissions = newAttachement;
        }
        else if(_ownerUuid == null && _automatic) setOwner(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if(changeOwnerIfDisconnect && e.getPlayer().getUniqueId().equals(_ownerUuid)) setFirstPlayerOwner(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onNewOwner(NewGameOwnerEvent e){
        Bukkit.broadcastMessage("§3§l> §3Le nouveau maître du jeu est §n%s§3.".formatted(e.getNewOwner().getName()));
    }

    private void setFirstPlayerOwner(@Nullable UUID exclude){
        final Player newOwner = Bukkit.getOnlinePlayers().stream().filter(p -> !p.getUniqueId().equals(exclude)).findFirst().orElse(null);
        setOwner(newOwner);
    }
}
