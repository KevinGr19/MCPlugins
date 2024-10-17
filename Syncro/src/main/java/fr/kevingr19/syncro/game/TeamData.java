package fr.kevingr19.syncro.game;

import fr.kevingr19.mclib.item.ItemHandle;
import fr.kevingr19.mclib.teams.MCTeam;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TeamData extends Datable {

    private static final Random RANDOM = new Random();
    private static final float PITCH_RANGE = 0.2f;

    public final MCTeam team;

    public final double maxHealth;
    private double _health;
    public Player currentDeath;

    private Inventory _inventory;

    public TeamData(MCTeam team, int maxHealth, int inventorySize){
        this.team = team;
        this.maxHealth = maxHealth;
        _health = this.maxHealth;
        if(inventorySize > 0) _inventory = Bukkit.createInventory(null, inventorySize, "Inventaire d'équipe");
    }

    @Nullable public Inventory getInventory() {
        return _inventory;
    }

    public double getHealth(){
        return _health;
    }
    public double setHealth(double health){
        final double oldHealth = _health;
        _health = Math.min(Math.max(health, 0), maxHealth);
        return _health - oldHealth;
    }

    public double heal(double heal){
        return setHealth(getHealth() + heal);
    }
    public double damage(double damage){
        return -setHealth(getHealth() - damage);
    }

    public boolean isDead(){
        return getHealth() <= 0;
    }
    public void resetHealth(){
        setHealth(maxHealth);
    }

    public void syncronizeHealth(@Nullable Player exclude){
        team.getPlayers().forEach(uuid -> {
            final Player player = Bukkit.getPlayer(uuid);
            if(player == null || player.isDead() || player.equals(exclude)) return;

            setHealthToPlayer(player);
        });
    }

    public void setHealthToPlayer(Player player){
        final double oldHealth = player.getHealth();

        player.setMaxHealth(maxHealth);
        player.setHealth(getHealth());

        if(player.getHealth() < oldHealth){
            player.sendHurtAnimation(90);
            player.playSound(player, Sound.ENTITY_GENERIC_HURT, 1, RANDOM.nextFloat() * PITCH_RANGE + (1-PITCH_RANGE/2f));
        }
    }

    public ItemHandle getDataIcon(){
        return new ItemHandle(team.getIcon(), true)
                .setName("§f" + team.getColor() + "Équipe " + team.getName())
                .setLore(getDataText())
                .addHideFlags();
    }
}
