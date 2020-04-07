package com.github.klyser8.karma;

import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.api.util.GenericMethods;
import com.github.klyser8.karma.karma.*;
import com.github.klyser8.karma.karma.commands.*;
import com.github.klyser8.karma.lang.LanguageHandler;
import com.github.klyser8.karma.storage.ReloadKarmaCommand;
import com.github.klyser8.karma.storage.StorageHandler;
import com.github.klyser8.karma.storage.StorageListener;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
import com.github.klyser8.karma.karma.KarmaEffectsHandler;
import com.github.klyser8.karma.storage.SaveKarmaCommand;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

import static com.github.klyser8.karma.api.util.GenericMethods.toPrimitiveArray;
import static com.github.klyser8.karma.api.util.UtilMethods.*;
import static me.mattstudios.mf.base.components.MfUtil.color;
import static org.bukkit.Bukkit.getPluginManager;

public final class KarmaPlugin extends JavaPlugin {

    public static int karmaHighLimit, karmaLowLimit;

    private boolean passiveKarmaGainEnabled;
    private double passiveKarmaGainAmount;
    private int passiveKarmaGainInterval;

    private boolean passiveMobKillingEnabled;
    private double passiveMobKillingAmount;

    private boolean monsterKillingEnabled;
    private double monsterKillingAmount;

    private boolean friendlyMobKillingEnabled;
    private double friendlyMobKillingAmount;

    private boolean playerKillingEnabled;
    private boolean playerHittingEnabled;

    private boolean villagerTradingEnabled;
    private double villagerTradingAmount;

    private boolean villagerHittingEnabled;
    private double villagerHittingAmount;

    private boolean entityTamedEnabled;
    private double entityTamedAmount;

    private boolean entityFedEnabled;
    private double entityFedAmount;

    private boolean serverVotedEnabled;
    private double serverVotedAmount;

    private boolean goldenCarrotConsumedEnabled;
    private double goldenCarrotConsumedAmount;

    private boolean placingBlocksEnabled;
    private Map<Material, Double> placedBlocksMap = new HashMap<>();

    private boolean breakingBlocksEnabled;
    private Map<Material, Double> brokenBlocksMap = new HashMap<>();

    private boolean messageSentEnabled;
    private Map<String, Double> karmaWordsMap = new HashMap<>();

    private boolean decreaseMultiplierEnabled;
    private double decreaseMultiplierAmount;

    private boolean increaseMultiplierEnabled;
    private double increaseMultiplierAmount;

    private double karmaTimeLimit;
    private int karmaLimitInterval;

    private boolean notificationSounds;
    private boolean creativeKarma;
    private boolean tablistAlignments;
    private boolean chatAlignments;
    private boolean displayNameAlignments;
    private boolean showAlignments;

