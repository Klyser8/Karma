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
public class AddKarmaCommand extends CommandBase {

    private KarmaPlugin plugin;
    private KarmaManager karma;
    private KarmaEnumFetcher fetcher;
    public AddKarmaCommand(KarmaPlugin plugin, KarmaManager karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }


    @SubCommand("add")
    @Permission("karma.command.add")
    public void addCommand(CommandSender sender, Double amount, @Optional String player) {
        if (player == null) player = sender.getName();
        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayerExact(player))) {
            sender.sendMessage(fetcher.getMessageString(Message.ERROR_UNKNOWN_PLAYER));
            return;
        }
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(Objects.requireNonNull(Bukkit.getPlayer(player)).getUniqueId());

        if (!isDouble(String.valueOf(amount)) || amount <= 0) {
            sender.sendMessage(fetcher.getMessageString(Message.ERROR_INVALID_NUMBER));
            return;
        }
        if (sender.getName().equalsIgnoreCase(player)) {
            sender.sendMessage(fetcher.getMessageString(Message.SCORE_ADDED_SUCCESSFULLY_SELF).replace("<NUMBER>", String.valueOf(amount)));
        } else {
            sender.sendMessage(fetcher.getMessageString(Message.SCORE_ADDED_SUCCESSFULLY).replace("<PLAYER>", player)
                    .replace("<NUMBER>", String.valueOf(amount)));
        }
        data.setKarmaScore(data.getKarmaScore() + amount, KarmaSource.COMMAND);
    }

    @CompleteFor("add")
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
