package me.klyser8.karma.handlers;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.Command;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.Keyword;
import me.klyser8.karma.enums.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;

import static me.klyser8.karma.util.UtilMethods.color;
import static me.klyser8.karma.util.UtilMethods.sendDebugMessage;

public class SettingsHandler {

    private Karma plugin;

    public SettingsHandler(Karma plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration lang;
    private File langFile;

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdir();
        }
        File playersFolder = new File(plugin.getDataFolder(), "players");
        if(!playersFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            playersFolder.mkdir();
        }

        File langFolder = new File(plugin.getDataFolder(), "lang");
        //langFile = new File(plugin.getDataFolder(), "lang/english.yml");
        //lang = YamlConfiguration.loadConfiguration(langFile);
        if (!langFolder.exists()) {
            plugin.saveResource("lang" + File.separator + "english.yml", false);
            plugin.saveResource("lang" + File.separator + "italian.yml", false);
            plugin.saveResource("lang" + File.separator + "simplified_chinese.yml", false);
            plugin.saveResource("lang" + File.separator + "vietnamese.yml", false);
        } else {
            if (!new File(plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + "english.yml").exists()) {
                plugin.saveResource("lang" + File.separator + "english.yml", false);
            }
            if (!new File(plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + "italian.yml").exists()) {
                plugin.saveResource("lang" + File.separator + "italian.yml", false);
            }
            if (!new File(plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + "simplified_chinese.yml").exists()) {
                plugin.saveResource("lang" + File.separator + "simplified_chinese.yml", false);
            }
            if (!new File(plugin.getDataFolder().getPath() + File.separator + "lang" + File.separator + "vietnamese.yml").exists()) {
                plugin.saveResource("lang" + File.separator + "vietnamese.yml", false);
            }
        }
    }

    public void setupLanguage() {
        String languageFile;
        plugin.setLanguage(plugin.getConfig().getString("Language").toLowerCase());
        if (plugin.getLanguage().equalsIgnoreCase("italian")) {
            languageFile = "italian.yml";
        } else if (plugin.getLanguage().equalsIgnoreCase("simplified chinese")) {
            languageFile = "simplified_chinese.yml";
        } else if (plugin.getLanguage().equalsIgnoreCase("vietnamese")) {
            languageFile = "vietnamese.yml";
        } else {
            languageFile = "english.yml";
        }
        langFile = new File(plugin.getDataFolder() + File.separator + "lang", languageFile);
        lang = YamlConfiguration.loadConfiguration(langFile);
        Command.loadCommands(lang);
        Message.loadMessages(lang);
        Keyword.loadKeywords(lang);
        //Bukkit.getLogger().info("Lang File: " + langFile + ", Lang config: " + lang);
        sendDebugMessage("Language test", color(Message.KARMA_RELOAD.getMessage()));
    }

    public FileConfiguration getLang() {
        if (lang == null) {
            Bukkit.getServer().getLogger().severe("Could not retrieve " + plugin.getLanguage() + ".yml!");
            return null;
        } else {
            return lang;
        }
    }

    public PluginDescriptionFile getDesc() {
        return plugin.getDescription();
    }

}