    public final static String VERSION = Bukkit.getVersion();
    public static boolean debugging = false;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})

    private int autosaveInterval;

    private String language = "english.yml";

    private LanguageHandler languageHandler;
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
        KarmaManager karmaManager = new KarmaManager();
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        commandManager.getMessageHandler().register("cmd.wrong.usage", sender -> sender.sendMessage(color("&cUnknown command. Type &6/Karma help&c for help.")));
        commandManager.register(new ViewKarmaCommand(this, karmaEnumFetcher));
        commandManager.register(new ReloadKarmaCommand(this, karmaManager, karmaEnumFetcher));
        commandManager.register(new SaveKarmaCommand(this, karmaManager, karmaEnumFetcher));
        commandManager.register(new HelpKarmaCommand(this, karmaManager, karmaEnumFetcher));
        commandManager.register(new AddKarmaCommand(this, karmaManager, karmaEnumFetcher));
        commandManager.register(new RemoveKarmaCommand(this, karmaManager, karmaEnumFetcher));
        commandManager.register(new SetKarmaCommand(this, karmaManager, karmaEnumFetcher));
        commandManager.register(new ClearKarmaCommand(this, karmaManager, karmaEnumFetcher));
        commandManager.register(new ListKarmaCommand(this, karmaManager, karmaEnumFetcher));
        languageHandler = new LanguageHandler(this);
        storageHandler = new StorageHandler(this, karmaManager);
        saveDefaultConfig();
        languageHandler.setup();

        languageHandler.setupLanguage();
        setupPreferences();

        getServer().getPluginManager().registerEvents(new StorageListener(this, karmaManager), this);
        getServer().getPluginManager().registerEvents(new KarmaListener(this), this);
        getServer().getPluginManager().registerEvents(new KarmaEffectsHandler(this), this);
        if (getServer().getPluginManager().getPlugin("Votifier") != null && getServer().getPluginManager().getPlugin("Votifier").isEnabled())
            getServer().getPluginManager().registerEvents(new KarmaVoteListener(this), this);
        //this.getCommand("karma").setExecutor(new KarmaCommands(this, karmaManager));

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

    public LanguageHandler getLanguageHandler() {
        return languageHandler;
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
            karmaPerkMap.put(effect, GenericMethods.toPrimitiveArray(getConfig().getDoubleList("Effects.Positive." + effect).toArray(new Double[]{})));
        }
        for (String effect : getConfig().getConfigurationSection("Effects.Negative").getValues(false).keySet()) {
            if (effect.equalsIgnoreCase("mobs anger")) {
                for (String subEffect : getConfig().getConfigurationSection("Effects.Negative." + effect).getValues(false).keySet()) {
                    karmaRepercussionMap.put(effect + "." + subEffect, GenericMethods.toPrimitiveArray(getConfig().getDoubleList("Effects.Negative." + effect + "." + subEffect).toArray(new Double[]{})));
                }
            } else
                karmaRepercussionMap.put(effect, GenericMethods.toPrimitiveArray(getConfig().getDoubleList("Effects.Negative." + effect).toArray(new Double[]{})));
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
        languageHandler.setupLanguage();
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

    public boolean isPassiveKarmaGainEnabled() {
        return passiveKarmaGainEnabled;
    }

    public double getPassiveKarmaGainAmount() {
        return passiveKarmaGainAmount;
    }

    public int getPassiveKarmaGainInterval() {
        return passiveKarmaGainInterval;
    }

    public boolean isPassiveMobKillingEnabled() {
        return passiveMobKillingEnabled;
    }

    public double getPassiveMobKillingAmount() {
        return passiveMobKillingAmount;
    }

    public boolean isMonsterKillingEnabled() {
        return monsterKillingEnabled;
    }

    public double getMonsterKillingAmount() {
        return monsterKillingAmount;
    }

    public boolean isFriendlyMobKillingEnabled() {
        return friendlyMobKillingEnabled;
    }

    public double getFriendlyMobKillingAmount() {
        return friendlyMobKillingAmount;
    }

    public boolean isPlayerKillingEnabled() {
        return playerKillingEnabled;
    }

    public boolean isPlayerHittingEnabled() {
        return playerHittingEnabled;
    }

    public boolean isVillagerTradingEnabled() {
        return villagerTradingEnabled;
    }

    public double getVillagerTradingAmount() {
        return villagerTradingAmount;
    }

    public boolean isVillagerHittingEnabled() {
        return villagerHittingEnabled;
    }

    public double getVillagerHittingAmount() {
        return villagerHittingAmount;
    }

    public boolean isEntityTamedEnabled() {
        return entityTamedEnabled;
    }

    public double getEntityTamedAmount() {
        return entityTamedAmount;
    }

    public boolean isEntityFedEnabled() {
        return entityFedEnabled;
    }

    public double getEntityFedAmount() {
        return entityFedAmount;
    }

    public boolean isServerVotedEnabled() {
        return serverVotedEnabled;
    }

    public double getServerVotedAmount() {
        return serverVotedAmount;
    }

    public boolean isGoldenCarrotConsumedEnabled() {
        return goldenCarrotConsumedEnabled;
    }

    public double getGoldenCarrotConsumedAmount() {
        return goldenCarrotConsumedAmount;
    }

    public boolean isPlacingBlocksEnabled() {
        return placingBlocksEnabled;
    }

    public boolean isBreakingBlocksEnabled() {
        return breakingBlocksEnabled;
    }

    public boolean isMessageSentEnabled() {
        return messageSentEnabled;
    }

    public boolean isDecreaseMultiplierEnabled() {
        return decreaseMultiplierEnabled;
    }

    public double getDecreaseMultiplierAmount() {
        return decreaseMultiplierAmount;
    }

    public boolean isIncreaseMultiplierEnabled() {
        return increaseMultiplierEnabled;
    }

    public double getIncreaseMultiplierAmount() {
        return increaseMultiplierAmount;
    }

    public double getKarmaTimeLimit() {
        return karmaTimeLimit;
    }

    public int getKarmaLimitInterval() {
        return karmaLimitInterval;
    }

    public boolean isNotificationSounds() {
        return notificationSounds;
    }

    public boolean isCreativeKarma() {
        return creativeKarma;
    }

    public boolean isTablistAlignments() {
        return tablistAlignments;
    }

    public boolean isChatAlignments() {
        return chatAlignments;
    }

    public boolean isDisplayNameAlignments() {
        return displayNameAlignments;
    }

    public boolean isShowAlignments() {
        return showAlignments;
    }
}
