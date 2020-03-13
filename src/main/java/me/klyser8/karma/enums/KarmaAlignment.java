package me.klyser8.karma.enums;

import me.klyser8.karma.Karma;
import me.klyser8.karma.util.UtilMethods;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.klyser8.karma.util.UtilMethods.isInteger;

public enum KarmaAlignment {
    EVIL("&4[Evil]", -2147483647, -50000, 2.0, 50),
    VILE("&c[Vile]", -49999, -30000, 1.5, 25),
    MEAN("&6[Mean]", -29999, -15000, 1.0, 15),
    RUDE("&e[Rude]", -14999, -5000, 0.5, 5),

    NEUTRAL("&7[Neutral]", -4999, 9999, 0, 0),

    KIND("&a[Kind]", 10000, 24999, -2.0, -15),
    GOOD("&2[Good]", 25000, 49999, -3.0, -50),
    PURE("&9[Pure]", 50000, 99999, -4.0, -100),
    BEST("&b[Best]", 100000, 2147483647, -5.0, -250);

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

    public String getDefaultName() {
        return name;
    }

    public int getDefaultLowBoundary() {
        return lowBoundary;
    }

    public int getDefaultHighBoundary() {
        return highBoundary;
    }

    public double getDefaultHitPenalty() {
        return hitPenalty;
    }

    public double getDefaultKillPenalty() {
        return killPenalty;
    }
}
