package com.github.klyser8.karma.lang;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public enum Message {


    RELOAD("&d[Karma] &rConfig, Data, Lang and Permissions file reloaded!"),
    SAVE("&d[Karma] &rData files saved manually!"),
    OFFLINE_MESSAGES("&d[Karma] &7While you were offline: "),

    ERROR_MISUSE("&4Wrong usage of the command &c/karma&4, try again."),
    ERROR_NO_PERMISSION("&4Unknown command. Type &c/karma help &4for help."),
    ERROR_UNKNOWN_PLAYER("&4You don't have the permission to use this command!"),
    ERROR_UNKNOWN_COMMAND("&4The specified player never played on this server."),
    ERROR_INVALID_NUMBER("&4Invalid number or the amount exceeds the player's Karma limit (2147483647/-2147483647)."),

    HELP_HELP("&d<HELP_COMMAND> &rshows this message."),
    HELP_RELOAD("&d<RELOAD_COMMAND> &rreloads the plugin."),
    HELP_SAVE("&d<SAVE_COMMAND> &rsaves the plugin's data."),
    HELP_VIEW("&d<VIEW_COMMAND> &rshows a player's karma."),
    HELP_ADD("&d<ADD_COMMAND> &radds Karma to a player's total."),
    HELP_REMOVE("&d<REMOVE_COMMAND> &rremoves Karma from a player's total."),
    HELP_SET("&d<SET_COMMAND> &rsets a player's Karma to an amount."),
    HELP_CLEAR("&d<CLEAR_COMMAND> &rclears a player's Karma."),
    HELP_LIST("&d<LIST_COMMAND> &rShows a list of all online players, based on their Karma Scores."),

    SCORE_ADDED_SUCCESSFULLY_SELF("&d[Karma] &fYou have given yourself &a<NUMBER> &fKarma points."),
    SCORE_REMOVED_SUCCESSFULLY_SELF("&d[Karma] &fYou have removed &c<NUMBER> &fKarma points from yourself."),
    SCORE_SET_SUCCESSFULLY_SELF("&d[Karma] &fYou set your Karma Score to &e<NUMBER>."),
    SCORE_CLEARED_SUCCESSFULLY_SELF("&d[Karma] &fYou cleared your Karma score."),
    VIEW_SCORE_SELF("&d[Karma] &fYour Karma score is &e<NUMBER>"),
    VIEW_ALIGNMENT_SELF("&d[Karma] &fYour alignment is <ALIGNMENT> &f."),

    SCORE_ADDED_SUCCESSFULLY("&d[Karma] &fGiven &a<NUMBER> &fKarma points to &e<PLAYER>."),
    SCORE_REMOVED_SUCCESSFULLY("&d[Karma] &fRemoved &c<NUMBER> &fKarma points from &e<PLAYER>."),
    SCORE_SET_SUCCESSFULLY("&d[Karma] &fSet &e<PLAYER>&f's Karma score to &e<NUMBER>."),
    SCORE_CLEARED_SUCCESSFULLY("&d[Karma] &fCleared &e<PLAYER>&f's Karma score."),
    VIEW_SCORE("&d[Karma] &e<PLAYER>&f's Karma score is &e<NUMBER>"),
    VIEW_ALIGNMENT("&d[Karma] &e<PLAYER>&f's alignment is <ALIGNMENT> &f."),
    VIEW_LIST_PLAYER("- &d<PLAYER> &rKarma: &e<SCORE>&r - &d<PLAYER> &rAlignment: <ALIGNMENT>");

    private String message;
    Message(String message) {
        this.message = message;
    }

    public String getDefaultMessage() {
        return message;
    }
}
