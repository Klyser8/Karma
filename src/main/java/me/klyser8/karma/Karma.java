package me.klyser8.karma;

import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.handlers.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static me.klyser8.karma.util.UtilMethods.*;
import static org.bukkit.Bukkit.getPluginManager;

public final class Karma extends JavaPlugin {

    public static int karmaHighLimit, karmaLowLimit;

    public boolean passiveKarmaGainEnabled;
    public double passiveKarmaGainAmount;
    public int passiveKarmaGainTimer;

    public boolean passiveMobKillingEnabled;
    public double passiveMobKillingAmount;

    public boolean monsterKillingEnabled;
    public double monsterKillingAmount;

    public boolean friendlyMobKillingEnabled;
    public double friendlyMobKillingAmount;

    public boolean playerKillingEnabled;
    public boolean playerHittingEnabled;

    public boolean villagerTradingEnabled;
    public double villagerTradingAmount;

    public boolean villagerHittingEnabled;
    public double villagerHittingAmount;

    public boolean entityTamedEnabled;
    public double entityTamedAmount;

    public boolean entityFedEnabled;
    public double entityFedAmount;

    public boolean serverVotedEnabled;
    public double serverVotedAmount;

    public boolean goldenCarrotConsumedEnabled;
    public double goldenCarrotConsumedAmount;

    public boolean placingBlocksEnabled;
    private Map<Material, Double> placedBlocksMap;

    public boolean breakingBlocksEnabled;
    private Map<Material, Double> brokenBlocksMap;

    public boolean messageSentEnabled;
    private HashMap<String, Double> karmaWordsMap;

    public boolean percentageDecreaseEnabled;
    public double percentageDecreaseAmount;

    public boolean percentageIncreaseEnabled;
    public double percentageIncreaseAmount;

    public double karmaTimeLimit;
    public int karmaLimitTimer;

    public boolean notificationSounds;
    public boolean creativeKarma;
    public boolean tablistAlignments;
    public boolean chatAlignments;
    public boolean displayNameAlignments;
    public boolean showAlignments;

    public final static String version = Bukkit.getVersion();
    public static boolean debugging = false;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static Economy econ = null;

    private int autosaveTimer;

    private String language = "english.yml";
    public String messageFormat;

    private SettingsHandler settings;
    private StorageHandler storageHandler;

    private Map<String, double[]> karmaRepercussionMap;
    private Map<String, double[]> karmaPerkMap;

    private Map<KarmaAlignment, ArrayList<String>> alignmentCommands;
    private Map<Player, BukkitTask> aggroRunnables;

    private List<Player> karmaLimitList;
    public List<String> disabledWorldList;

