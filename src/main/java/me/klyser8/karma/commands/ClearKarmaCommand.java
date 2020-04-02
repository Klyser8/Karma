package me.klyser8.karma.commands;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.enums.Message;
import me.klyser8.karma.handlers.KarmaHandler;
import me.klyser8.karma.handlers.KarmaEnumFetcher;
import me.klyser8.karma.storage.PlayerData;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@Command("karma")
public class ClearKarmaCommand extends CommandBase {

    private Karma plugin;
    private KarmaHandler karma;
    private KarmaEnumFetcher fetcher;
    public ClearKarmaCommand(Karma plugin, KarmaHandler karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }


    @SubCommand("clear")
    @Completion("#players")
    @Permission("karma.command.clear")
    public void clearCommand(CommandSender sender, String player) {
        PlayerData data = plugin.getStorageHandler().getPlayerData(Objects.requireNonNull(Bukkit.getPlayer(player)).getUniqueId());
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
