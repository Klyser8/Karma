package me.klyser8.karma.listeners;

import me.klyser8.karma.Karma;
import me.klyser8.karma.handlers.KarmaHandler;
import me.klyser8.karma.handlers.StorageHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static me.klyser8.karma.util.UtilMethods.debugMessage;

public class StorageListener extends StorageHandler implements Listener {

    public StorageListener(Karma plugin, KarmaHandler karmaHandler) {
        super(plugin, karmaHandler);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setupPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        savePlayerData(event.getPlayer().getUniqueId());
    }


}
