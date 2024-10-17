package fr.kevingr19.syncro;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class SyncroSettings {

    private int heartsPerTeam;
    private double foodMultiplier;
    private int teamInventorySize;
    private int maxTimeMinutes;
    private int invincibilitySeconds;
    private int noPvPMinutes;
    private boolean pvpEnabled;
    private boolean showHeartsBelowName;
    private boolean showHeartsInTab;
    private final Inventory startingInventory;

    private boolean isLocked;

    public SyncroSettings(){
        isLocked = false;

        setHeartsPerTeam(20);
        setFoodMultiplier(0.8);
        setTeamInventorySize(18);
        setMaxTimeMinutes(210);
        setInvincibilitySeconds(300);
        setNoPvPMinutes(20);

        pvpEnabled = true;
        showHeartsBelowName = true;
        showHeartsInTab = true;

        startingInventory = Bukkit.createInventory(null, 36, "Inventaire de départ");
    }

    public int getHeartsPerTeam(){
        return heartsPerTeam;
    }
    public double getFoodMultiplier(){
        return foodMultiplier;
    }
    public int getMaxTimeMinutes() {
        return maxTimeMinutes;
    }
    public int getTeamInventorySize() {
        return teamInventorySize;
    }
    public int getInvincibilitySeconds() {
        return invincibilitySeconds;
    }
    public int getNoPvPMinutes() {
        return noPvPMinutes;
    }
    public boolean isShowHeartsBelowName() {
        return showHeartsBelowName;
    }
    public boolean isShowHeartsInTab() {
        return showHeartsInTab;
    }
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public Inventory getStartingInventory() {
        return startingInventory;
    }

    public void setHeartsPerTeam(int heartsPerTeam) {
        if(!isLocked) this.heartsPerTeam = Math.min(Math.max(heartsPerTeam, 10), 40);
    }
    public void setFoodMultiplier(double foodMultiplier) {
        if(!isLocked) this.foodMultiplier = Math.min(Math.max(foodMultiplier, 0.5), 1);
    }
    public void setTeamInventorySize(int teamInventorySize) {
        if(!isLocked) this.teamInventorySize = Math.min(Math.max(teamInventorySize/9, 0), 6) * 9;
    }
    public void setMaxTimeMinutes(int maxTimeMinutes) {
        if(!isLocked) this.maxTimeMinutes = Math.min(Math.max(maxTimeMinutes, 60), 360);
    }
    public void setInvincibilitySeconds(int invincibilitySeconds) {
        if(!isLocked) this.invincibilitySeconds = Math.min(Math.max(invincibilitySeconds, 0), 900);
    }
    public void setNoPvPMinutes(int noPvPMinutes){
        if(!isLocked) this.noPvPMinutes = Math.min(Math.max(noPvPMinutes, 0), 60);
    }
    public void setPvpEnabled(boolean pvpEnabled) {
        if(!isLocked) this.pvpEnabled = pvpEnabled;
    }
    public void setShowHeartsBelowName(boolean showHeartsBelowName) {
        if(!isLocked) this.showHeartsBelowName = showHeartsBelowName;
    }
    public void setShowHeartsInTab(boolean showHeartsInTab) {
        if(!isLocked) this.showHeartsInTab = showHeartsInTab;
    }

    public void moveHeartsPerTeam(boolean inverse){
        setHeartsPerTeam(heartsPerTeam + 2 * coef(inverse));
    }
    public void moveFoodMultiplier(boolean inverse){
        setFoodMultiplier(foodMultiplier - 0.1 * coef(inverse));
    }
    public void moveTeamInventorySize(boolean inverse){
        setTeamInventorySize(teamInventorySize + 9 * coef(inverse));
    }
    public void moveMaxTimeMinutes(boolean inverse){
        setMaxTimeMinutes(maxTimeMinutes + 30 * coef(inverse));
    }
    public void moveInvincibilitySeconds(boolean inverse){
        setInvincibilitySeconds(invincibilitySeconds + 60 * coef(inverse));
    }
    public void moveNoPvPMinutes(boolean inverse){
        setNoPvPMinutes(getNoPvPMinutes() + 5 * coef(inverse));
    }

    public void lock(){
        isLocked = true;
    }
    public void unlock(){
        isLocked = false;
    }

    private int coef(boolean inverse){
        return inverse ? -1 : 1;
    }
}
