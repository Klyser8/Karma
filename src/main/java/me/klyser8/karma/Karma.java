package me.klyser8.karma;

import me.klyser8.karma.commands.*;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.events.KarmaVoteListener;
import me.klyser8.karma.handlers.*;
import me.klyser8.karma.listeners.KarmaEffectsListener;
import me.klyser8.karma.listeners.KarmaListener;
import me.klyser8.karma.listeners.StorageListener;
import me.mattstudios.mf.base.CommandManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

import static me.klyser8.karma.util.UtilMethods.*;
import static org.bukkit.Bukkit.getPluginManager;

public final class Karma extends JavaPlugin {

    public static int karmaHighLimit, karmaLowLimit;

    public boolean passiveKarmaGainEnabled;
    public double passiveKarmaGainAmount;
    public int passiveKarmaGainInterval;

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
    private Map<Material, Double> placedBlocksMap = new HashMap<>();

    public boolean breakingBlocksEnabled;
    private Map<Material, Double> brokenBlocksMap = new HashMap<>();

    public boolean messageSentEnabled;
    private Map<String, Double> karmaWordsMap = new HashMap<>();

    public boolean decreaseMultiplierEnabled;
    public double decreaseMultiplierAmount;

    public boolean increaseMultiplierEnabled;
    public double increaseMultiplierAmount;

    public double karmaTimeLimit;
    public int karmaLimitInterval;

    public boolean notificationSounds;
    public boolean creativeKarma;
    public boolean tablistAlignments;
    public boolean chatAlignments;
    public boolean displayNameAlignments;
    public boolean showAlignments;

    public final static String VERSION = Bukkit.getVersion();
    public static boolean debugging = false;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static Economy econ = null;

    private int autosaveInterval;

    private String language = "english.yml";

    private SettingsHandler settings;
    private StorageHandler storageHandler;
    private KarmaEnumFetcher karmaEnumFetcher;

    private Map<String, double[]> karmaRepercussionMap = new HashMap<>();
    private Map<String, double[]> karmaPerkMap = new HashMap<>();

    private Map<KarmaAlignment, List<String>> alignmentCommands = new HashMap<>();

    private List<Player> karmaLimitList = new ArrayList<>();
    public List<String> disabledWorldList;

