package fr.kevingr19.mclib.lobby;

import java.util.HashMap;
import java.util.Map;

public class LobbySettings {
    public boolean setTimeOnWait = false;
    public int setTimeOnWaitValue = 6000;
    public boolean freezeTime = true;
    public boolean spawnMobs = true;
    public boolean noWeather = false;

    private final Map<String, Object> _cache = new HashMap<>();
    public void cache(String key, Object cache){
        _cache.put(key, cache);
    }

    public Object getCached(String key){
        return _cache.get(key);
    }

    public boolean hasCached(String key){
        return _cache.containsKey(key);
    }
}
