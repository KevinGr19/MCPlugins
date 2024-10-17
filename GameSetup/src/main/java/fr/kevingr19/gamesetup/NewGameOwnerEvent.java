package fr.kevingr19.gamesetup;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionAttachment;

import java.util.UUID;

public class NewGameOwnerEvent extends Event {

    private final UUID _oldOwner;
    private final Player _newOwner;
    private final PermissionAttachment _permissionAttachment;

    public NewGameOwnerEvent(UUID oldOwner, Player newOwner, PermissionAttachment permissionAttachment){
        _oldOwner = oldOwner;
        _newOwner = newOwner;
        _permissionAttachment = permissionAttachment;
    }

    public UUID getOldOwner() {
        return _oldOwner;
    }
    public Player getNewOwner() {
        return _newOwner;
    }
    public PermissionAttachment getPermissionAttachment(){
        return _permissionAttachment;
    }

    private static HandlerList HANDLERS = new HandlerList();
    public static HandlerList getHandlerList(){ return HANDLERS; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
}
