package com.github.klyser8.karma.karma.commands;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.lang.Message;
import com.github.klyser8.karma.storage.PlayerKarma;
import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.karma.KarmaSource;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import com.github.klyser8.karma.api.util.UtilMethods;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.github.klyser8.karma.api.util.GenericMethods.isDouble;

@Command("karma")
public class SetKarmaCommand extends CommandBase {

    private KarmaPlugin plugin;
    private KarmaManager karma;
    private KarmaEnumFetcher fetcher;
    public SetKarmaCommand(KarmaPlugin plugin, KarmaManager karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }


    @SubCommand("set")
    @Permission("karma.command.set")
    public void setCommand(CommandSender sender, Double score, @Optional String player) {
        if (player == null) player = sender.getName();
        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(player))) {
            sender.sendMessage(fetcher.getMessageString(Message.ERROR_UNKNOWN_PLAYER));
            return;
        }
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(Objects.requireNonNull(Bukkit.getPlayer(player)).getUniqueId());
        if (!isDouble(String.valueOf(score)) || score > KarmaPlugin.karmaHighLimit || score < KarmaPlugin.karmaLowLimit) {
            sender.sendMessage(fetcher.getMessageString(Message.ERROR_INVALID_NUMBER));
            return;
        }
        if (sender.getName().equalsIgnoreCase(player)) {
            sender.sendMessage(fetcher.getMessageString(Message.SCORE_SET_SUCCESSFULLY_SELF).replace("<NUMBER>", String.valueOf(score)));
        } else {
            sender.sendMessage(fetcher.getMessageString(Message.SCORE_SET_SUCCESSFULLY).replace("<PLAYER>", player)
                    .replace("<NUMBER>", String.valueOf(score)));
        }
        data.setKarmaScore(score, KarmaSource.COMMAND);
    }

    @CompleteFor("set")
    public List<String> commandCompletion(List<String> args) {
        if (args.size() != 2)
            return null;
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers())
            playerNames.add(player.getName());
        List<String> tabList = new ArrayList<>();
        if (args.get(args.size() - 1).equals(""))
            return playerNames;
        for (String option : playerNames) {
            if (option.toLowerCase().startsWith(args.get(args.size() - 1).toLowerCase())) {
                tabList.add(option);
            }
        }
        Collections.sort(tabList);
        return tabList;
    }

}
