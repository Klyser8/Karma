package me.klyser8.karma.commands;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.Keyword;
import me.klyser8.karma.enums.Message;
import me.klyser8.karma.handlers.KarmaHandler;
import me.klyser8.karma.handlers.KarmaEnumFetcher;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.command.CommandSender;

import static me.klyser8.karma.util.UtilMethods.color;
import static me.klyser8.karma.enums.Command.*;

@Command("karma")
public class HelpKarmaCommand extends CommandBase {

    private Karma plugin;
    private KarmaHandler karma;
    private KarmaEnumFetcher fetcher;
    public HelpKarmaCommand(Karma plugin, KarmaHandler karma, KarmaEnumFetcher fetcher) {
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
        if (sender.hasPermission("karma.commands.help") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_HELP)
                    .replace("<HELP_COMMAND>", "/Karma " + fetcher.getCommandString(HELP_COMMAND).toLowerCase()));
        if (sender.hasPermission("karma.commands.reload") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_RELOAD)
                    .replace("<RELOAD_COMMAND>", "/Karma " + fetcher.getCommandString(RELOAD_COMMAND).toLowerCase()));
        if (sender.hasPermission("karma.commands.save") || isOp)
            sender.sendMessage(fetcher.getMessageString(Message.HELP_SAVE)
                    .replace("<SAVE_COMMAND>", "/Karma " + fetcher.getCommandString(SAVE_COMMAND).toLowerCase()));

        String viewCommand = "";
        if (sender.hasPermission("karma.commands.view.others") || isOp)
            viewCommand = fetcher.getMessageString(Message.HELP_VIEW)
                .replace("<VIEW_COMMAND>", "/Karma " + fetcher.getCommandString(VIEW_COMMAND).toLowerCase() + " [" + fetcher.getKeywordString(Keyword.PLAYER).toLowerCase() + "]");
        else if (sender.hasPermission("karma.commands.view.self"))
            viewCommand = fetcher.getMessageString(Message.HELP_VIEW)
                    .replace("<VIEW_COMMAND>", "/Karma " + fetcher.getCommandString(VIEW_COMMAND).toLowerCase());
        sender.sendMessage(viewCommand);
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
