package me.klyser8.karma.enums;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;

public enum Keyword {
    NUMBER("Number"),
    PLAYER("Player");

    private String word;
    Keyword(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public static void loadKeywords(ConfigurationSection section) {
        Arrays.stream(values()).forEach(wrd -> {
            if (section.isSet(wrd.name()))
                wrd.word = section.getString(wrd.name());
        });
    }
}
