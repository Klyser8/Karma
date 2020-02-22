package me.klyser8.karma.events;

import com.vexsoftware.votifier.model.VotifierEvent;
import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.handlers.KarmaHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

import static me.klyser8.karma.util.UtilMethods.color;
import static me.klyser8.karma.util.UtilMethods.sendDebugMessage;

public class KarmaVoteListener implements Listener {

    private Karma plugin;
    public KarmaVoteListener(Karma plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVoteServer(VotifierEvent event) throws IOException {
        if (plugin.serverVotedEnabled) {
            Player player = Bukkit.getPlayer(event.getVote().getUsername());
            if (player != null) {
                if (player.isOnline()) {
                    new KarmaHandler(plugin).changeKarmaScore(player, plugin.serverVotedAmount, KarmaSource.VOTING);
                    sendDebugMessage(color(player.getName() + " has voted for the server, gaining &f" + plugin.serverVotedAmount + " &dKarma"));
                } else {
                    File playerFile = new File(plugin.getDataFolder() + File.separator + "players", player.getUniqueId().toString() + ".plr");
                    FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
                    double karma = playerConfig.getDouble("KarmaScore");
                    karma+=plugin.serverVotedAmount;
                    playerConfig.set("KarmaScore", karma);
                    playerConfig.save(playerFile);
                    sendDebugMessage(color(player.getName() + " has voted for the server, adding &f" + plugin.serverVotedAmount + " &dKarma to their save file."));
                }
            }
        }
    }

}

