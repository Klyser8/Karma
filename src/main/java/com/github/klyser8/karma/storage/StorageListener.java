package com.github.klyser8.karma.storage;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.KarmaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.github.klyser8.karma.api.util.UtilMethods.debugMessage;

public class StorageListener extends StorageHandler implements Listener {

    public StorageListener(KarmaPlugin plugin, KarmaManager karmaManager) {
        super(plugin, karmaManager);
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
