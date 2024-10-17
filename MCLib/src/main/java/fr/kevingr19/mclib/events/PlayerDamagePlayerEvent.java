package fr.kevingr19.mclib.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamagePlayerEvent extends Event implements Cancellable {

    private final Player _target, _damager;
    private final EntityDamageEvent _event;

    public PlayerDamagePlayerEvent(Player target, Player damager, EntityDamageEvent e){
        _damager = damager;
        _target = target;
        _event = e;
    }

    public Player getDamager() {
        return _damager;
    }
    public Player getTarget() {
        return _target;
    }
    public EntityDamageEvent getSourceEvent() {
        return _event;
    }

    private static HandlerList HANDLER_LIST = new HandlerList();
    public static HandlerList getHandlerList(){
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    private boolean _isCancelled;
    @Override public boolean isCancelled() {
        return _isCancelled || _event.isCancelled();
    }
    @Override public void setCancelled(boolean b) {
        _isCancelled = b;
    }
}
