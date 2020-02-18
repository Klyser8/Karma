package me.klyser8.karma.handlers;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.KarmaSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static me.klyser8.karma.util.UtilMethods.sendDebugMessage;

public class StorageHandler implements Listener {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    private Karma plugin;
    public StorageHandler(Karma plugin) {
        this.plugin = plugin;
    }


    /**Will setup the specified player's data.
     *
     * If a player has been observed playing while this plugin was on the server
     * before, the data stored in the PlayerData class will be taken from their
     * respective data files.
     *
     * Additionally, if the plugin has observed the player joining the server prior
     * to it restarting, the data will be loaded from 'playerDataMap'.
     *
     * @param uuid UUID of the player
     */
    public void setupPlayerData(UUID uuid) {
        File playerFile = new File(plugin.getDataFolder() + File.separator + "players", uuid.toString() + ".plr");
        FileConfiguration data = YamlConfiguration.loadConfiguration(playerFile);
        PlayerData playerData;
        if (!plugin.getStorageHandler().getPlayerDataMap().containsKey(uuid)) {
            playerData = new PlayerData(Objects.requireNonNull(Bukkit.getPlayer(uuid)), plugin);
            if (data.isSet("FirstJoin")) {
                playerData.setFirstJoin(data.getBoolean("FirstJoin"));
                playerData.setKarmaScore(data.getInt("KarmaScore"), KarmaSource.COMMAND);
                playerData.setKarmaAlignment(KarmaAlignment.valueOf(data.getString("KarmaAlignments")), false);
            } else {
                try {
                    boolean fileCreated = playerFile.createNewFile();
                    sendDebugMessage(uuid.toString(), "Was data file created? - " + fileCreated);
                } catch (IOException e) {
                    Bukkit.getServer().getLogger().severe(ChatColor.RED + "(Players) Could not create " + uuid.toString() + ".plr!");
                }
            }
            plugin.getStorageHandler().getPlayerDataMap().put(uuid, playerData);
        }
    }


    /**Saves all the 'permanent' variables in all the PlayerData class instances
     * in the plugin onto unique .ply files. If a player does not have a personal
     * .ply file yet, one is created to then be used right away to store the data.
     */
    public void savePlayerData(UUID uuid) {
        File playerFile = new File(plugin.getDataFolder() + File.separator + "players", uuid.toString() + ".plr");
        if (!playerFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                playerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        PlayerData data = plugin.getStorageHandler().getPlayerData(uuid);
        try {
            playerConfig.set("FirstJoin", data.isFirstJoin());
            playerConfig.set("KarmaScore", data.getKarmaScore());
            playerConfig.set("KarmaAlignments", data.getKarmaAlignment().toString());
            playerConfig.save(playerFile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "(Players) Could not save " + uuid.toString() + ".plr!");
        }
    }


    /**Returns the instance of the Player Data class stored on the map.
     *
     * @param uuid UUID of the player.
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }



    /**Gets the HashMap which stores all of the players data classes by using
     * their UUIDs as keys.
     */
    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                setupPlayerData(event.getPlayer().getUniqueId());
                plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).setFirstJoin(false);
            }
        }.runTaskLater(plugin, 1);
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getKarmaLimitList().remove(event.getPlayer());
                plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).clearRecentKarmaGained();
                sendDebugMessage("Recent Karma has been reset for", event.getPlayer().getName());
                if (!event.getPlayer().isOnline())
                    cancel();
            }
        }.runTaskTimer(plugin, plugin.karmaLimitTimer * 20, plugin.karmaLimitTimer * 20);
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
