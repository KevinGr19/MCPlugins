package fr.kevingr19.syncro.gui;

import fr.kevingr19.mclib.gui.ClickGUI;
import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.item.ItemUtils;
import fr.kevingr19.syncro.Syncro;
import fr.kevingr19.syncro.SyncroSettings;
import fr.kevingr19.syncro.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SettingsGUI extends ClickGUI {

    public SettingsGUI() {
        super(Syncro.instance(), "Paramètres de jeu", false);
        final SyncroSettings settings = Syncro.instance().settings();

        ItemUtils.outlineInventory(inventory, 1, 1, 1, 1);

        addUpdateButton(10, () -> {
            final ItemHandle handle = new ItemHandle(Material.SPLASH_POTION)
                    .setName("§6Nombre de coeurs par équipe")
                    .setLore("§c" + settings.getHeartsPerTeam() + " coeurs§f par équipe")
                    .addHideFlags();

            ((PotionMeta) handle.meta()).setBasePotionType(PotionType.INSTANT_HEAL);
            return handle.build();
        }, (p, e) -> settings.moveHeartsPerTeam(e.isRightClick()));

        addUpdateButton(11, () ->
            new ItemHandle(settings.isShowHeartsBelowName() ? Material.NAME_TAG : Material.BARRIER)
                    .setName("§6Vie visible en dessous du pseudo")
                    .setLore(settings.isShowHeartsBelowName() ? "§aOui" : "§4Non")
                    .addHideFlags().build()
        , (p, e) -> settings.setShowHeartsBelowName(!settings.isShowHeartsBelowName()));

        addUpdateButton(12, () ->
            new ItemHandle(settings.isShowHeartsInTab() ? Material.SPYGLASS : Material.BARRIER)
                    .setName("§6Vie visible dans la liste des joueurs")
                    .setLore(settings.isShowHeartsInTab() ? "§aOui" : "§4Non")
                    .addHideFlags().build()
        , (p, e) -> settings.setShowHeartsInTab(!settings.isShowHeartsInTab()));

        addUpdateButton(13, () -> new ItemHandle(Material.COOKED_BEEF)
                .setName("§6Vitesse de la faim")
                .setLore("§fMultiplicateur : §a%.1f".formatted(settings.getFoodMultiplier()))
                .addHideFlags().build(),
            (p, e) -> settings.moveFoodMultiplier(e.isRightClick()));

        addUpdateButton(19, () -> {
            final int size = settings.getTeamInventorySize();
            return new ItemHandle(size > 0 ? Material.ENDER_CHEST : Material.BARRIER, Math.max(1, size))
                    .setName("§6Taille de l'inventaire d'équipe")
                    .setLore(size > 0 ? "§a" + size + " slots" : "§4Désactivé")
                    .addHideFlags().build();
        }, (p, e) -> settings.moveTeamInventorySize(e.isRightClick()));

        addButton(20, new ItemHandle(Material.CHEST)
                .setName("§6Inventaire de départ")
                .setLore("§7Cliquer pour modifier")
                .addHideFlags().build(), (p, e) -> Syncro.instance().editStartInventory(p));

        addUpdateButton(21, () -> {
            final Duration duration = Duration.ofMinutes(settings.getMaxTimeMinutes());
            return new ItemHandle(Material.CLOCK)
                    .setName("§6Temps de jeu maximum")
                    .setLore("§fMax : §c%02dh%02d".formatted(duration.toHours(), duration.toMinutesPart()))
                    .addHideFlags().build();
        }, (p, e) -> settings.moveMaxTimeMinutes(e.isRightClick()));

        addUpdateButton(22, () -> {
            final Duration duration = Duration.ofSeconds(settings.getInvincibilitySeconds());
            return new ItemHandle(duration.toSeconds() > 0 ? Material.TOTEM_OF_UNDYING : Material.BARRIER)
                .setName("§6Invincibilité de début de jeu")
                .setLore(duration.toSeconds() > 0 ? "§fTemps : §b%02dm%02d".formatted(duration.toMinutes(), duration.toSecondsPart()) : "§4Désactivée")
                .addHideFlags().build();
        }, (p, e) -> settings.moveInvincibilitySeconds(e.isRightClick()));

        addUpdateButton(29, () ->
            new ItemHandle(settings.isPvpEnabled() ? Material.DIAMOND_SWORD : Material.BARRIER)
                    .setName("§6PvP")
                    .setLore(settings.isPvpEnabled() ? "§aActivé" : "§4Désactivé")
                    .addHideFlags().build()
        , (p, e) -> settings.setPvpEnabled(!settings.isPvpEnabled()));

        addUpdateButton(30, () -> {
            final Duration duration = Duration.ofMinutes(settings.getNoPvPMinutes());
            return new ItemHandle(Material.FIRE_CHARGE)
                    .setName("§6Temps avant PvP")
                    .setLore(duration.toSeconds() > 0 ? "§fTemps : §b%02dm%02d".formatted(duration.toMinutes(), duration.toSecondsPart()) : "§fTemps : §bAucun")
                    .addHideFlags().build();

        }, (p, e) -> settings.moveNoPvPMinutes(e.isRightClick()));


        final ItemStack startButton = new ItemHandle(Material.TRIPWIRE_HOOK)
                .setName("§bDémarrer la partie")
                .setGlowingEnchant()
                .addHideFlags().build();
        addUpdateButton(25, () -> startButton, (p, e) -> Syncro.instance().startGame(p));
    }

    private void addUpdateButton(final int slot, final Supplier<ItemStack> itemGenerator, final BiConsumer<Player, InventoryClickEvent> action){
        addButton(slot, itemGenerator.get(), (p, e) -> {
            if(e != null) {
                if(!p.hasPermission("syncro.owner") || Syncro.instance().state() != GameState.LOBBY){
                    Close(p);
                    return;
                }
                action.accept(p, e);
            }
            inventory.setItem(slot, itemGenerator.get());
        });
        buttons.get(slot).accept(null, null);
    }

    @Override
    protected Inventory createInventory(String s) {
        return Bukkit.createInventory(null, 45, s);
    }
}
