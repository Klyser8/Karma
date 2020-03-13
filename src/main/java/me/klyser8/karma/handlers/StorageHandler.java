package me.klyser8.karma.handlers;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static me.klyser8.karma.util.UtilMethods.checkVersion;
import static me.klyser8.karma.util.UtilMethods.debugMessage;

public class StorageHandler implements Listener {

    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    protected Karma plugin;
    protected KarmaHandler karmaHandler;
    public StorageHandler(Karma plugin, KarmaHandler karmaHandler) {
        this.plugin = plugin;
        this.karmaHandler = karmaHandler;
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
        if (plugin.getStorageHandler().getPlayerDataMap().containsKey(uuid)) return;
        playerData = new PlayerData(Objects.requireNonNull(Bukkit.getPlayer(uuid)), plugin);
        if (data.isSet("FirstJoin")) {
            playerData.setFirstJoin(data.getBoolean("FirstJoin"));
            playerData.setKarmaScore(data.getInt("KarmaScore"), KarmaSource.COMMAND);
            playerData.setKarmaAlignment(KarmaAlignment.valueOf(data.getString("KarmaAlignments")), false);
        } else {
            try {
                boolean fileCreated = playerFile.createNewFile();
                debugMessage("Status of data file creation for " + Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName(), fileCreated);
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "(Players) Could not create " + uuid.toString() + ".plr!");
            }
        }
        plugin.getStorageHandler().getPlayerDataMap().put(uuid, playerData);
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


    /**
     * "Readies up" the player, by starting all needed Runnables.
     *
     * @param player player to set up.
     */
    public void setupPlayer(Player player) {
        setupPlayerData(player.getUniqueId());
        plugin.getStorageHandler().getPlayerData(player.getUniqueId()).setFirstJoin(false);
        startAggroRunner(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) cancel();
                plugin.getKarmaLimitList().remove(player);
                plugin.getStorageHandler().getPlayerData(player.getUniqueId()).clearRecentKarmaGained();
                debugMessage("Karma limit has been reset for", player.getName());
            }
        }.runTaskTimer(plugin, plugin.karmaLimitInterval * 20, plugin.karmaLimitInterval * 20);
        new BukkitRunnable() {
            int currentSeconds = 0;
            @Override
            public void run() {
                if (!player.isOnline()) cancel();
                currentSeconds++;
                if (currentSeconds == plugin.passiveKarmaGainInterval)
                    karmaHandler.changeKarmaScore(player, plugin.passiveKarmaGainAmount, KarmaSource.PASSIVE);
            }
        }.runTaskTimer(plugin, 20, 20);
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

    public void startAggroRunner(Player player) {
        Random random = new Random();
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        boolean doBeesExist = Karma.VERSION.contains("1.15");
        new BukkitRunnable() {
            double beeChance;
            double wolfChance;
            double pigmanChance;

            private KarmaEnumFetcher fetcher = new KarmaEnumFetcher(plugin);

            @Override
            public void run() {
                if (data.getKarmaScore() > fetcher.getAlignmentHighBoundary(KarmaAlignment.RUDE)) return;
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
                KarmaAlignment alignment = data.getKarmaAlignment();
                switch (alignment) {
                    case RUDE:
                        beeChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Bee")[0];
                        wolfChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Wolf")[0];
                        pigmanChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Pigman")[0];
                        break;
                    case MEAN:
                        beeChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Bee")[1];
                        wolfChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Wolf")[1];
                        pigmanChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Pigman")[1];
                        break;
                    case VILE:
                        beeChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Bee")[2];
                        wolfChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Wolf")[2];
                        pigmanChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Pigman")[2];
                        break;
                    case EVIL:
                        beeChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Bee")[3];
                        wolfChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Wolf")[3];
                        pigmanChance = plugin.getKarmaRepercussionMap().get("Mobs Anger.Pigman")[3];
                        break;
                    default:
                        beeChance = 0;
                        wolfChance = 0;
                        pigmanChance = 0;
                }
                for (Entity entity : player.getNearbyEntities(10, 5, 10)) {
                    if (entity instanceof Wolf && random.nextInt(100) < wolfChance) {
                        Wolf wolf = (Wolf) entity;
                        if (!wolf.isTamed()) {
                            wolf.setAngry(true);
                            wolf.setTarget(player);
                        }
                    }
                    if (entity instanceof PigZombie && random.nextInt(100) < pigmanChance) {
                        ((PigZombie) entity).setAngry(true);
                        if (checkVersion("1.15", "1.14", "1.13"))
                            entity.getWorld().playSound(((PigZombie) entity).getEyeLocation(), Sound.ENTITY_ZOMBIE_PIGMAN_ANGRY, 1.5F, 2.0f);
                        else
                            entity.getWorld().playSound(((PigZombie) entity).getEyeLocation(), Sound.valueOf("ZOMBIE_PIG_ANGRY"), 1.5F, 1.5f);
                    }
                    if (doBeesExist && entity instanceof Bee && random.nextInt(100) < beeChance) {
                        ((Bee) entity).setAnger(600);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}
