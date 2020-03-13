package me.klyser8.karma.commands;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.Message;
import me.klyser8.karma.handlers.KarmaHandler;
import me.klyser8.karma.handlers.KarmaEnumFetcher;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command("karma")
public class SaveKarmaCommand extends CommandBase {

    private Karma plugin;
    private KarmaHandler karma;
    private KarmaEnumFetcher fetcher;
    public SaveKarmaCommand(Karma plugin, KarmaHandler karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }


    @SubCommand("save")
    @Completion("#empty")
    @Permission("karma.command.save")
    public void saveCommand(CommandSender sender, String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getStorageHandler().savePlayerData(player.getUniqueId());
        }
        sender.sendMessage(fetcher.getMessageString(Message.SAVE));
    }

}