    @Override
    public void onEnable() {
        CommandManager commandManager = new CommandManager(this);
        karmaEnumFetcher = new KarmaEnumFetcher(this);
        KarmaHandler karmaHandler = new KarmaHandler(this);
        commandManager.getMessageHandler().register("cmd.wrong.usage", sender -> sender.sendMessage(color("&cUnknown command. Type &6/Karma help&c for help.")));
        commandManager.register(new ViewKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new ReloadKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new SaveKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new HelpKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new AddKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new RemoveKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new SetKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new ClearKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        commandManager.register(new ListKarmaCommand(this, karmaHandler, karmaEnumFetcher));
        settings = new SettingsHandler(this);
        storageHandler = new StorageHandler(this, karmaHandler);
        saveDefaultConfig();
        settings.setup();

        settings.setupLanguage();
        setupPreferences();

        getServer().getPluginManager().registerEvents(new StorageListener(this, karmaHandler), this);
        getServer().getPluginManager().registerEvents(new KarmaListener(this), this);
        getServer().getPluginManager().registerEvents(new KarmaEffectsListener(this), this);
        if (getServer().getPluginManager().getPlugin("Votifier") != null && getServer().getPluginManager().getPlugin("Votifier").isEnabled())
            getServer().getPluginManager().registerEvents(new KarmaVoteListener(this), this);
        //this.getCommand("karma").setExecutor(new KarmaCommands(this, karmaHandler));

        if (Bukkit.getOnlinePlayers().size() > 0) {
            setupOnlinePlayers();
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    getStorageHandler().savePlayerData(player.getUniqueId());
                }
            }, autosaveInterval * 20, autosaveInterval * 20);
        }

        Plugin plugin = getPluginManager().getPlugin("PlaceholderAPI");
        if (plugin == null) return;
        for (File child : plugin.getDataFolder().listFiles()) {
            if (!child.getName().toLowerCase().contains("karma")) continue;
            Bukkit.getConsoleSender().sendMessage(color("[Karma] &dSince you are using PlaceholderAPI, you are being informed that Karma has got a PAPI expansion you can download!" +
                    " You do that by either downloading it from the eCloud (If it's available there), or by downloading it here: " +
                    "&rhttps://api.extendedclip.com/expansions/karma-papi-expansion/"));
            break;
        }
    }

    /**
     * Sets up players who are currently online
     *
     */
    private void setupOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            getStorageHandler().setupPlayer(player);
        }
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
        debugMessage("Notification sounds enabled", false);
        creativeKarma = getConfig().getBoolean("Creative Mode Karma");
        debugMessage("Creative mode Karma enabled", creativeKarma);
        autosaveInterval = getConfig().getInt("Autosave Interval");
        debugMessage("Auto-save interval", autosaveInterval);
        //noinspection ConstantConditions
        language = getConfig().getString("Language").toLowerCase();
        debugMessage("Current Language", language);
        chatAlignments = getConfig().getBoolean("Chat Alignment");
        debugMessage("Chat alignments enabled", chatAlignments);
        tablistAlignments = getConfig().getBoolean("Tablist Alignment");
        debugMessage("Tab list alignments enabled", tablistAlignments);
        displayNameAlignments = getConfig().getBoolean("Display Name Alignment");
        debugMessage("Display name alignments enabled", false);
        showAlignments = getConfig().getBoolean("Show Alignments");
        debugMessage("Visible alignments enabled", showAlignments);

        passiveKarmaGainEnabled = getConfig().getBoolean("Passive Karma Gain.Enabled");
        debugMessage("Passive Karma gain enabled", passiveKarmaGainEnabled);
        passiveKarmaGainAmount = getConfig().getDouble("Passive Karma Gain.Amount");
        debugMessage("Passive Karma gain amount", passiveKarmaGainAmount);
        passiveKarmaGainInterval = getConfig().getInt("Passive Karma Gain.Interval");
        debugMessage("Passive Karma gain interval", passiveKarmaGainInterval);

        passiveMobKillingEnabled = getConfig().getBoolean("Passive Mob Killing.Enabled");
        debugMessage("Karma change on passive mob kill enabled", passiveMobKillingEnabled);
        passiveMobKillingAmount = getConfig().getDouble("Passive Mob Killing.Amount");
        debugMessage("Karma change on passive mob kill amount", passiveMobKillingAmount);

        monsterKillingEnabled = getConfig().getBoolean("Monster Killing.Enabled");
        debugMessage("Karma change on monster kill enabled", monsterKillingEnabled);
        monsterKillingAmount = getConfig().getDouble("Monster Killing.Amount");
        debugMessage("Karma change on passive mob kill amount", monsterKillingAmount);

        friendlyMobKillingEnabled = getConfig().getBoolean("Friendly Mob Killing.Enabled");
        debugMessage("Karma change on friendly mob kill enabled", friendlyMobKillingEnabled);
        friendlyMobKillingAmount = getConfig().getDouble("Friendly Mob Killing.Amount");
        debugMessage("Karma change on passive mob kill amount", friendlyMobKillingAmount);

        playerKillingEnabled = getConfig().getBoolean("Player Killing.Enabled");
        debugMessage("Karma change on player kill", playerKillingEnabled);
        playerHittingEnabled = getConfig().getBoolean("Player Hitting.Enabled");
        debugMessage("Karma change on player hit", playerHittingEnabled);

        villagerTradingEnabled = getConfig().getBoolean("Villager Trading.Enabled");
        debugMessage("Karma change on successful trade enabled", villagerTradingEnabled);
        villagerTradingAmount = getConfig().getDouble("Villager Trading.Amount");
        debugMessage("Karma change on successful trade amount", villagerTradingAmount);

        villagerHittingEnabled = getConfig().getBoolean("Villager Hitting.Enabled");
        debugMessage("Karma change on villager hit enabled", villagerHittingEnabled);
        villagerHittingAmount = getConfig().getDouble("Villager Hitting.Amount");
        debugMessage("Karma change on villager hit amount", villagerHittingAmount);

        entityTamedEnabled = getConfig().getBoolean("Entity Tamed.Enabled");
        debugMessage("Karma change on mob taming enabled", entityTamedEnabled);
        entityTamedAmount = getConfig().getDouble("Entity Tamed.Amount");
        debugMessage("Karma change on mob taming amount", entityTamedAmount);

        entityFedEnabled = getConfig().getBoolean("Entity Fed.Enabled");
        debugMessage("Karma change on mob feeding enabled", entityFedEnabled);
        entityFedAmount = getConfig().getDouble("Entity Fed.Amount");
        debugMessage("Karma change on mob feeding amount", entityFedAmount);

        if (getServer().getPluginManager().getPlugin("Votifier") != null) {
            serverVotedEnabled = getConfig().getBoolean("Server Voted.Enabled");
        } else {
            serverVotedEnabled = false;
        }
        debugMessage("Karma change on server vote enabled", serverVotedEnabled);
        serverVotedAmount = getConfig().getDouble("Server Voted.Amount");
        debugMessage("Karma change on server vote amount", serverVotedAmount);

        goldenCarrotConsumedEnabled = getConfig().getBoolean("Golden Carrot Consumed.Enabled");
        debugMessage("Karma change on golden carrot consumption enabled", goldenCarrotConsumedEnabled);
        goldenCarrotConsumedAmount = getConfig().getDouble("Golden Carrot Consumed.Amount");
        debugMessage("Karma change on golden carrot consumption amount", goldenCarrotConsumedAmount);

        placingBlocksEnabled = getConfig().getBoolean("Placing Blocks.Enabled");
        debugMessage("Karma change on blocks placed enabled", placingBlocksEnabled);
        if (placingBlocksEnabled) {
            for (String string : getConfig().getConfigurationSection("Placing Blocks.Blocks").getValues(false).keySet()) {
                if (Material.valueOf(string).isBlock()) {
                    placedBlocksMap.put(Material.valueOf(string), getConfig().getDouble("Placing Blocks.Blocks." + string));
                    debugMessage("- " + string);
                }
            }
        }

        breakingBlocksEnabled = getConfig().getBoolean("Breaking Blocks.Enabled");
        debugMessage("Karma change on blocks destroyed enabled", breakingBlocksEnabled);

        if (breakingBlocksEnabled) {
            for (String string : getConfig().getConfigurationSection("Breaking Blocks.Blocks").getValues(false).keySet()) {
                if (Material.valueOf(string).isBlock()) {
                    brokenBlocksMap.put(Material.valueOf(string), getConfig().getDouble("Breaking Blocks.Blocks." + string));
                    debugMessage("- " + string);
                }
            }
        }

        messageSentEnabled = getConfig().getBoolean("Message Sent.Enabled");
        debugMessage("Karma change on message sent enabled", messageSentEnabled);
        if (messageSentEnabled) {
            for (String word : getConfig().getConfigurationSection("Message Sent.Words").getValues(false).keySet()) {
                karmaWordsMap.put(word, getConfig().getDouble("Message Sent.Words." + word));
                debugMessage("- " + word);
            }
        }

        karmaTimeLimit = getConfig().getDouble("Karma Limit.Max Amount");
        karmaLimitInterval = getConfig().getInt("Karma Limit.Interval");
        debugMessage("Amount of Karma a player can gain every " + karmaLimitInterval + " seconds", karmaTimeLimit);

        decreaseMultiplierEnabled = getConfig().getBoolean("Decrease Multiplier.Enabled");
        debugMessage("Change in Karma gained upon gaining points from the same source repeatedly enabled", decreaseMultiplierEnabled);
        decreaseMultiplierAmount = getConfig().getDouble("Decrease Multiplier.Amount");
        debugMessage("Change in Karma gained upon gaining points from the same source repeatedly percent", decreaseMultiplierAmount + "%");

        increaseMultiplierEnabled = getConfig().getBoolean("Increase Multiplier.Enabled");
        debugMessage("Change in Karma lost upon losing points from the same source repeatedly enabled", increaseMultiplierEnabled);
        increaseMultiplierAmount = getConfig().getDouble("Increase Multiplier.Amount");
        debugMessage("Change in Karma lost upon losing points from the same source repeatedly amount", increaseMultiplierAmount + "%");

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
        for (KarmaAlignment alignment : KarmaAlignment.values()) {
            alignmentCommands.put(alignment, getConfig().getStringList("Alignment Commands." + alignment.toString()));
        }

        karmaHighLimit = karmaEnumFetcher.getAlignmentHighBoundary(KarmaAlignment.BEST);
        debugMessage("Karma Upper Limit", karmaHighLimit);
        karmaLowLimit = karmaEnumFetcher.getAlignmentLowBoundary(KarmaAlignment.EVIL);
        debugMessage("Karma Lower Limit", karmaLowLimit);

        disabledWorldList = getConfig().getStringList("Disabled Worlds");
        debugMessage("Disabled Worlds", disabledWorldList.toString());
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


    public Map<KarmaAlignment, List<String>> getAlignmentCommandsMap() {
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

}
