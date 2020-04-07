package com.github.klyser8.karma.storage;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.lang.Message;
import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;

@Command("karma")
public class ReloadKarmaCommand extends CommandBase {

    private KarmaPlugin plugin;
    private KarmaManager karma;
    private KarmaEnumFetcher fetcher;
    public ReloadKarmaCommand(KarmaPlugin plugin, KarmaManager karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }

    @SubCommand("reload")
    @Completion("#empty")
    @Permission("karma.command.reload")
    public void reloadCommand(CommandSender sender, String[] args) {
        plugin.reload();
        sender.sendMessage(fetcher.getMessageString(Message.RELOAD));
    }

}
