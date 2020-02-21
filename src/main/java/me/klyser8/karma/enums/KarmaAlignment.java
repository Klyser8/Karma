package me.klyser8.karma.enums;

import me.klyser8.karma.Karma;
import me.klyser8.karma.util.UtilMethods;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.klyser8.karma.util.UtilMethods.isInteger;

public enum KarmaAlignment {
    EVIL("&4[Evil]", -2147483647, -50000, 0, 0),
    VILE("&c[Vile]", -49999, -30000, 0, 0),
    MEAN("&6[Mean]", -29999, -15000, 0, 0),
    RUDE("&e[Rude]", -14999, -5000, 0, 0),

    NEUTRAL("&7[Neutral]", -4999, 9999, 0, 0),

    KIND("&a[Kind]", 10000, 24999, 0, 0),
    GOOD("&2[Good]", 25000, 49999, 0, 0),
    PURE("&9[Pure]", 50000, 99999, 0, 0),
    BEST("&b[Best]", 100000, 2147483647, 0, 0);

    private String name;
    private int lowBoundary;
    private int highBoundary;
    private double hitPenalty;
    private double killPenalty;

    KarmaAlignment(String name, int lowBoundary, int highBoundary, double hitPenalty, double killPenalty) {
        this.name = name;
        this.lowBoundary = lowBoundary;
        this.highBoundary = highBoundary;
        this.hitPenalty = hitPenalty;
        this.killPenalty = killPenalty;
    }

    public String getName() {
        return name;
    }

    public static void loadAlignments(Karma plugin, ConfigurationSection lang, ConfigurationSection config) {
        Arrays.stream(values()).forEach(aln -> {
            if (lang.isSet(aln.name())) {
                aln.name = lang.getString(aln.name());
                int[] thresholds = UtilMethods.toPrimitiveArray(config.getIntegerList("Alignment Thresholds." + aln.toString()).toArray(new Integer[]{}));
                aln.lowBoundary = thresholds[0];
                aln.highBoundary = thresholds[1];
                if (plugin.playerHittingEnabled)
                    aln.hitPenalty = config.getDouble("Player Hitting.Alignment Amount." + aln.toString());
                if (plugin.playerKillingEnabled)
                    aln.killPenalty = config.getDouble("Player Killing.Alignment Amount." + aln.toString());
            }
        });
    }

    public int getLowBoundary() {
        return lowBoundary;
    }

    public int getHighBoundary() {
        return highBoundary;
    }

    public double getHitPenalty() {
        return hitPenalty;
    }

    public double getKillPenalty() {
        return killPenalty;
    }
}
