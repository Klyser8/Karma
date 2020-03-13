package me.klyser8.karma.handlers;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.Message;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static me.klyser8.karma.util.UtilMethods.*;

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
        List<String> languages = Arrays.asList("english", "italian", "spanish", "russian", "vietnamese", "simplified_chinese");
        for (String language : languages) {
            if (!new File(plugin.getDataFolder().getPath() + File.separator + "lang/" + language + ".yml").exists())
                plugin.saveResource("lang/" + language + ".yml", false);
        }
    }

    public void setupLanguage() {
        String languageFile;
        plugin.setLanguage(plugin.getConfig().getString("Language").toLowerCase());
        switch (plugin.getLanguage().toLowerCase()) {
            default: languageFile = "english.yml";
                break;
            case "italian": languageFile = "italian.yml";
                break;
            case "spanish": languageFile = "spanish.yml";
                break;
            case "russian": languageFile = "russian.yml";
                break;
            case "vietnamese": languageFile = "vietnamese.yml";
                break;
            case "simplified chinese": languageFile = "simplified_chinese.yml";
                break;
        }
        langFile = new File(plugin.getDataFolder() + File.separator + "lang", languageFile);
        lang = YamlConfiguration.loadConfiguration(langFile);
        debugMessage("Testing " + plugin.getLanguage(), new KarmaEnumFetcher(plugin).getMessageString(Message.RELOAD));
    }

    public FileConfiguration getLang() {
        if (lang == null) {
            Bukkit.getServer().getLogger().severe("Could not retrieve " + plugin.getLanguage() + ".yml!");
            return null;
        } else {
            return lang;
        }
    }

}