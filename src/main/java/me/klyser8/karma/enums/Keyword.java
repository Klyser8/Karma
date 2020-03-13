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

    public String getDefaultKeyword() {
        return word;
    }
}
