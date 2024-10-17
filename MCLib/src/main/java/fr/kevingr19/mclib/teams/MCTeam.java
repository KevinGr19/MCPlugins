package fr.kevingr19.mclib.teams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;

public class MCTeam {

    public final String id;
    private final TeamManager _manager;

    private boolean _enabled = true, _locked = false;
    private int _maxPlayers;

    private String _prefix = "", _suffix = "", _name = "";
    private ChatColor _color = ChatColor.RESET;
    private ItemStack _icon;

    private final Set<UUID> _players = new HashSet<>();

    MCTeam(TeamManager manager, String id){
        _manager = manager;
        this.id = id;
    }

    public boolean isEnabled(){
        return _enabled;
    }
    public void enable(){
        if(isLocked()) throw new RuntimeException("Team is locked");
        if(isEnabled()) return;

        _enabled = true;
    }
    public void disable(){
        if(isLocked()) throw new RuntimeException("Team is locked");
        if(!isEnabled()) return;

        _enabled = false;
        clearPlayers();
    }

    public boolean isLocked(){
        return _locked;
    }
    void setLocked(boolean locked){
        _locked = locked;
    }

    public int getMaxPlayers(){
        return _maxPlayers;
    }
    public void setMaxPlayers(int maxPlayers){
        if(isLocked()) throw new RuntimeException("Team is locked");
        _maxPlayers = maxPlayers;
        if(!isUnlimited() && _maxPlayers < _players.size()) clearPlayers();
    }
    public boolean isUnlimited(){
        return _maxPlayers <= 0;
    }
    public boolean isFull(){
        return !isUnlimited() && _players.size() >= _maxPlayers;
    }

    public int getPlayerCount(){
        return _players.size();
    }
    public Set<UUID> getPlayers(){
        return Collections.unmodifiableSet(_players);
    }
    void addPlayer(UUID uuid){
        if(isEnabled() && !isFull()) _players.add(uuid);
    }
    void removePlayer(UUID uuid){
        _players.remove(uuid);
    }

    public void clearPlayers(){
        if(isLocked()) throw new RuntimeException("Team is locked");
        for(UUID uuid : _players.stream().toList()) _manager.setPlayerTeam(uuid, null);
    }

    public String getName(){
        return _name;
    }
    public void setName(String name){
        if(isLocked()) throw new RuntimeException("Team is locked");
        _name = name;
    }

    public String getPrefix(){
        return _prefix;
    }
    public void setPrefix(String prefix){
        if(isLocked()) throw new RuntimeException("Team is locked");
        _prefix = prefix;
        _manager.updateTeamData(this, true, false, false);
    }

    public String getSuffix(){
        return _suffix;
    }
    public void setSuffix(String suffix){
        if(isLocked()) throw new RuntimeException("Team is locked");
        _suffix = suffix;
        _manager.updateTeamData(this, false, true, false);
    }

    public ItemStack getIcon(){
        return _icon;
    }
    public void setIcon(ItemStack icon){
        if(isLocked()) throw new RuntimeException("Team is locked");
        _icon = icon;
    }

    public ChatColor getColor(){
        return _color;
    }
    public void setColor(ChatColor color){
        if(isLocked()) throw new RuntimeException("Team is locked");
        _color = color;
        _manager.updateTeamData(this, false, false, true);
    }

    public String getFullName(){
        return _color + _name;
    }

    public String getPlayerName(String name){
        return _color + _prefix + name + _suffix;
    }

    public void sendTeamMessage(String message){
        executeOnline(p -> p.sendMessage(message));
    }

    public void executeOnline(Consumer<Player> action){
        _players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(action);
    }

}
