package com.github.klyser8.karma.api;
import com.github.klyser8.karma.api.events.KarmaAlignmentChangeEvent;
import com.github.klyser8.karma.api.events.KarmaGainEvent;
import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.karma.KarmaAlignment;
import com.github.klyser8.karma.karma.KarmaSource;
import com.github.klyser8.karma.api.events.KarmaLossEvent;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import com.github.klyser8.karma.storage.PlayerKarma;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

import java.util.*;

import static com.github.klyser8.karma.api.util.UtilMethods.*;
import static com.github.klyser8.karma.api.util.GenericMethods.*;
import static me.mattstudios.mf.base.components.MfUtil.color;


public class KarmaManager {

    protected KarmaPlugin plugin;
    public KarmaManager() {
        plugin = KarmaPlugin.getPlugin(KarmaPlugin.class);
    }


    /**
     * Sets a player's karma score to the specified amount.
     *
     * @param player the chosen player.
     * @param amount number to set karma to.
     */
    public void setKarmaScore(Player player, double amount) {
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        if (amount > data.getKarmaScore()) {
            KarmaGainEvent event = new KarmaGainEvent(player, amount - data.getKarmaScore(), data.getKarmaScore(), KarmaSource.COMMAND);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                plugin.getStorageHandler().getPlayerData(player.getUniqueId()).setKarmaScore(amount, event.getSource());
        } else {
            KarmaLossEvent event = new KarmaLossEvent(player, data.getKarmaScore() - amount, data.getKarmaScore(), KarmaSource.COMMAND);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                plugin.getStorageHandler().getPlayerData(player.getUniqueId()).setKarmaScore(amount, event.getSource());
        }
    }

    /**Resets a player's karma to 0.
     *
     * @param player the chosen player
     */
    public void clearKarmaScore(Player player) {
        plugin.getStorageHandler().getPlayerData(player.getUniqueId()).setKarmaScore(0.0, KarmaSource.COMMAND);
    }


    /**
     * Adds or removes Karma from a player's total
     * depending on whether the written amount is
     * positive or negative. If the amount is positive,
     * there will be an increase in the player's karma,
     * if it is negative there will be a decrease.
     *
     * @param player chosen player.
     * @param amount amount to change
     * @param source source of the karma.
     */
    public void changeKarmaScore(Player player, double amount, KarmaSource source) {
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        if (amount < 0) {
            if (data.getKarmaScore() - amount <= KarmaPlugin.karmaLowLimit)
                data.setKarmaScore(KarmaPlugin.karmaLowLimit, source);
            else
                subtractKarmaScore(player, Math.abs(amount), source);
        } else {
            if (data.getKarmaScore() + amount >= KarmaPlugin.karmaHighLimit)
                data.setKarmaScore(KarmaPlugin.karmaHighLimit, source);
            else
                addKarmaScore(player, amount, source);
        }
        KarmaAlignment oldAlignment = data.getKarmaAlignment();
        if (oldAlignment == plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment()) return;
        KarmaAlignmentChangeEvent event = new KarmaAlignmentChangeEvent(player, oldAlignment, plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment());
        Bukkit.getPluginManager().callEvent(event);
    }


    /**
     * Updates a chosen player's Karma alignment.
     * This method should be executed any time a player's
     * Karma score is changed.
     *
     * @param player chosen player.
     */
    public void updateAlignments(KarmaPlugin plugin, Player player) {
        PlayerKarma data = Objects.requireNonNull(plugin.getStorageHandler().getPlayerData(player.getUniqueId()));
        KarmaAlignment oldAlignment = data.getKarmaAlignment();
        KarmaEnumFetcher fetcher = new KarmaEnumFetcher(plugin);
        for (KarmaAlignment alignment : KarmaAlignment.values()) {
            if (oldAlignment == alignment) continue;
            int lowBoundary = fetcher.getAlignmentLowBoundary(alignment);
            int highBoundary = fetcher.getAlignmentHighBoundary(alignment);
            if (!isBetween(data.getKarmaScore(), lowBoundary, highBoundary)) continue;
            List<String> commands = plugin.getAlignmentCommandsMap().get(alignment);
            for (String command : commands) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("<PLAYER>", player.getName()));
            }
            data.setKarmaAlignment(alignment, true);
            if (!plugin.isTablistAlignments()) continue;
            debugMessage(player.getName() + "'s alignment updated to: ", fetcher.getAlignmentName(alignment));
            if (plugin.isShowAlignments()) {
                player.setPlayerListName(color(fetcher.getAlignmentName(alignment) + "&r ") + player.getName());
            } else {
                CharSequence color = fetcher.getAlignmentName(alignment).subSequence(0, 2);
                player.setPlayerListName(color(color + player.getName()));
            }
            /*if (player.getPlayerListName().equalsIgnoreCase(player.getName())) {
                player.setPlayerListName(color(fetcher.getAlignmentName(alignment) + "&r ") + player.getName());
            } else {
                if (plugin.showAlignments) {
                    player.setPlayerListName(color(fetcher.getAlignmentName(alignment) + "&r ") + player.getDisplayName());
                } else {
                    CharSequence color = fetcher.getAlignmentName(alignment).subSequence(0, 2);
                    player.setPlayerListName(color(color + player.getDisplayName()));
                }
            }*/
        }
    }

    /**
     * Adds an amount to a player's Karma score. Does not
     * accept negative numbers.
     *
     * @param player chosen player.
     * @param amount amount to add.
     * @param source source of the karma.
     */
    protected void addKarmaScore(Player player, double amount, KarmaSource source) {
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        KarmaGainEvent event = new KarmaGainEvent(player, amount, data.getKarmaScore(), source);
        Bukkit.getPluginManager().callEvent(event);
        if (amount <= 0)
            event.setCancelled(true);
        if (plugin.getKarmaLimitList().contains(player) && source != KarmaSource.COMMAND && source != KarmaSource.VOTING)
            event.setCancelled(true);
        if (!event.isCancelled()) {
            double oldKarma = data.getKarmaScore();
            data.setKarmaScore(oldKarma + event.getAmount(), event.getSource());
            data.setLastSource(event.getSource());
        }
    }


    /**
     * Subtracts an amount from a player's karma score.
     * Does not accept negative numbers.
     *
     * @param player chosen player.
     * @param amount amount to subtract.
     * @param source source of the karma.
     */
    protected void subtractKarmaScore(Player player, double amount, KarmaSource source) {
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        KarmaLossEvent event = new KarmaLossEvent(player, amount, data.getKarmaScore(), source);
        Bukkit.getPluginManager().callEvent(event);
        if (amount <= 0) event.setCancelled(true);
        if (!event.isCancelled()) {
            double oldKarma = data.getKarmaScore();
            data.setKarmaScore(oldKarma - event.getAmount(), event.getSource());
            data.setLastSource(event.getSource());
        }
    }




}
