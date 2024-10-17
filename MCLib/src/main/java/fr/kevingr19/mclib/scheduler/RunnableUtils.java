package fr.kevingr19.mclib.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;
import java.util.function.Function;

public class RunnableUtils {

    public static void Delay(Runnable action, int ticks, Plugin plugin){
        new BukkitRunnable(){
            @Override public void run() { action.run(); }
        }.runTaskLater(plugin, ticks);
    }

    public static void Repeat(Function<Integer, Boolean> action, int delayTicks, int periodTicks, Plugin plugin){
        new BukkitRunnable(){
            private int _tick = 0;
            @Override public void run() { if(action.apply(_tick++)) cancel(); }
        }.runTaskTimer(plugin, delayTicks, periodTicks);
    }

    public static void Repeat(Function<Integer, Boolean> action, int delayTicks, int periodTicks, int iterations, Plugin plugin){
        new BukkitRunnable(){
            private int _tick = 0;
            @Override public void run() {
                if(action.apply(_tick++) || _tick >= iterations) cancel();
            }
        }.runTaskTimer(plugin, delayTicks, periodTicks);
    }

    public static void Repeat(Consumer<Integer> action, int delayTicks, int periodTicks, Plugin plugin) {
        Repeat((i) -> {
            action.accept(i);
            return false;
        }, delayTicks, periodTicks, plugin);
    }

    public static void Repeat(Consumer<Integer> action, int delayTicks, int periodTicks, int iterations, Plugin plugin) {
        Repeat((i) -> {
            action.accept(i);
            return false;
        }, delayTicks, periodTicks, iterations, plugin);
    }
}
