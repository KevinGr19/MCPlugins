package fr.kevingr19.mclib.commands.tab;

import java.util.List;

/**
 * Base class of all tab nodes.
 * Provides {@link SingleTabEnd} that guarantees a single next node at most,
 * and {@link MappedTabEnd} that returns a tab node based on its name.
 * NOTE : This class is not preventing any circular route.
 */

abstract public class TabNode {

    protected final String name;

    public TabNode(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract public List<String> getPossibleWords();

    public interface SingleTabEnd{
        TabNode getNext();
        void setNext(TabNode node);
    }
    public interface MappedTabEnd{
        TabNode getNext(String name);
        void addNext(TabNode node);
        void addNext(TabNode... nodes);
    }

}