    @Override
    public void onEnable() {
        settings = new SettingsHandler(this);
        storageHandler = new StorageHandler(this);
        KarmaHandler karmaHandler = new KarmaHandler(this);
        saveDefaultConfig();
        settings.setup();

        placedBlocksMap = new HashMap<>();
        brokenBlocksMap = new HashMap<>();
        karmaWordsMap = new HashMap<>();
        karmaLimitList = new ArrayList<>();
        disabledWorldList = new ArrayList<>();
        karmaRepercussionMap = new HashMap<>();
        karmaPerkMap = new HashMap<>();
        alignmentCommands = new HashMap<>();
        aggroRunnables = new HashMap<>();

        settings.setupLanguage();
        setupPreferences();

        getServer().getPluginManager().registerEvents(new StorageHandler(this), this);
        getServer().getPluginManager().registerEvents(new KarmaHandler(this), this);
        getServer().getPluginManager().registerEvents(new KarmaEffectsHandler(this), this);
        this.getCommand("karma").setExecutor(new KarmaCommands(this, karmaHandler));

        //this.getCommand("karma").setExecutor(new KarmaCommands(this, new KarmaHandler(this)));
        if (Bukkit.getOnlinePlayers().size() > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                storageHandler.setupPlayerData(player.getUniqueId());
                if (tablistAlignments) {
                    karmaHandler.updateAlignments(player);
                }
                if (passiveKarmaGainEnabled) {
                    new KarmaHandler(this).new PassiveKarmaRunnable(player).runTaskTimer(this, passiveKarmaGainTimer * 20, passiveKarmaGainTimer * 20);
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        getKarmaLimitList().remove(player);
                        getStorageHandler().getPlayerData(player.getUniqueId()).clearRecentKarmaGained();
                        sendDebugMessage("Recent Karma has been reset for", player.getName());
                    }
                }.runTaskTimer(this, karmaLimitTimer * 20, karmaLimitTimer * 20);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        startAggroRunner(Karma.this, player);
                    }
                }.runTaskLater(this, 20);
            }

        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    getStorageHandler().savePlayerData(player.getUniqueId());
                }
            }
        }.runTaskTimer(this, autosaveTimer * 20, autosaveTimer * 20);
        if (getPluginManager().getPlugin("PlaceholderAPI") != null)
            Bukkit.getConsoleSender().sendMessage(color("[Karma] &dSince you are using PlaceholderAPI, you are being informed that Karma has got a PAPI expansion you can download! You do that by either downloading it from the eCloud (If it's available there), or by downloading it here: &rhttps://api.extendedclip.com/expansions/karma-papi-expansion/"));
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getStorageHandler().savePlayerData(player.getUniqueId());
        }
    }

    public StorageHandler getStorageHandler() {
        return storageHandler;
    }

    @SuppressWarnings("unused")
    public SettingsHandler getSettings() {
        return settings;
    }

    public void setupPreferences() {
        debugging = getConfig().getBoolean("Debugging");
        notificationSounds = getConfig().getBoolean("Notification Sounds");
        creativeKarma = getConfig().getBoolean("Creative Mode Karma");
        autosaveTimer = getConfig().getInt("Autosave Timer");
        language = getConfig().getString("Language").toLowerCase();
        chatAlignments = getConfig().getBoolean("Chat Alignment");
        tablistAlignments = getConfig().getBoolean("Tablist Alignment");
        displayNameAlignments = getConfig().getBoolean("Display Name Alignment");
        showAlignments = getConfig().getBoolean("Show Alignments");
        messageFormat = getConfig().getString("Message Format");

        passiveKarmaGainEnabled = getConfig().getBoolean("Passive Karma Gain.Enabled");
        passiveKarmaGainAmount = getConfig().getDouble("Passive Karma Gain.Amount");
        passiveKarmaGainTimer = getConfig().getInt("Passive Karma Gain.Timer");

        passiveMobKillingEnabled = getConfig().getBoolean("Passive Mob Killing.Enabled");
        passiveMobKillingAmount = getConfig().getDouble("Passive Mob Killing.Amount");

        monsterKillingEnabled = getConfig().getBoolean("Monster Killing.Enabled");
        monsterKillingAmount = getConfig().getDouble("Monster Killing.Amount");

        friendlyMobKillingEnabled = getConfig().getBoolean("Friendly Mob Killing.Enabled");
        friendlyMobKillingAmount = getConfig().getDouble("Friendly Mob Killing.Amount");

        playerKillingEnabled = getConfig().getBoolean("Player Killing.Enabled");
        playerHittingEnabled = getConfig().getBoolean("Player Hitting.Enabled");

        villagerTradingEnabled = getConfig().getBoolean("Villager Trading.Enabled");
        villagerTradingAmount = getConfig().getDouble("Villager Trading.Amount");

        villagerHittingEnabled = getConfig().getBoolean("Villager Hitting.Enabled");
        villagerHittingAmount = getConfig().getDouble("Villager Hitting.Amount");

        entityTamedEnabled = getConfig().getBoolean("Entity Tamed.Enabled");
        entityTamedAmount = getConfig().getDouble("Entity Tamed.Amount");

        entityFedEnabled = getConfig().getBoolean("Entity Fed.Enabled");
        entityFedAmount = getConfig().getDouble("Entity Fed.Amount");

        if (getServer().getPluginManager().getPlugin("Votifier") != null) {
            serverVotedEnabled = getConfig().getBoolean("Server Voted.Enabled");
        } else {
            serverVotedEnabled = false;
            sendDebugMessage("[Karma]", "Disabled server voting rewards due to missing Dependency (Votifier).");
        }
        serverVotedAmount = getConfig().getDouble("Server Voted.Amount");

        goldenCarrotConsumedEnabled = getConfig().getBoolean("Golden Carrot Consumed.Enabled");
        goldenCarrotConsumedAmount = getConfig().getDouble("Golden Carrot Consumed.Amount");

        placingBlocksEnabled = getConfig().getBoolean("Placing Blocks.Enabled");
        breakingBlocksEnabled = getConfig().getBoolean("Breaking Blocks.Enabled");

        messageSentEnabled = getConfig().getBoolean("Message Sent.Enabled");

        karmaTimeLimit = getConfig().getDouble("Karma Limit.Max Amount");
        karmaLimitTimer = getConfig().getInt("Karma Limit.Timer");

        percentageDecreaseEnabled = getConfig().getBoolean("Percentage Decrease.Enabled");
        percentageDecreaseAmount = getConfig().getDouble("Percentage Decrease.Amount");

        percentageIncreaseEnabled = getConfig().getBoolean("Percentage Increase.Enabled");
        percentageIncreaseAmount = getConfig().getDouble("Percentage Increase.Amount");

        for (String string : getConfig().getConfigurationSection("Placing Blocks.Blocks").getValues(false).keySet()) {
            if (Material.valueOf(string).isBlock())
                placedBlocksMap.put(Material.valueOf(string), getConfig().getDouble("Placing Blocks.Blocks." + string));
        }

        for (String string : getConfig().getConfigurationSection("Breaking Blocks.Blocks").getValues(false).keySet()) {
            if (Material.valueOf(string).isBlock())
                brokenBlocksMap.put(Material.valueOf(string), getConfig().getDouble("Breaking Blocks.Blocks." + string));
        }

        for (String word : getConfig().getConfigurationSection("Message Sent.Words").getValues(false).keySet()) {
            karmaWordsMap.put(word, getConfig().getDouble("Message Sent.Words." + word));
        }

        for (String effect : getConfig().getConfigurationSection("Effects.Positive").getValues(false).keySet()) {
            karmaPerkMap.put(effect, toPrimitiveArray(getConfig().getDoubleList("Effects.Positive." + effect).toArray(new Double[]{})));
        }

        for (String effect : getConfig().getConfigurationSection("Effects.Negative").getValues(false).keySet()) {
            if (effect.equalsIgnoreCase("mobs anger")) {
                for (String subEffect : getConfig().getConfigurationSection("Effects.Negative." + effect).getValues(false).keySet()) {
                    karmaRepercussionMap.put(effect + "." + subEffect, toPrimitiveArray(getConfig().getDoubleList("Effects.Negative." + effect + "." + subEffect).toArray(new Double[]{})));
                }
            } else
                karmaRepercussionMap.put(effect, toPrimitiveArray(getConfig().getDoubleList("Effects.Negative." + effect).toArray(new Double[]{})));
        }

        disabledWorldList = getConfig().getStringList("Disabled Worlds");

        for (KarmaAlignment alignment : KarmaAlignment.values()) {
            alignmentCommands.put(alignment, (ArrayList<String>) getConfig().getStringList("Alignment Commands." + alignment.toString()));
        }

        karmaHighLimit = KarmaAlignment.BEST.getHighBoundary();
        karmaLowLimit = KarmaAlignment.EVIL.getLowBoundary();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void reload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getStorageHandler().savePlayerData(player.getUniqueId());
        }
        reloadConfig();
        setupPreferences();
        settings.setupLanguage();
        KarmaAlignment.loadAlignments(this, settings.getLang(), getConfig());
    }


    /**
     * Returns a map which stores all the blocks which affect a player's
     * Karma Score when placed. This is obtained from the plugin's config
     * file
     *
     * @return the placedBlocks map.
     */
    public Map<Material, Double> getPlacedBlocksMap() {
        return placedBlocksMap;
    }


    /**
     * Returns a map which stores all the blocks which affect a player's
     * Karma Score when broken. This is obtained from the plugin's config
     * file
     *
     * @return the brokenBlocks map.
     */
    public Map<Material, Double> getBrokenBlocksMap() {
        return brokenBlocksMap;
    }


    /**
     * Returns a list which stores all the online players who have reached
     * the limit of Karma Points which can be gained for each time period
     * written in the config file.
     *
     * @return the karmaLimitList.
     */
    public List<Player> getKarmaLimitList() {
        return karmaLimitList;
    }


    /**
     * Returns a map which stores the current active Karma perks (Positive effects).
     *
     * @return the karmaPerkMap.
     */
    public Map<String, double[]> getKarmaPerkMap() {
        return karmaPerkMap;
    }


    /**
     * Returns a map which stores the current active Karma repercussions (Negative Effects).
     *
     * @return the karmaRepercussionMap.
     */
    public Map<String, double[]> getKarmaRepercussionMap() {
        return karmaRepercussionMap;
    }


    public Map<KarmaAlignment, ArrayList<String>> getAlignmentCommandsMap() {
        return alignmentCommands;
    }

    /**
     * Returns a map containing all words which will alter the sender's Karma.
     *
     * @return the karmaWordsMap.
     */
    public Map<String, Double> getKarmaWordsMap() {
        return karmaWordsMap;
    }


    /**
     * Returns the specified player's Karma alignment.
     *
     * @param player Player who's alignment wants to be obtained.
     * @return The player's alignment.
     */
    @SuppressWarnings("unused")
    public KarmaAlignment getPlayerAlignment(Player player) {
        return getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment();
    }

    /**
     * Returns the specified player's Karma score.
     *
     * @param player Player who's score wants to be obtained.
     * @return The player's Karma Score.
     */
    @SuppressWarnings("unused")
    public double getPlayerKarma(Player player) {
        return getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaScore();
    }

    /**
     * Vault implementation setup method
     */

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public Map<Player, BukkitTask> getAggroRunnables() {
        return aggroRunnables;
    }
}
