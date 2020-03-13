package me.klyser8.karma.handlers;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.Command;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.Keyword;
import me.klyser8.karma.enums.Message;
import static me.klyser8.karma.util.UtilMethods.color;

public class KarmaEnumFetcher {

    private Karma plugin;
    public KarmaEnumFetcher(Karma plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("ConstantConditions")
    public String getMessageString(Message message) {
        String msg;
        if (plugin.getSettings().getLang().isSet(message.name()))
            msg = color(plugin.getSettings().getLang().getString(message.name()));
        else
            msg = color(message.getDefaultMessage());
        return msg;
    }


    @SuppressWarnings("ConstantConditions")
    public String getCommandString(Command command) {
        if (plugin.getSettings().getLang().isSet(command.name()))
            return color(plugin.getSettings().getLang().getString(command.name()));
        else
            return color(command.getDefaultCommand());
    }


    @SuppressWarnings("ConstantConditions")
    public String getKeywordString(Keyword keyword) {
        if (plugin.getSettings().getLang().isSet(keyword.name()))
            return color(plugin.getSettings().getLang().getString(keyword.name()));
        else
            return color(keyword.getDefaultKeyword());
    }

    @SuppressWarnings("ConstantConditions")
    public String getAlignmentName(KarmaAlignment alignment) {
        if (plugin.getSettings().getLang().isSet(alignment.name()))
            return color(plugin.getSettings().getLang().getString(alignment.name()));
        else
            return color(alignment.getDefaultName());
    }

    public Integer getAlignmentLowBoundary(KarmaAlignment alignment) {
        if (plugin.getConfig().isSet("Alignment Thresholds." + alignment.toString().toUpperCase()))
            return plugin.getConfig().getIntegerList("Alignment Thresholds." + alignment.toString().toUpperCase()).get(0);
        else
            return alignment.getDefaultLowBoundary();
    }

    public Integer getAlignmentHighBoundary(KarmaAlignment alignment) {
        if (plugin.getConfig().isSet("Alignment Thresholds." + alignment.toString().toUpperCase()))
            return plugin.getConfig().getIntegerList("Alignment Thresholds." + alignment.toString().toUpperCase()).get(1);
        else
            return alignment.getDefaultHighBoundary();
    }

    public Double getAlignmentHitPenalty(KarmaAlignment alignment) {
        if (plugin.getConfig().isSet("Player Hitting.Alignment Amount." + alignment.toString().toUpperCase()))
            return plugin.getConfig().getDouble("Player Hitting.Alignment Amount." + alignment.toString().toUpperCase());
        else
            return alignment.getDefaultHitPenalty();
    }

    public Double getAlignmentKillPenalty(KarmaAlignment alignment) {
        if (plugin.getConfig().isSet("Player Killing.Alignment Amount." + alignment.toString().toUpperCase()))
            return plugin.getConfig().getDouble("Player Killing.Alignment Amount." + alignment.toString().toUpperCase());
        else
            return alignment.getDefaultKillPenalty();
    }

}
