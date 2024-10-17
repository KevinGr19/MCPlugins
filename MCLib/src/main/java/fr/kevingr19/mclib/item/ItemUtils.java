package fr.kevingr19.mclib.item;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    public static final ItemStack EMPTY_SLOT = new ItemHandle(Material.BLACK_STAINED_GLASS_PANE)
            .setName(" ").addHideFlags().build();

    public static void outlineInventory(Inventory inventory, int top, int bottom, int left, int right){
        for(int y = 0; y < top; y++)
            for(int i = 0; i < 9; i++) inventory.setItem(y*9+i, EMPTY_SLOT);

        for(int y = 0; y < bottom; y++)
            for(int i = 0; i < 9; i++) inventory.setItem(inventory.getSize()-(y+1)*9+i, EMPTY_SLOT);

        final int height = inventory.getSize() / 9;
        for(int x = 0; x < left; x++)
            for(int i = 0; i < height; i++) inventory.setItem(i*9+x, EMPTY_SLOT);

        for(int x = 0; x < right; x++)
            for(int i = 0; i < height; i++) inventory.setItem(i*9+8-x, EMPTY_SLOT);
    }

    public static boolean isNull(ItemStack i){
        return i == null || i.getType() == Material.AIR;
    }
}
