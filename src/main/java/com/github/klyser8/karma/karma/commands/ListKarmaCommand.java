package com.github.klyser8.karma.karma.commands;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.lang.Message;
import com.github.klyser8.karma.storage.PlayerKarma;
import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Command("karma")
public class ListKarmaCommand extends CommandBase {

    private KarmaPlugin plugin;
    private KarmaManager karma;
    private KarmaEnumFetcher fetcher;
    public ListKarmaCommand(KarmaPlugin plugin, KarmaManager karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }


    @SubCommand("list")
    @Completion("#empty")
    @Permission("karma.command.list")
    public void saveCommand(CommandSender sender, String[] args) {
        Map<PlayerKarma, Double> map = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerKarma pData = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
            map.put(pData, pData.getKarmaScore());
        }
        LinkedHashMap<PlayerKarma, Double> reverseSortedMap = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        for (PlayerKarma pData : reverseSortedMap.keySet()) {
            sender.sendMessage(fetcher.getMessageString(Message.VIEW_LIST_PLAYER)
                    .replace("<PLAYER>", pData.getPlayer().getName())
                    .replace("<SCORE>", String.valueOf(reverseSortedMap.get(pData)))
                    .replace("<ALIGNMENT>", fetcher.getAlignmentName(pData.getKarmaAlignment())));
        }
    }

}
