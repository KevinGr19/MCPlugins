package fr.kevingr19.mclib.scoreboard.sidebar;

import net.minecraft.util.Tuple;
import org.bukkit.scoreboard.*;

public class Sidebar extends AbstractSidebar{

    private final Scoreboard _scoreboard;
    private final Objective _obj;

    public Sidebar(Scoreboard scoreboard, String objectiveKey, String title) {
        _scoreboard = scoreboard;
        _obj = _scoreboard.registerNewObjective(objectiveKey, Criteria.DUMMY, title);
        _obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    @Override
    public void setLine(int index, String content) {
        Tuple<String, String> split = getSplitLine(content);
        final String prefix = split.getA(), suffix = split.getB(), entry = getEntry(index);

        Team team = _scoreboard.getTeam(entry);
        if(team == null) team = _scoreboard.registerNewTeam(entry);

        team.setPrefix(prefix);
        team.setSuffix(suffix);
        if(!team.hasEntry(entry)) team.addEntry(entry);

        _obj.getScore(entry).setScore(index);
    }

    @Override
    public void setTitle(String title) {
        _obj.setDisplayName(title);
    }
}
