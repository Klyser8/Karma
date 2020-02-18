package me.klyser8.karma.handlers;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.events.KarmaAlignmentChangeEvent;
import me.klyser8.karma.util.UtilMethods;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.klyser8.karma.util.UtilMethods.color;
import static me.klyser8.karma.util.UtilMethods.sendDebugMessage;

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
     * will be saved on an external .ply file on plugin disable
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
        double oldKarmaScore = karmaScore;
        if (-2147483647 < amount && amount < 2147483647) {
            if (amount > karmaScore) {
                if (!plugin.getKarmaLimitList().contains(getPlayer()) && source != KarmaSource.COMMAND && source != KarmaSource.VOTING) {
                    if ((player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) ) {
                        if (plugin.creativeKarma) {
                            recentKarmaGained = (double) (Math.round((recentKarmaGained + (amount - karmaScore)) * 10)) / 10;
                            karmaScore = Math.round(amount * 10) / 10.0;
                            lastSource = source;
                            sendDebugMessage(color("&d[Karma] &e " + player.getName() + "&r's Karma Score: &r" + karmaScore + "&a (+" + (amount - oldKarmaScore)) + ")");
                        } else
                            UtilMethods.sendDebugMessage(getPlayer().getName() + " did not gain karma since they were in Creative/Spectator mode.");
                    } else {
                        recentKarmaGained = (double) (Math.round((recentKarmaGained + (amount - karmaScore)) * 10)) / 10;
                        karmaScore = Math.round(amount * 10) / 10.0;
                        lastSource = source;
                        sendDebugMessage(color("&d[Karma] &e " + player.getName() + "&r's Karma Score: &r" + karmaScore + "&a (+" + (amount - oldKarmaScore)) + ")");
                    }
                } else {
                    if (source == KarmaSource.COMMAND || source == KarmaSource.VOTING) {
                        karmaScore = Math.round(amount * 10) / 10.0;
                        lastSource = source;
                        sendDebugMessage(color("&d[Karma] &e " + player.getName() + "&r's Karma Score: &r" + karmaScore + "&a (+" + (amount - oldKarmaScore)) + ")");
                    } else {
                        sendDebugMessage(getPlayer().getName() + " has reached their recent karma limit.");
                    }
                }
            } else {
                if ((player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) && source != KarmaSource.COMMAND && source != KarmaSource.VOTING) {
                    if (plugin.creativeKarma) {
                        karmaScore = Math.round(amount * 10) / 10.0;
                        lastSource = source;
                        sendDebugMessage(color("&d[Karma] &e " + player.getName() + "&r's Karma Score: &r" + karmaScore + "&c (-" + (oldKarmaScore - amount)) + ")");
                    } else
                        UtilMethods.sendDebugMessage(getPlayer().getName() + " did not lose karma since they were in Creative/Spectator mode.");
                } else {
                    karmaScore = Math.round(amount * 10) / 10.0;
                    lastSource = source;
                    sendDebugMessage(color("&d[Karma] &e " + player.getName() + "&r's Karma Score: &r" + karmaScore + "&c (-" + (oldKarmaScore - amount)) + ")");
                }
            }
            if (recentKarmaGained >= plugin.karmaTimeLimit) {
                if (!plugin.getKarmaLimitList().contains(player)) {
                    plugin.getKarmaLimitList().add(player);
                    sendDebugMessage(player.getName() + " has reached the max recent karma!");
                }
            }
        } else
            sendDebugMessage("Maximum possible amount of total Karma reached", getPlayer().getName());
        sendDebugMessage("Recent Karma Gained", recentKarmaGained.toString());
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
