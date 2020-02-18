package me.klyser8.karma.enums;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;

public enum Message {


    KARMA_RELOAD("&d[Karma] &rConfig, Data, Lang and Permissions file reloaded!"),
    KARMA_SAVE("&d[Karma] &rData files saved manually!"),
    KARMA_OFFLINE_MESSAGES("&d[Karma] &7While you were offline: "),

    KARMA_ERROR_MISUSE("&4Wrong usage of the command &c/karma&4, try again."),
    KARMA_ERROR_NO_PERMISSION("&4Unknown command. Type &c/karma help &4for help."),
    KARMA_ERROR_UNKNOWN_PLAYER("&4You don't have the permission to use this command!"),
    KARMA_ERROR_UNKNOWN_COMMAND("&4The specified player never played on this server."),
    KARMA_ERROR_INVALID_NUMBER("&4Invalid number or the amount exceeds the player's Karma limit (2147483647/-2147483647)."),

    KARMA_HELP_HELP("&d<COMMAND> &rshows this message."),
    KARMA_HELP_RELOAD("&d<COMMAND> &rreloads the plugin."),
    KARMA_HELP_SAVE("&d<COMMAND> &rsaves the plugin's data."),
    KARMA_HELP_VIEW("&d<COMMAND> &rshows a player's karma."),
    KARMA_HELP_ADD("&d<COMMAND> &radds Karma to a player's total."),
    KARMA_HELP_REMOVE("&d<COMMAND> &rremoves Karma from a player's total."),
    KARMA_HELP_SET("&d<COMMAND> &rsets a player's Karma to an amount."),
    KARMA_HELP_CLEAR("&d<COMMAND> &rclears a player's Karma."),
    KARMA_HELP_LIST("&d<COMMAND> &rShows a list of all online players, based on their Karma Scores."),

    KARMA_ADDED_SUCCESSFULLY("&d[Karma] &fYou have given yourself &a<NUMBER> &fKarma points."),
    KARMA_REMOVED_SUCCESSFULLY("&d[Karma] &fYou have removed &c<NUMBER> &fKarma points from yourself."),
    KARMA_SET_SUCCESSFULLY("&d[Karma] &fYou set your Karma Score to &e<NUMBER>."),
    KARMA_CLEARED_SUCCESSFULLY("&d[Karma] &fYou cleared your Karma score."),
    KARMA_VIEW_SCORE_SELF("&d[Karma] &fYour Karma score is &e<NUMBER>"),
    KARMA_VIEW_ALIGNMENT_SELF("&d[Karma] &fYour alignment is <ALIGNMENT> &f."),

    KARMA_ADDED_SUCCESSFULLY_SELF("&d[Karma] &fGiven &a<NUMBER> &fKarma points to &e<PLAYER>."),
    KARMA_REMOVED_SUCCESSFULLY_SELF("&d[Karma] &fRemoved &c<NUMBER> &fKarma points from &e<PLAYER>."),
    KARMA_SET_SUCCESSFULLY_SELF("&d[Karma] &fSet &e<PLAYER>&f's Karma score to &e<NUMBER>."),
    KARMA_CLEARED_SUCCESSFULLY_SELF("&d[Karma] &fCleared &e<PLAYER>&f's Karma score."),
    KARMA_VIEW_SCORE("&d[Karma] &e<PLAYER>&f's Karma score is &e<NUMBER>"),
    KARMA_VIEW_ALIGNMENT("&d[Karma] &e<PLAYER>&f's alignment is <ALIGNMENT> &f."),
    KARMA_VIEW_LIST_PLAYER("- &d<PLAYER> &rKarma: &e<SCORE>&r - &d<PLAYER> &rAlignment: <ALIGNMENT>");

    private String message;
    Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static void loadMessages(ConfigurationSection section) {
        Arrays.stream(values()).forEach(msg -> {
            if (section.isSet(msg.name()))
                msg.message = section.getString(msg.name());
        });
    }
}
