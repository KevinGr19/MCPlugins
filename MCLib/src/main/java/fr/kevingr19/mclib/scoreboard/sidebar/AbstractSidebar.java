package fr.kevingr19.mclib.scoreboard.sidebar;

import net.minecraft.util.Tuple;
import org.bukkit.ChatColor;

public abstract class AbstractSidebar {

    private static final String[] entriesPrefixes = { "§a", "§b", "§c", "§d", "§e", "§f", "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9" };

    abstract public void setLine(int index, String content);
    abstract public void setTitle(String title);

    protected Tuple<String, String> getSplitLine(String content){
        String prefix, suffix = "";

        if(content.length() <= 16) prefix = content;
        else{
            int length = content.charAt(15) == '§' ? 15 : 16;
            prefix = content.substring(0, length);

            final String lastColors = ChatColor.getLastColors(prefix);
            if(content.length() + lastColors.length() > length+16) throw new IllegalArgumentException("Line too long");

            suffix = lastColors + content.substring(length);
        }

        return new Tuple<>(prefix, suffix);
    }

    protected String getEntry(int index){
        return entriesPrefixes[index] + "§r";
    }

}
