package com.github.klyser8.karma.karma.commands;

import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.lang.Keyword;
import com.github.klyser8.karma.lang.Message;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;

import static com.github.klyser8.karma.lang.Command.*;
import static me.mattstudios.mf.base.components.MfUtil.color;

@Command("karma")
public class HelpKarmaCommand extends CommandBase {

    private KarmaPlugin plugin;
    private KarmaManager karma;
    private KarmaEnumFetcher fetcher;
    public HelpKarmaCommand(KarmaPlugin plugin, KarmaManager karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }

    @SubCommand("help")
    @Completion("#empty")
    @Permission("karma.command.help")
    public void helpCommand(CommandSender sender, String[] args) {
        boolean isOp = sender.isOp();
        sender.sendMessage(color("&5--------- &r" + fetcher.getCommandString(HELP_COMMAND).toUpperCase() + " &5---------------------------"));
        String viewCommand = "";
        if (sender.hasPermission("karma.commands.view.others") || isOp)
            viewCommand = fetcher.getMessageString(Message.HELP_VIEW)
                    .replace("<VIEW_COMMAND>", "/Karma [" + fetcher.getKeywordString(Keyword.PLAYER).toLowerCase() + "]");
        else if (sender.hasPermission("karma.commands.view.self"))
            viewCommand = fetcher.getMessageString(Message.HELP_VIEW)
                    .replace("<VIEW_COMMAND>", "/Karma");
        sender.sendMessage(viewCommand);
        if (sender.hasPermission("karma.commands.help") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_HELP)
                    .replace("<HELP_COMMAND>", "/Karma " + fetcher.getCommandString(HELP_COMMAND).toLowerCase()));
        if (sender.hasPermission("karma.commands.reload") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_RELOAD)
                    .replace("<RELOAD_COMMAND>", "/Karma " + fetcher.getCommandString(RELOAD_COMMAND).toLowerCase()));
        if (sender.hasPermission("karma.commands.save") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_SAVE)
                    .replace("<SAVE_COMMAND>", "/Karma " + fetcher.getCommandString(SAVE_COMMAND).toLowerCase()));

        if (sender.hasPermission("karma.commands.add") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_ADD)
                    .replace("<ADD_COMMAND>", "/Karma " + fetcher.getCommandString(ADD_COMMAND).toLowerCase() + " <" + fetcher.getKeywordString(Keyword.PLAYER).toLowerCase() + "> <" + fetcher.getKeywordString(Keyword.NUMBER).toLowerCase() + ">"));
        if (sender.hasPermission("karma.commands.remove") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_REMOVE)
                    .replace("<REMOVE_COMMAND>", "/Karma " + fetcher.getCommandString(REMOVE_COMMAND).toLowerCase() + " <" + fetcher.getKeywordString(Keyword.PLAYER).toLowerCase() + "> <" + fetcher.getKeywordString(Keyword.NUMBER).toLowerCase() + ">"));
        if (sender.hasPermission("karma.commands.set") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_SET)
                    .replace("<SET_COMMAND>", "/Karma " + fetcher.getCommandString(SET_COMMAND).toLowerCase() + " <" + fetcher.getKeywordString(Keyword.PLAYER).toLowerCase() + "> <" + fetcher.getKeywordString(Keyword.NUMBER).toLowerCase() + ">"));
        if (sender.hasPermission("karma.commands.clear") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_CLEAR)
                    .replace("<CLEAR_COMMAND>", "/Karma " + fetcher.getCommandString(CLEAR_COMMAND).toLowerCase() + " <" + fetcher.getKeywordString(Keyword.PLAYER).toLowerCase() + ">"));
        if (sender.hasPermission("karma.commands.list") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_LIST)
                    .replace("<LIST_COMMAND>", "/Karma " + fetcher.getCommandString(LIST_COMMAND).toLowerCase()));
    }

}
