package fr.kevingr19.syncro;

import fr.kevingr19.gamesetup.GameSetup;
import fr.kevingr19.gamesetup.NewGameOwnerEvent;
import fr.kevingr19.gamesetup.NoGameOwnerEvent;
import fr.kevingr19.mclib.display.BroadcastUtils;
import fr.kevingr19.mclib.scheduler.RunnableUtils;
import fr.kevingr19.mclib.teams.MCTeam;
import fr.kevingr19.mcteams.MCTeams;
import fr.kevingr19.syncro.commands.SyncroCommand;
import fr.kevingr19.syncro.game.*;
import fr.kevingr19.syncro.gui.PlayersStatsGUI;
import fr.kevingr19.syncro.gui.SettingsGUI;
import fr.kevingr19.syncro.gui.TeamStatsGUI;
import fr.kevingr19.syncro.gui.TeamsStatsGUI;
import fr.kevingr19.syncro.utils.PresortedArray;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class Syncro extends JavaPlugin implements Listener {

    private static Syncro _inst;
    public static Syncro instance(){
        return _inst;
    }

    private boolean _debug;
    public boolean isDebug(){
        return _debug;
    }

    private GameState _gameState;
    private LobbyHandler _lobbyHandler;
    private SyncroSettings _settings;
    private String _gameOwner;

    public GameState state(){
        return _gameState;
    }
    public SyncroSettings settings(){
        return _settings;
    }
    public String getGameOwnerName(){
        return _gameOwner;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        _inst = this;

        loadConfig();

        _gameState = GameState.NONE;
        _lobbyHandler = new LobbyHandler();
        _settings = new SyncroSettings();

        GameSetup.instance().setLobbyCommandExecutor(this::lobbyCommand);
        GameSetup.instance().setAutomatedLobbyExecutor(this::automatedLobby);

        buildRulesBook();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginCommand("syncro").setExecutor(new SyncroCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(state() != GameState.NONE){
            Bukkit.getOnlinePlayers().forEach(p -> p.setPlayerListHeaderFooter("", ""));
        }

        _lobbyHandler.stopLobby(true);
        if(_gameListener != null) _gameListener.stop();
        if(_endGameListener != null) _endGameListener.stop();
    }

    private void loadConfig(){
        saveDefaultConfig();
        final FileConfiguration config = getConfig();

        _debug = config.getBoolean("debug", false);
    }

    public boolean hasGameStarted(){
        return state() == GameState.GAME || state() == GameState.END;
    }

    private ItemStack _rulesBook;
    private void buildRulesBook(){
        _rulesBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) _rulesBook.getItemMeta();
        meta.setTitle("Syncro");
        meta.setAuthor("KevinGr19");

        try(InputStream in = getClass().getResourceAsStream("/rules.txt"))
        {
            byte[] data = in.readAllBytes();
            String text = new String(data, StandardCharsets.UTF_8).replaceAll("\r", "");
            String[] pages = text.split("--PAGE\\n");
            meta.setPages(pages);
        }
        catch (Exception e)
        {
            getLogger().severe("Could not read rules.txt : " + e.getMessage());
            e.printStackTrace();
        }

        _rulesBook.setItemMeta(meta);
    }

    //region Lobby instantiation
    private boolean lobbyCommand(Player player, Command command, String s, String[] strings){
        if(_gameState != GameState.NONE){
            player.sendMessage("§c> Le lobby a déjà été démarré.");
            return false;
        }

        final Location spawn = player.getWorld().getSpawnLocation();
        spawn.setY(180);

        if(strings.length > 0){
            if(strings.length < 3){
                player.sendMessage("§c> Entrez des coordonnées valides.");
                return false;
            }

            double x,y,z;
            try{
                x = Double.parseDouble(strings[0]);
                y = Double.parseDouble(strings[1]);
                z = Double.parseDouble(strings[2]);
            }
            catch (NumberFormatException e){
                player.sendMessage("§c> Entrez des coordonnées valides.");
                return false;
            }

            if(y < -64 || y > 250){
                player.sendMessage("§c> Le Y doit être compris entre -64 et 250");
                return false;
            }

            spawn.setX(x);
            spawn.setY(y);
            spawn.setZ(z);
        }

        startLobby(spawn);
        return true;
    }

    private void automatedLobby(){
        if(_gameState == GameState.NONE){
            final Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
            spawn.setY(180);
            startLobby(spawn);
        }
    }

    private void startLobby(Location spawn){
        _lobbyHandler.startLobby(spawn);
        _gameState = GameState.LOBBY;

        RunnableUtils.Repeat((i) -> {
            Bukkit.getOnlinePlayers().forEach(this::updatePlayerListText);
        }, 0, 60, this);
    }
    //endregion

    //region Game
    private GameListener _gameListener;
    private EndGameListener _endGameListener;

    private GameWinner _winner;
    private StatisticsManager _statisticsManager;

    public GameListener gameListener(){
        return _gameListener;
    }
    public EndGameListener endGameListener(){
        return _endGameListener;
    }
    public StatisticsManager stats() {
        return _statisticsManager;
    }
    public GameWinner winner(){
        return _winner;
    }

    public void startGame(Player player){
        // Checks
        if(state() != GameState.LOBBY){
            player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
            return;
        }

        if(!player.hasPermission("syncro.owner")){
            player.sendMessage("§c> Vous n'avez pas la permission de faire cette action");
            return;
        }

        final List<MCTeam> activeTeams = MCTeams.instance().getActiveTeams();
        if(activeTeams.size() < 2 && !isDebug()){
            player.sendMessage("§c> Il faut au moins 2 équipes pour lancer la partie.");
            return;
        }

        final Collection<String> missingPlayers = activeTeams.stream().flatMap(t ->
                        t.getPlayers().stream().map(Bukkit::getOfflinePlayer).filter(p -> !p.isOnline()))
                .map(OfflinePlayer::getName).toList();

        if(missingPlayers.size() > 0){
            player.sendMessage("§c> Des joueurs de certaines équipes ne sont pas en ligne :");
            player.sendMessage(String.join("\n", missingPlayers.stream().map(name -> "§c- "+ name).toList()));
            return;
        }

        // Start game
        _gameState = GameState.START;
        _settings.lock();
        MCTeams.instance().isLocked = true;

        _statisticsManager = new StatisticsManager();
        for(MCTeam team : activeTeams)
            _statisticsManager.addTeam(new TeamData(team, _settings.getHeartsPerTeam()*2, _settings.getTeamInventorySize()));

        _gameListener = new GameListener();
        _endGameListener = new EndGameListener();

        _endGameListener.start();
        _gameListener.start();

        _lobbyHandler.stopLobby(false);
        _statisticsManager.getTeams().forEach((k,t) -> t.syncronizeHealth(null));

        _endGameListener.registerDefaultSidebar();
        if(_settings.isShowHeartsBelowName()){
            MCTeams.instance().scoreboardPool().execute(this::setHealthVisibleBelowName);
            MCTeams.instance().scoreboardPool().registerCreate(this::setHealthVisibleBelowName);
        }
        if(_settings.isShowHeartsInTab()){
            MCTeams.instance().scoreboardPool().execute(this::setHealthVisiblePlayerList);
            MCTeams.instance().scoreboardPool().registerCreate(this::setHealthVisiblePlayerList);
        }

        _gameState = GameState.GAME;

        Bukkit.getWorlds().forEach(w -> w.setFullTime(3000));

        GameSetup.instance().gameStarted();
        GameSetup.instance().changeOwnerIfDisconnect = false;

        BroadcastUtils.title("§3§lSyncro 1.20", "§bKevinGr19", 10, 100, 40);
        BroadcastUtils.sound(Sound.ENTITY_WITHER_SPAWN, 0.8f, 0.5f);
        broadcast("Le jeu vient de commencer ! Bonne chance.");
    }

    public void endGame(@Nullable Player winner){
        if(state() != GameState.GAME) return;

        stats().sortEverything();
        _gameState = GameState.END;

        _lobbyHandler.stopLobby(true);
        _gameListener.stop();

        _winner = new GameWinner(winner);
        Bukkit.getOnlinePlayers().forEach(_endGameListener::handlePlayer);

        if(_winner.noTeam()){
            BroadcastUtils.title("§7Égalité", "§7§oPersonne n'a gagné.", 15, 160, 20);
            BroadcastUtils.sound(Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
            broadcast("Personne n'a gagné. §nEgalité !");
        }
        else{
            final String message = "L'équipe %s§r a remporté la partie (%s§r)".formatted(_winner.teamWinner.getFullName(), winner.getDisplayName());
            Bukkit.getOnlinePlayers().forEach(p -> {
                String title = _winner.isWinner(p) ? "§6§l>§r §e§lVictoire§r §6§l<" : "§7§l>§r §c§lDéfaite§r §7§l<";
                p.sendTitle(title, message, 20, 300, 40);
            });
            broadcast(message);
        }

        RunnableUtils.Delay(this::broadcastStatsResults, 250, this);
        RunnableUtils.Delay(GameSetup.instance()::gameEnded, 1800, this);
    }

    private void setHealthVisibleBelowName(String key, Scoreboard s){
        final Objective objName = s.registerNewObjective("health_below_name", Criteria.HEALTH, "§c♥");
        objName.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    private void setHealthVisiblePlayerList(String key, Scoreboard s){
        final Objective objName = s.registerNewObjective("health_player_list", Criteria.HEALTH, "§c♥");
        objName.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        objName.setRenderType(RenderType.INTEGER);
    }

    private void broadcastStatsResults(){

        final PlayerData mvp = stats().getBestPlayer((p1, p2) -> Double.compare(p2.balance(), p1.balance()), true);
        final PlayerData good = stats().getBestPlayer((p1, p2) -> Double.compare(p2.heals, p1.heals), false);
        final PlayerData bad = stats().getBestPlayer((p1, p2) -> Integer.compare(p2.kills, p1.kills), false);
        final PlayerData ugly = stats().getBestPlayer((p1, p2) -> Double.compare(p2.inflictedDamages, p1.inflictedDamages), false);

        StringBuilder message = new StringBuilder(
                """
                §b§lMENTIONS HONORABLES§7 (/syncro stats)
                §3-------------------------------
                §f- %s§r a.k.a "§6§lLe MVP§r" (%s§r)
                """.formatted(mvp.getFullName(), mvp.getBalanceText()));

        if(good != null || bad != null || ugly != null){
            message.append("§f\n");
            if(good != null) message.append("§f- %s§r a.k.a \"§aLe Bon§r\" (Soins : §a%.1f§r)\n".formatted(good.getFullName(), good.heals));
            if(bad != null) message.append("§f- %s§r a.k.a \"§4La Brute§r\" (Kills : §4%d§r)\n".formatted(bad.getFullName(), bad.kills));
            if(ugly != null) message.append("§f- %s§r a.k.a \"§cLe Truand§r\" (Dégâts infligés : §c%.1f§r)\n".formatted(ugly.getFullName(), ugly.inflictedDamages));
        }

        message.append("§3-------------------------------");
        broadcast(message.toString());
    }
    //endregion

    //region Commands
    public void settingsCommand(Player player){
        if(!player.hasPermission("syncro.owner")) player.sendMessage("§c> Vous n'avez pas la permission de faire cette action.");
        else if(_gameState != GameState.LOBBY) player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
        else new SettingsGUI().open(player);
    }

    public void teamInventoryCommand(Player player){
        if(!hasGameStarted()){
            player.sendMessage("§c> La partie n'a pas encore commencé.");
            return;
        }

        final TeamData teamData = stats().getTeam(player);
        if(teamData == null) player.sendMessage("§c> Vous n'êtes dans aucune équipe.");
        else if(teamData.getInventory() == null) player.sendMessage("§c> Les inventaires d'équipe sont désactivés.");
        else player.openInventory(teamData.getInventory());
    }

    public void editStartInventory(Player player){
        if(!player.hasPermission("syncro.owner")) player.sendMessage("§c> Vous n'avez pas la permission de faire cette action.");
        else if(_gameState != GameState.LOBBY) player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
        else player.openInventory(_settings.getStartingInventory());
    }

    public void openRules(Player player){
        player.openBook(_rulesBook);
    }

    public void openTeamStats(Player player){
        final MCTeam team = MCTeams.instance().teamManager().getPlayerTeam(player.getUniqueId());

        if(team == null) player.sendMessage("§c> Vous n'êtes dans aucune équipe.");
        else openTeamStats(player, team.id);
    }

    public void openTeamStats(Player player, String teamId){
        if(!hasGameStarted()){
            player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
            return;
        }

        if(!stats().hasTeam(teamId)){
            player.sendMessage("§c> Cette équipe n'existe pas.");
            return;
        }

        final TeamData teamData = stats().getTeam(teamId);
        if(state() != GameState.END && !teamData.team.getPlayers().contains(player.getUniqueId())){
            player.sendMessage("§c> Vous ne pouvez pas faire cette action pour le moment.");
            return;
        }

        List<PlayerData> datas = new ArrayList<>(teamData.team.getPlayerCount());
        teamData.team.getPlayers().forEach(uuid -> {
            if(stats().has(uuid)) datas.add(stats().get(uuid));
        });

        new TeamStatsGUI(teamData, datas).open(player);
    }

    public void openTeamsStats(Player player){
        if(state() != GameState.END){
            player.sendMessage("§c> Vous devez attendre la fin de la partie pour consulter les statistiques des autres équipes.");
            return;
        }

        new TeamsStatsGUI().open(player);
    }

    public void openPlayersStats(Player player){
        if(state() != GameState.END){
            player.sendMessage("§c> Vous devez attendre la fin de la partie pour consulter les statistiques des autres joueurs.");
            return;
        }

        new PlayersStatsGUI().open(player);
    }
    //endregion

    //region Events
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e){
        e.setJoinMessage(getJoinQuitMessage("§a+", e.getPlayer(), Bukkit.getOnlinePlayers().size()));
        if(state() != GameState.NONE) updatePlayerListText(e.getPlayer());
    }

    private void updatePlayerListText(Player player){
        player.setPlayerListHeaderFooter(
                """
                                                         §r
                    §3§lSyncro§r    §r
                    §7§oPar KevinGr19    §r
                """,
                """
                
                    §7Maître du jeu : §r%s    §r
                    §7Ping : §a%dms§r    §r
                """.formatted(_gameOwner, player.getPing()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        e.setQuitMessage(getJoinQuitMessage("§c-", e.getPlayer(), Bukkit.getOnlinePlayers().size()-1));
    }

    private String getJoinQuitMessage(String symbol, Player player, int count){
        return "[%s§r] %s§r (§e%s/%s§r)".formatted(symbol, player.getDisplayName(), count, Bukkit.getMaxPlayers());
    }

    @EventHandler
    public void onNewOwner(NewGameOwnerEvent e){
        e.getPermissionAttachment().setPermission("mcteams.admin", true);
        e.getPermissionAttachment().setPermission("syncro.owner", true);
        _gameOwner = e.getNewOwner().getName();
    }

    @EventHandler
    public void onNoOwner(NoGameOwnerEvent e){
        _gameOwner = "§7Aucun";
    }
    //endregion

    public static void broadcast(String msg){
        Bukkit.broadcastMessage("§3[Syncro] §l>§r %s".formatted(msg));
    }
}
