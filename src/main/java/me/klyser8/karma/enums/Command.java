package me.klyser8.karma.enums;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;

public enum Command {
    KARMA_RELOAD_COMMAND("reload"),
    KARMA_SAVE_COMMAND("save"),
    KARMA_HELP_COMMAND("help"),

    KARMA_ADD_COMMAND("add"),
    KARMA_REMOVE_COMMAND("remove"),
    KARMA_SET_COMMAND("set"),
    KARMA_CLEAR_COMMAND("clear"),
    KARMA_GET_COMMAND("get"),
    KARMA_VIEW_COMMAND("view"),
    KARMA_LIST_COMMAND("list");

    private String command;

    Command(String command) {
        this.command = command;
    }

    public static void loadCommands(ConfigurationSection section) {
        Arrays.stream(values()).forEach(cmd -> {
            if (section.isSet(cmd.name()))
                cmd.command = section.getString(cmd.name());
        });
    }

    public String getCommand() {
        return command;
    }
}
