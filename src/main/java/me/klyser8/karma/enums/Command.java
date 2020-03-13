package me.klyser8.karma.enums;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;

public enum Command {
    RELOAD_COMMAND("reload"),
    SAVE_COMMAND("save"),
    HELP_COMMAND("help"),

    ADD_COMMAND("add"),
    REMOVE_COMMAND("remove"),
    SET_COMMAND("set"),
    CLEAR_COMMAND("clear"),
    VIEW_COMMAND("view"),
    LIST_COMMAND("list");

    private String command;

    Command(String command) {
        this.command = command;
    }

    public String getDefaultCommand() {
        return command;
    }
}
