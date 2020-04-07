package com.github.klyser8.karma.storage;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.api.events.KarmaAlignmentChangeEvent;
import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.karma.KarmaAlignment;
import com.github.klyser8.karma.karma.KarmaSource;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("unused")
public class PlayerKarma {

    //Important data
    private UUID uuid;
    private KarmaPlugin plugin;
    private KarmaManager karmaManager;

    //Temporary Data
    private Player player;
    private KarmaSource lastSource;
    private Double recentKarmaGained;

    //Permanent Data
    private boolean firstJoin = true;

    private double karmaScore = 0.0;

    private String playerLanguage = "english";

    private KarmaAlignment karmaAlignment = KarmaAlignment.NEUTRAL;

    public PlayerKarma(Player player, KarmaPlugin plugin) {
        this.plugin = plugin;
        this.player = player;
        this.uuid = player.getUniqueId();
        this.recentKarmaGained = 0.0;
        karmaManager = new KarmaManager();
    }


    /**The below data getters/setters are for temporary data only.
     * This data will not be saved on any external file when the
     * plugin is disabled.
     */
    public UUID getUuid() {
        return uuid;
    }

    public KarmaSource getLastSource() {
        return lastSource;
    }

    public void setLastSource(KarmaSource source) {
        lastSource = source;
    }

    public Double getRecentKarmaGained() {
        return recentKarmaGained;
    }

    public void setRecentKarmaGained(Double amount) {
        recentKarmaGained = Math.round(amount * 10) / 10.0;
    }

    public void clearRecentKarmaGained() {
        recentKarmaGained = 0.0;
    }

    /**The below getters/setters are for permanent data only.
     * This means that any of the data modified using these methods
     * will be saved on an external .plr file on plugin disable
     */
    public OfflinePlayer getPlayer() {
        return player;
    }

    public boolean isFirstJoin() {
        return firstJoin;
    }

    public void setFirstJoin(boolean join) {
        firstJoin = join;
    }

    public double getKarmaScore() {
        return karmaScore;
    }

    public void setKarmaScore(double amount, KarmaSource source) {
        if (plugin.disabledWorldList.contains(player.getWorld().getName())) return;
        double oldKarmaScore = karmaScore;
        boolean isCreative = player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
        if (amount < KarmaPlugin.karmaLowLimit)
            amount = KarmaPlugin.karmaLowLimit;
        else if (amount > KarmaPlugin.karmaHighLimit)
            amount = KarmaPlugin.karmaHighLimit;
        if (amount > karmaScore) {
            if (!plugin.getKarmaLimitList().contains(getPlayer()) && source != KarmaSource.COMMAND && source != KarmaSource.VOTING) {
                if (isCreative && !plugin.isCreativeKarma()) return;
                recentKarmaGained = (double) (Math.round((recentKarmaGained + (amount - karmaScore)) * 10)) / 10;
            }
        } else {
            if (isCreative && source != KarmaSource.COMMAND && source != KarmaSource.VOTING) {
                if (!plugin.isCreativeKarma()) return;
            }
        }
        karmaScore = Math.round(amount * 10) / 10.0;
        lastSource = source;
        if (recentKarmaGained >= plugin.getKarmaTimeLimit()) {
            if (plugin.getKarmaLimitList().contains(player)) return;
            plugin.getKarmaLimitList().add(player);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> karmaManager.updateAlignments(plugin, player), 10);
    }

    public KarmaAlignment getKarmaAlignment() {
        return karmaAlignment;
    }

    public void setKarmaAlignment(KarmaAlignment alignment, boolean triggersEvent) {
        if (triggersEvent) {
            KarmaAlignmentChangeEvent event = new KarmaAlignmentChangeEvent(player, karmaAlignment, alignment);
            Bukkit.getPluginManager().callEvent(event);
        }
        karmaAlignment = alignment;
    }

    public String getPlayerLanguage() {
        return playerLanguage;
    }

    public void setPlayerLanguage(String language) {
        playerLanguage = language;
    }

}
