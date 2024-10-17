package fr.kevingr19.mclib.display;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.*;

public class FloatingTextCollection {

    public enum AlignMode{
        TOP,
        CENTER,
        BOTTOM
    }

    private Location _location;
    private AlignMode _alignement;
    private double _spacing;

    private final SortedMap<Integer, Entity> _entities;

    public FloatingTextCollection(Location location, AlignMode alignment, double spacing){
        _location = location.add(0, -2.2, 0);
        _alignement = alignment;
        _spacing = spacing;

        _entities = new TreeMap<>();
    }

    public void setLocation(@Nonnull Location location){
        _location = location;
        updatePositions();
    }
    public Location getLocation(){
        return _location;
    }

    public void setAlignment(AlignMode alignment){
        _alignement = alignment;
        updatePositions();
    }
    public AlignMode getAlignment() {
        return _alignement;
    }

    public void setSpacing(double spacing){
        _spacing = spacing;
        updatePositions();
    }
    public double getSpacing(){
        return _spacing;
    }

    private void addOrUpdateLine(int index, String text){
        if(_entities.containsKey(index)){
            if(!_entities.get(index).isDead()) _entities.get(index).setCustomName(text);
            return;
        }

        final ArmorStand stand = (ArmorStand) _location.getWorld().spawnEntity(_location, EntityType.ARMOR_STAND);
        stand.setInvulnerable(true);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setCollidable(false);
        stand.setPersistent(true);
        stand.setSilent(true);
        stand.setCustomNameVisible(true);

        stand.setCustomName(text);
        _entities.put(index, stand);
    }

    public void setLine(int index, String text){
        addOrUpdateLine(index, text);
        updatePositions();
    }

    public void setLines(Map<Integer, String> lines){
        for(final int index : lines.keySet()) addOrUpdateLine(index, lines.get(index));
        updatePositions();
    }

    private boolean deleteLine(int index){
        if(!_entities.containsKey(index)) return false;
        _entities.get(index).remove();
        _entities.remove(index);
        return true;
    }

    public void removeLine(int index){
        if(deleteLine(index)) updatePositions();
    }

    public void removeLines(Collection<Integer> indexes){
        boolean update = false;
        for(final int index : indexes) update |= deleteLine(index);

        if(update) updatePositions();
    }

    public void updatePositions(){
        if(_entities.size() == 0) return;

        double y = _location.getY();
        if(_alignement == AlignMode.BOTTOM) y += (_entities.size()-1) * _spacing;
        else if(_alignement == AlignMode.CENTER) y += (_entities.size()-1) * _spacing * .5;

        Location pos = _location.clone();
        final int first = _entities.firstKey();

        for(final int line : _entities.keySet()){
            if(_entities.get(line).isDead()) continue;

            pos.setY(y - _spacing * (line-first));
            _entities.get(line).teleport(pos);
        }
    }

    public void clearDeadLines(){
        List<Integer> deadIndexes = _entities.keySet().stream().filter(k -> _entities.get(k).isDead()).toList();
        removeLines(deadIndexes);
    }

    public void clear(){
        removeLines(_entities.keySet().stream().toList());
    }

}
