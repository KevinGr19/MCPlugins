package fr.kevingr19.mclib.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class ScoreboardPool {

    private final Map<String, Scoreboard> _scoreboards = new HashMap<>();
    private final Set<BiConsumer<String, Scoreboard>> createNotifier = new HashSet<>();

    public Scoreboard create(String key){
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        _scoreboards.put(key, scoreboard);

        createNotifier.forEach(a -> a.accept(key, scoreboard));
        return scoreboard;
    }

    public void addExisting(String key, Scoreboard scoreboard){
        _scoreboards.put(key, scoreboard);
    }

    public boolean has(String key){
        return _scoreboards.containsKey(key);
    }
    public Scoreboard get(String key){
        return _scoreboards.get(key);
    }
    public Scoreboard remove(String key){
        return _scoreboards.remove(key);
    }

    public void clear(){
        _scoreboards.clear();
    }

    public void execute(BiConsumer<String, Scoreboard> action){
        _scoreboards.forEach(action);
    }

    public void registerCreate(BiConsumer<String, Scoreboard> action){
        createNotifier.add(action);
    }
    public void unregisterCreate(BiConsumer<String, Scoreboard> action) {
        createNotifier.remove(action);
    }
}
