package com.github.klyser8.karma.storage;

import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.lang.Message;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
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

    private KarmaPlugin plugin;
    private KarmaManager karma;
    private KarmaEnumFetcher fetcher;
    public SaveKarmaCommand(KarmaPlugin plugin, KarmaManager karma, KarmaEnumFetcher fetcher) {
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
