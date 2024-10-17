package fr.kevingr19.mcteams;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public record TeamData(String id, String name, ChatColor color, ItemStack icon, String prefix, String suffix) {
}
