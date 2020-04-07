package com.github.klyser8.karma.api.util;

import com.github.klyser8.karma.KarmaPlugin;
import org.bukkit.Bukkit;

import static me.mattstudios.mf.base.components.MfUtil.color;

@SuppressWarnings("unused")
public class UtilMethods {

    /**If the parameter 'Debugging' is set to true, this method sends a
     * message to the console.
     *
     * @param name name of the value you want to check
     * @param value value to check
     */
    public static void debugMessage(String name, Object value) {
        if (KarmaPlugin.debugging)
            Bukkit.getConsoleSender().sendMessage(color("&d[Karma]&f " + name + ":&d " + value));
    }


    /**If the parameter 'Debugging' is set to true, this method sends a
     * message to the console.
     *
     * @param value value to check
     */
    public static void debugMessage(Object value) {
        if (KarmaPlugin.debugging)
            Bukkit.getConsoleSender().sendMessage(color("&d[Karma]&f " + value));
    }
}
