package me.klyser8.karma.storage;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.events.KarmaAlignmentChangeEvent;
import me.klyser8.karma.handlers.KarmaHandler;
import me.klyser8.karma.util.UtilMethods;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.klyser8.karma.util.UtilMethods.color;

@SuppressWarnings("unused")
public class PlayerData {

    //Important data
    private UUID uuid;
    private Karma plugin;

    //Temporary Data
    private Player player;
    private KarmaSource lastSource;
    private Double recentKarmaGained;

    //Permanent Data
    private boolean firstJoin = true;

    private double karmaScore = 0.0;

    private String playerLanguage = "english";

    private KarmaAlignment karmaAlignment = KarmaAlignment.NEUTRAL;

    public PlayerData(Player player, Karma plugin) {
        this.plugin = plugin;
        this.player = player;
        this.uuid = player.getUniqueId();
        this.recentKarmaGained = 0.0;
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
        if (amount < Karma.karmaLowLimit)
            amount = Karma.karmaLowLimit;
        else if (amount > Karma.karmaHighLimit)
            amount = Karma.karmaHighLimit;
        if (amount > karmaScore) {
            if (!plugin.getKarmaLimitList().contains(getPlayer()) && source != KarmaSource.COMMAND && source != KarmaSource.VOTING) {
                if (isCreative && !plugin.creativeKarma) return;
                recentKarmaGained = (double) (Math.round((recentKarmaGained + (amount - karmaScore)) * 10)) / 10;
            }
        } else {
            if (isCreative && source != KarmaSource.COMMAND && source != KarmaSource.VOTING) {
                if (!plugin.creativeKarma) return;
            }
        }
        karmaScore = Math.round(amount * 10) / 10.0;
        lastSource = source;
        if (recentKarmaGained >= plugin.karmaTimeLimit) {
            if (plugin.getKarmaLimitList().contains(player)) return;
            plugin.getKarmaLimitList().add(player);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> KarmaHandler.updateAlignments(plugin, player), 10);
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
