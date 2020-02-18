package me.klyser8.karma.enums;

import me.klyser8.karma.util.UtilMethods;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.klyser8.karma.util.UtilMethods.isInteger;

public enum KarmaAlignment {
    EVIL("&4[Evil]", -2147483647, -50000),
    VILE("&c[Vile]", -49999, -30000),
    MEAN("&6[Mean]", -29999, -15000),
    RUDE("&e[Rude]", -14999, -5000),

    NEUTRAL("&7[Neutral]", -4999, 9999),

    KIND("&a[Kind]", 10000, 24999),
    GOOD("&2[Good]", 25000, 49999),
    PURE("&9[Pure]", 50000, 99999),
    BEST("&b[Best]", 100000, 2147483647);

    private String name;
    private int lowBoundary;
    private int highBoundary;

    KarmaAlignment(String name, int lowBoundary, int highBoundary) {
        this.name = name;
        this.lowBoundary = lowBoundary;
        this.highBoundary = highBoundary;
    }

    public String getName() {
        return name;
    }

    public static void loadAlignments(ConfigurationSection lang, ConfigurationSection config) {
        Arrays.stream(values()).forEach(aln -> {
            if (lang.isSet(aln.name())) {
                aln.name = lang.getString(aln.name());
                int[] thresholds = UtilMethods.toPrimitiveArray(config.getIntegerList("Alignment Thresholds." + aln.toString()).toArray(new Integer[]{}));
                aln.lowBoundary = thresholds[0];
                aln.highBoundary = thresholds[1];
            }
        });
    }

    public int getLowBoundary() {
        return lowBoundary;
    }

    public int getHighBoundary() {
        return highBoundary;
    }
}
