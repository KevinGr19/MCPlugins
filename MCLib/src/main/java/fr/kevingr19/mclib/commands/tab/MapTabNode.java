package fr.kevingr19.mclib.commands.tab;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link TabNode} that generates different node routes based on a Map.
 */

public class MapTabNode extends TabNode implements TabNode.MappedTabEnd {

    protected final Map<String, TabNode> nodes = new HashMap<>();

    public MapTabNode(String name, TabNode... nodes) {
        super(name);
        addNext(nodes);
    }

    @Override
    public List<String> getPossibleWords() {
        return nodes.keySet().stream().toList();
    }

    @Override
    public TabNode getNext(String name) {
        return nodes.getOrDefault(name, null);
    }

    @Override
    public void addNext(TabNode node) {
        nodes.put(node.name, node);
    }

    @Override
    public void addNext(TabNode... nodes) {
        for(TabNode node : nodes) addNext(node);
    }

    public void addEmptyNext(String name){
        nodes.put(name, null);
    }

    public void addEmptyNext(String... names){
        for(String name : names) addEmptyNext(name);
    }
}
