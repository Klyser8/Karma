package com.github.klyser8.karma.api.events;

import com.github.klyser8.karma.karma.KarmaSource;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KarmaLossEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private double amount;
    private final double oldKarma;
    private final double newKarma;
    private final KarmaSource source;

    public KarmaLossEvent(Player player, double amount, double oldKarma, KarmaSource source) {
        this.player = player;
        this.amount = amount;
        this.oldKarma = oldKarma;
        this.newKarma = oldKarma - amount;
        this.source = source;
    }


    /**
     * Returns the player who's Karma has increased.
     *
     * @return the affected player
     */
    public Player getPlayer() {
        return player;
    }


    /**
     * Returns the amount of Karma which has been lost.
     *
     * @return amount of Karma gained
     */
    public double getAmount() {
        return amount;
    }


    /**
     * Sets the amount of karma lost.
     *
     * @param amount any positive double.
     */
    public void setAmount(Double amount) {
        if (amount >= 0)
            this.amount = amount;
        else
            Bukkit.getServer().getLogger().severe("[KarmaGainEvent] - 'amount' variable only accepts positive doubles!");
    }


    /**
     * Returns the player's total Karma before the event.
     *
     * @return player's Karma Score before the event.
     */
    public double getOldKarma() {
        return oldKarma;
    }


    /**
     * Returns the player's total Karma after the event.
     *
     * @return player's Karma after the event.
     */
    public double getNewKarma() {
        return newKarma;
    }


    /**
     * Returns the source the Karma was removed from. Check the Enum 'KarmaSource' for all the possibilities.
     *
     * @return source of the Karma.
     */
    public KarmaSource getSource() {
        return source;
    }



    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
