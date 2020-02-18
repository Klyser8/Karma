package me.klyser8.karma.events;

import me.klyser8.karma.enums.KarmaAlignment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KarmaAlignmentChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final KarmaAlignment oldAlignment;
    private final KarmaAlignment newAlignment;

    public KarmaAlignmentChangeEvent(Player player, KarmaAlignment oldAlignment, KarmaAlignment newAlignment) {
        this.player = player;
        this.oldAlignment = oldAlignment;
        this.newAlignment = newAlignment;
    }

    public Player getPlayer() {
        return player;
    }

    public KarmaAlignment getOldAlignment() {
        return oldAlignment;
    }

    public KarmaAlignment getNewAlignment() {
        return newAlignment;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
