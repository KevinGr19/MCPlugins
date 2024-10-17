package fr.kevingr19.mclib.item;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class ItemHandle {

    private ItemStack _item;
    private ItemMeta _meta;
    private CompoundTag _tag;

    public ItemHandle(Material type){ this(type, 1); }
    public ItemHandle(Material type, int amount){
        _item = new ItemStack(type, amount);
        _meta = _item.getItemMeta();
        _tag = new CompoundTag();
    }

    public ItemHandle(ItemStack item){
        this(item, false);
    }
    public ItemHandle(ItemStack item, boolean clone){
        if(item == null) _tag = new CompoundTag();
        else{
            _item = clone ? item.clone() : item;
            _meta = _item.getItemMeta();

            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(_item);
            _tag = nmsItem.hasTag() ? nmsItem.getTag() : new CompoundTag();
        }
    }

    public ItemStack item() { return _item; }
    public ItemMeta meta() { return _meta; }
    public CompoundTag tag() { return _tag; }

    // BUILDER

    public ItemHandle setName(String name) { _meta.setDisplayName(name); return this; }
    public ItemHandle setLore(List<String> lore) { _meta.setLore(lore); return this; }
    public ItemHandle setLore(String... lore) { return setLore(List.of(lore)); }
    public ItemHandle addFlags(ItemFlag... flags) { _meta.addItemFlags(flags); return this; }
    public ItemHandle addEnchant(Enchantment enchant, int level, boolean vanilla){
        _meta.addEnchant(enchant, level, !vanilla);
        return this;
    }

    public ItemHandle setGlowingEnchant() { return addEnchant(Enchantment.DURABILITY, 1, false); }

    public ItemHandle addHideFlags() { return addFlags(
            ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ENCHANTS,
            ItemFlag.HIDE_POTION_EFFECTS,
            ItemFlag.HIDE_PLACED_ON,
            ItemFlag.HIDE_UNBREAKABLE,
            ItemFlag.HIDE_DESTROYS,
            ItemFlag.HIDE_ARMOR_TRIM
    ); }

    public ItemHandle setCustomModelData(int data){ _meta.setCustomModelData(data); return this; }

    public ItemHandle addBoolean(String key, boolean value) { _tag.putBoolean(key, value); return this; }
    public ItemHandle addString(String key, String value) { _tag.putString(key, value); return this; }
    public ItemHandle addInt(String key, int value) { _tag.putInt(key, value); return this; }
    public ItemHandle addDouble(String key, double value) { _tag.putDouble(key, value); return this; }
    public ItemHandle addUUID(String key, UUID uuid) { _tag.putUUID(key, uuid); return this; }

    public ItemStack build(){
        _item.setItemMeta(Bukkit.getItemFactory().asMetaFor(_meta, _item));
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(_item);

        if(nmsItem.hasTag()) nmsItem.setTag(_tag.merge(nmsItem.getTag()));
        return CraftItemStack.asCraftMirror(nmsItem);
    }

}