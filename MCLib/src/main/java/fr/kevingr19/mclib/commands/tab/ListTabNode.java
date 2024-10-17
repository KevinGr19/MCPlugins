package fr.kevingr19.mclib.commands.tab;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link TabNode} that gives a list of fixed possible words.
 */

public class ListTabNode extends TabNode implements TabNode.SingleTabEnd {

    protected final List<String> words;
    protected TabNode next;

    public ListTabNode(String name, TabNode next, String... words) {
        super(name);
        this.next = next;
        this.words = new ArrayList<>();
        addWords(words);
    }

    public ListTabNode(String name, TabNode next, List<String> words) {
        super(name);
        this.next = next;
        this.words = words;
    }

    public void addWord(String word){
        words.add(word);
    }

    public void addWords(String... words){
        this.words.addAll(List.of(words));
    }

    @Override
    public List<String> getPossibleWords() {
        return words;
    }

    @Override
    public TabNode getNext() {
        return next;
    }

    @Override
    public void setNext(TabNode node) {
        this.next = node;
    }
}