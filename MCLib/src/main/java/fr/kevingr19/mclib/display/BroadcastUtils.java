package fr.kevingr19.mclib.display;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class BroadcastUtils {

    public static void execute(Consumer<Player> action){
        for(Player player : Bukkit.getOnlinePlayers()) action.accept(player);
    }

    public static void title(String title, String subtitle, int fadeIn, int stay, int fadeOut){
        execute(p -> p.sendTitle(title, subtitle, fadeIn, stay, fadeOut));
    }

    public static void actionBar(String message){
        final TextComponent msg = new TextComponent(message);
        execute(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg));
    }

    public static void sound(Sound sound, float volume, float pitch){
        execute(p -> p.playSound(p, sound, volume, pitch));
    }

    public static void sound(String s, float volume, float pitch){
        execute(p -> p.playSound(p, s, volume, pitch));
    }

}
