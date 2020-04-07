package com.github.klyser8.karma.lang;

import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.karma.KarmaAlignment;

import static me.mattstudios.mf.base.components.MfUtil.color;

public class KarmaEnumFetcher {

    private KarmaPlugin plugin;
    public KarmaEnumFetcher(KarmaPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("ConstantConditions")
    public String getMessageString(Message message) {
        String msg;
        if (plugin.getLanguageHandler().getLang().isSet(message.name()))
            msg = color(plugin.getLanguageHandler().getLang().getString(message.name()));
        else
            msg = color(message.getDefaultMessage());
        return msg;
    }


    @SuppressWarnings("ConstantConditions")
    public String getCommandString(Command command) {
        if (plugin.getLanguageHandler().getLang().isSet(command.name()))
            return color(plugin.getLanguageHandler().getLang().getString(command.name()));
        else
            return color(command.getDefaultCommand());
    }


    @SuppressWarnings("ConstantConditions")
    public String getKeywordString(Keyword keyword) {
        if (plugin.getLanguageHandler().getLang().isSet(keyword.name()))
            return color(plugin.getLanguageHandler().getLang().getString(keyword.name()));
        else
            return color(keyword.getDefaultKeyword());
    }

    @SuppressWarnings("ConstantConditions")
    public String getAlignmentName(KarmaAlignment alignment) {
        if (plugin.getLanguageHandler().getLang().isSet(alignment.name()))
            return color(plugin.getLanguageHandler().getLang().getString(alignment.name()));
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
