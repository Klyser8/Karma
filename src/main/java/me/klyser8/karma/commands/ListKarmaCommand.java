package me.klyser8.karma.commands;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.Message;
import me.klyser8.karma.handlers.KarmaHandler;
import me.klyser8.karma.handlers.KarmaEnumFetcher;
import me.klyser8.karma.storage.PlayerData;
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

    private Karma plugin;
    private KarmaHandler karma;
    private KarmaEnumFetcher fetcher;
    public ListKarmaCommand(Karma plugin, KarmaHandler karma, KarmaEnumFetcher fetcher) {
        this.plugin = plugin;
        this.karma = karma;
        this.fetcher = fetcher;
    }


    @SubCommand("list")
    @Completion("#empty")
    @Permission("karma.command.list")
    public void saveCommand(CommandSender sender, String[] args) {
        Map<PlayerData, Double> map = new HashMap<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData pData = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
            map.put(pData, pData.getKarmaScore());
        }
        LinkedHashMap<PlayerData, Double> reverseSortedMap = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        for (PlayerData pData : reverseSortedMap.keySet()) {
            sender.sendMessage(fetcher.getMessageString(Message.VIEW_LIST_PLAYER)
                    .replace("<PLAYER>", pData.getPlayer().getName())
                    .replace("<SCORE>", String.valueOf(reverseSortedMap.get(pData)))
                    .replace("<ALIGNMENT>", fetcher.getAlignmentName(pData.getKarmaAlignment())));
        }
    }

}
