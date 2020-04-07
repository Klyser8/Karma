package com.github.klyser8.karma.karma.commands;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.lang.Message;
import com.github.klyser8.karma.storage.PlayerKarma;
import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.karma.KarmaSource;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@Command("karma")
public class ClearKarmaCommand extends CommandBase {

    private KarmaPlugin plugin;
    private KarmaManager karma;
    private KarmaEnumFetcher fetcher;
    public ClearKarmaCommand(KarmaPlugin plugin, KarmaManager karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }


    @SubCommand("clear")
    @Completion("#players")
    @Permission("karma.command.clear")
    public void clearCommand(CommandSender sender, String player) {
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(Objects.requireNonNull(Bukkit.getPlayer(player)).getUniqueId());
        if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(player))) {
            sender.sendMessage(fetcher.getMessageString(Message.ERROR_UNKNOWN_PLAYER));
            return;
        }
        if (sender.getName().equalsIgnoreCase(player)) {
            sender.sendMessage(fetcher.getMessageString(Message.SCORE_CLEARED_SUCCESSFULLY_SELF));
        } else {
            sender.sendMessage(fetcher.getMessageString(Message.SCORE_CLEARED_SUCCESSFULLY).replace("<PLAYER>", player));
        }
        data.setKarmaScore(0, KarmaSource.COMMAND);
    }

}
