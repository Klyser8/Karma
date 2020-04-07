package com.github.klyser8.karma.storage;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.karma.KarmaAlignment;
import com.github.klyser8.karma.karma.KarmaSource;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
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

import static com.github.klyser8.karma.api.util.GenericMethods.checkVersion;
import static com.github.klyser8.karma.api.util.UtilMethods.*;
import static me.mattstudios.mf.base.components.MfUtil.color;

public class StorageHandler implements Listener {

    private final Map<UUID, PlayerKarma> playerDataMap = new HashMap<>();

    protected KarmaPlugin plugin;
    protected KarmaManager karmaManager;
    public StorageHandler(KarmaPlugin plugin, KarmaManager karmaManager) {
        this.plugin = plugin;
        this.karmaManager = karmaManager;
    }


    /**Will setup the specified player's data.
     *
     * If a player has been observed playing while this plugin was on the server
     * before, the data stored in the PlayerKarma class will be taken from their
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
        PlayerKarma playerKarma;
        if (plugin.getStorageHandler().getPlayerDataMap().containsKey(uuid)) return;
        playerKarma = new PlayerKarma(Objects.requireNonNull(Bukkit.getPlayer(uuid)), plugin);
        if (data.isSet("FirstJoin")) {
            playerKarma.setFirstJoin(data.getBoolean("FirstJoin"));
            playerKarma.setKarmaScore(data.getInt("KarmaScore"), KarmaSource.COMMAND);
            playerKarma.setKarmaAlignment(KarmaAlignment.valueOf(data.getString("KarmaAlignments")), false);
        } else {
            try {
                boolean fileCreated = playerFile.createNewFile();
                debugMessage("Status of data file creation for " + Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName(), fileCreated);
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "(Players) Could not create " + uuid.toString() + ".plr!");
            }
        }
        plugin.getStorageHandler().getPlayerDataMap().put(uuid, playerKarma);
    }


    /**Saves all the 'permanent' variables in all the PlayerKarma class instances
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
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(uuid);
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
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        data.setFirstJoin(false);
        startAggroRunner(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) cancel();
                plugin.getKarmaLimitList().remove(player);
                plugin.getStorageHandler().getPlayerData(player.getUniqueId()).clearRecentKarmaGained();
                debugMessage("Karma limit has been reset for", player.getName());
            }
        }.runTaskTimer(plugin, plugin.getKarmaLimitInterval() * 20, plugin.getKarmaLimitInterval() * 20);
        new BukkitRunnable() {
            int currentSeconds = 0;
            @Override
            public void run() {
                if (!player.isOnline()) cancel();
                currentSeconds++;
                if (currentSeconds == plugin.getPassiveKarmaGainInterval())
                    karmaManager.changeKarmaScore(player, plugin.getPassiveKarmaGainAmount(), KarmaSource.PASSIVE);
            }
        }.runTaskTimer(plugin, 20, 20);
        if (!plugin.isTablistAlignments()) return;
        KarmaEnumFetcher fetcher = new KarmaEnumFetcher(plugin);
        if (plugin.isShowAlignments()) {
            player.setPlayerListName(color(fetcher.getAlignmentName(data.getKarmaAlignment()) + "&r ") + player.getName());
        } else {
            CharSequence color = fetcher.getAlignmentName(data.getKarmaAlignment()).subSequence(0, 2);
            player.setPlayerListName(color(color + player.getName()));
        }
    }


    /**Returns the instance of the Player Data class stored on the map.
     *
     * @param uuid UUID of the player.
     */
    public PlayerKarma getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }



    /**Gets the HashMap which stores all of the players data classes by using
     * their UUIDs as keys.
     */
    public Map<UUID, PlayerKarma> getPlayerDataMap() {
        return playerDataMap;
    }

    public void startAggroRunner(Player player) {
        Random random = new Random();
        PlayerKarma data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        boolean doBeesExist = KarmaPlugin.VERSION.contains("1.15");
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
