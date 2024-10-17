package fr.kevingr19.syncro.game;

import fr.kevingr19.mclib.teams.MCTeam;
import fr.kevingr19.mcteams.MCTeams;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class GameWinner{

    public final Player winner;
    public final MCTeam teamWinner;

    public GameWinner(@Nullable Player winner){
        this.winner = winner;
        teamWinner = winner != null ? MCTeams.instance().teamManager().getPlayerTeam(winner.getUniqueId()) : null;
    }

    public boolean isWinner(Player player){
        return !noTeam() && teamWinner.getPlayers().contains(player.getUniqueId());
    }

    public boolean noTeam(){
        return teamWinner == null;
    }

}
