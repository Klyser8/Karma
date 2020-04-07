package com.github.klyser8.karma.karma;

import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.api.KarmaManager;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

import static com.github.klyser8.karma.api.util.UtilMethods.debugMessage;

public class KarmaVoteListener implements Listener {

    private KarmaPlugin plugin;
    public KarmaVoteListener(KarmaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVoteServer(VotifierEvent event) throws IOException {
        Player player = Bukkit.getPlayer(event.getVote().getUsername());
        if (!plugin.isServerVotedEnabled()) return;
        if (player == null) return;
        if (player.isOnline()) {
            new KarmaManager().changeKarmaScore(player, plugin.getServerVotedAmount(), KarmaSource.VOTING);
            debugMessage(player.getName() + " has voted for this server. Karma gained", plugin.getServerVotedAmount());
        } else {
            File playerFile = new File(plugin.getDataFolder() + File.separator + "players", player.getUniqueId().toString() + ".plr");
            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            double karma = playerConfig.getDouble("KarmaScore");
            karma+=plugin.getServerVotedAmount();
            playerConfig.set("KarmaScore", karma);
            playerConfig.save(playerFile);
        }
    }

}

