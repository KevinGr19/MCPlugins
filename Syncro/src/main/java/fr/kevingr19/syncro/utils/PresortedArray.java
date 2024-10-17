package fr.kevingr19.syncro.utils;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.IntStream;

public class PresortedArray<T> {

    private final T[] _array;
    public final int length;

    private final Map<String, Integer[]> _sorts;

    public PresortedArray(T[] array){
        _array = array;
        length = _array.length;

        _sorts = new HashMap<>();
        _sorts.put(null, IntStream.range(0, length).boxed().toArray(Integer[]::new));
    }

    public int getIndex(int i, @Nullable String sortKey){
        return _sorts.get(sortKey)[i];
    }

    public T get(int i){
        return _array[i];
    }
    public T get(int i, String sortKey){
        return get(getIndex(i, sortKey));
    }

    public void addSort(String key, Comparator<T> comparator){
        final Integer[] indexes = IntStream.range(0, length).boxed().toArray(Integer[]::new);
        Arrays.sort(indexes, (i1, i2) -> comparator.compare(_array[i1], _array[i2]));
        _sorts.put(key, indexes);
    }

    public Iterator<T> getSorted(@Nullable String sortKey){
        return new Iterator<>() {
            final Integer[] indexes = _sorts.get(sortKey);
            int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < indexes.length;
            }

            @Override
            public T next() {
                return _array[indexes[currentIndex++]];
            }
        };
    }

}
