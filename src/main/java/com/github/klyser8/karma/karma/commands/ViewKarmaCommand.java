package com.github.klyser8.karma.karma.commands;

import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.lang.Message;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.mattstudios.mf.base.components.MfUtil.color;

@Command("karma")
public class ViewKarmaCommand extends CommandBase {

    private KarmaPlugin plugin;
    private KarmaEnumFetcher fetcher;
    public ViewKarmaCommand(KarmaPlugin plugin, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.fetcher = fetcher;
    }

    @Default
    public void viewCommand(CommandSender sender, @Optional String player) {
        if (player == null) {
            if (!sender.hasPermission("karma.command.view.self")) return;
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(color("&4Please specify a player."));
                return;
            }
            sender.sendMessage(fetcher.getMessageString(Message.VIEW_SCORE_SELF).replace("<NUMBER>", String.valueOf(plugin.getPlayerKarma((Player) sender))));
            sender.sendMessage(fetcher.getMessageString(Message.VIEW_ALIGNMENT_SELF)
                    .replace("<ALIGNMENT>", String.valueOf(fetcher.getAlignmentName(plugin.getPlayerAlignment((Player) sender)))));
        } else {
            if (!sender.hasPermission("karma.command.view.others")) return;
            Player targetPlayer = Bukkit.getPlayerExact(player);
            if (targetPlayer == null) {
                sender.sendMessage(fetcher.getMessageString(Message.ERROR_UNKNOWN_PLAYER));
                return;
            }
            sender.sendMessage(fetcher.getMessageString(Message.VIEW_SCORE)
                    .replace("<PLAYER>", player)
                    .replace("<NUMBER>", String.valueOf(plugin.getPlayerKarma(targetPlayer))));
            sender.sendMessage(fetcher.getMessageString(Message.VIEW_ALIGNMENT)
                    .replace("<PLAYER>", player)
                    .replace("<ALIGNMENT>", String.valueOf(fetcher.getAlignmentName(plugin.getPlayerAlignment(targetPlayer)))));
        }
    }

    @CompleteFor("view")
    public List<String> commandCompletion(List<String> args) {
        if (args.size() == 0)
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
