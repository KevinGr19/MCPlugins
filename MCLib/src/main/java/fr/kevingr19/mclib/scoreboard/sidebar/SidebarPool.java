package fr.kevingr19.mclib.scoreboard.sidebar;

import fr.kevingr19.mclib.scoreboard.ScoreboardPool;
import net.minecraft.util.Tuple;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SidebarPool extends AbstractSidebar {

    private final ScoreboardPool _pool;
    private final String _objectiveKey;

    private String _title = "";
    private final Map<Integer, String[]> _lines = new HashMap<>();

    public SidebarPool(ScoreboardPool pool, String objectiveKey, String title){
        _pool = pool;
        _objectiveKey = objectiveKey;
        _title = title;

        _pool.execute(this::updateNewScoreboard);
        _pool.registerCreate(this::updateNewScoreboard);
    }

    public void execute(Consumer<Objective> action){
        _pool.execute((k, s) -> action.accept(s.getObjective(_objectiveKey)));
    }

    @Override
    public void setTitle(String title){
        _title = title;
        execute(o -> o.setDisplayName(title));
    }

    @Override
    public void setLine(int index, String content){
        if(index < 0 || index > 15) throw new IllegalArgumentException("Line index should be between 0 and 15");
        Tuple<String, String> split = getSplitLine(content);

        // Displaying content
        final String _prefix = split.getA(), _suffix = split.getB();
        final String entry = getEntry(index);

        _pool.execute((k, s) -> {
            final Objective obj = s.getObjective(_objectiveKey);
            if(obj == null) return;

            Team team = s.getTeam(entry);
            if(team == null) team = s.registerNewTeam(entry);

            team.setPrefix(_prefix);
            team.setSuffix(_suffix);

            if(!team.hasEntry(entry)) team.addEntry(entry);
            obj.getScore(entry).setScore(index);
        });

        // Saving content
        _lines.put(index, new String[]{_prefix, _suffix});
    }

    private void updateNewScoreboard(String key, Scoreboard s){
        final Objective obj = s.registerNewObjective(_objectiveKey, Criteria.DUMMY, _title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for(final int line : _lines.keySet()){
            final String entry = getEntry(line);
            final String[] content = _lines.get(line);
            final Team team = s.registerNewTeam(entry);
            team.setPrefix(content[0]);
            team.setSuffix(content[1]);

            team.addEntry(entry);
            obj.getScore(entry).setScore(line);
        }
    }

}
