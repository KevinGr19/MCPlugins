package fr.kevingr19.mclib;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class MCLib {

    /*
    private static JavaPlugin plugin;

    public static void setPlugin(JavaPlugin plugin){
        MCLib.plugin = plugin;
    }
    public static JavaPlugin getPlugin(){
        return MCLib.plugin;
    }
    */

    public static void registerListener(Listener listener, Plugin plugin){
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}
