package fr.kevingr19.mcteams;

public class MCTeamsSettings implements Cloneable{

    public static final int MAXPLAYERS_LIMIT = 50;

    private boolean isLocked = false;

    public void lock(){
        isLocked = true;
    }

    public boolean isLocked() {
        return isLocked;
    }

    private int maxPlayers = 0;
    private boolean teamPvP = true;
    private boolean playerTeamChoice = true;

    public int getMaxPlayers() { return maxPlayers; }
    public boolean isTeamPvP() { return teamPvP; }
    public boolean isPlayerTeamChoice() { return playerTeamChoice; }

    public void setMaxPlayers(int maxPlayers){
        if(!isLocked) this.maxPlayers = Math.min(Math.max(0, maxPlayers), MAXPLAYERS_LIMIT);
    }

    public void setTeamPvP(boolean value) { if(!isLocked) this.teamPvP = value; }
    public void setPlayerTeamChoice(boolean value) { if(!isLocked) this.playerTeamChoice = value; }

    @Override
    public MCTeamsSettings clone() {
        try {
            MCTeamsSettings clone = (MCTeamsSettings) super.clone();
            clone.maxPlayers = maxPlayers;
            clone.teamPvP = teamPvP;
            clone.playerTeamChoice = playerTeamChoice;
            clone.isLocked = false;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
