package me.klyser8.karma.util;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.storage.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

@SuppressWarnings("unused")
public class UtilMethods {


    /**Returns true if the parsed string contains only numbers, letters
     * or '-' and '_' symbols.
     *
     * @param string any string.
     */
    public static boolean isAlphanumeric(String string) {
        return ((string != null && string.matches("[a-zA-Z0-9_-]+")));
    }


    /**Turns Minecraft color codes, such as &5, into ChatColor enums.
     *
     * @param msg any string.
     */
    public static String color(String msg) {
        StringBuilder coloredMsg = new StringBuilder();
        for(int i = 0; i < msg.length(); i++)
        {
            if(msg.charAt(i) == '&')
                coloredMsg.append('§');
            else
                coloredMsg.append(msg.charAt(i));
        }
        return coloredMsg.toString();
    }


    /**If the parameter 'Debugging' is set to true, this method sends a
     * message to the console.
     *
     * @param name name of the value you want to check
     * @param value value to check
     */
    public static void debugMessage(String name, Object value) {
        if (Karma.debugging)
            Bukkit.getConsoleSender().sendMessage(color("&d[Karma]&f " + name + ":&d " + value));
    }


    /**If the parameter 'Debugging' is set to true, this method sends a
     * message to the console.
     *
     * @param value value to check
     */
    public static void debugMessage(Object value) {
        if (Karma.debugging)
            Bukkit.getConsoleSender().sendMessage(color("&d[Karma]&f " + value));
    }


    /**Returns true if the parsed string contains only integers.
     *
     * @param string any string
     */
    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }
    }



    /**Returns true if the parsed string contains only doubles.
     *
     * @param string any string
     */
    public static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
            return true;
        }
        catch(NumberFormatException e) {
            return false;
        }

    }


    /**
     * @param x number to check for
     * @param min lowest number
     * @param max highest number
     * @return whether X is between min and max
     */
    public static boolean isBetween(double x, double min, double max) {
        return x > min && x < max;
    }


    /**
     * Converts an Integer array to an int array.
     *
     * @param intObjects an Integer[] array.
     * @return an int[] array
     */
    public static int[] toPrimitiveArray(Integer[] intObjects) {
        int[] ints = new int[intObjects.length];
        for (int index = 0; index < intObjects.length; index++) {
            if (intObjects[index] == null)
                ints[index] = 0;
            else
                ints[index] = intObjects[index];
        }
        return ints;
    }


    /**
     * Converts a Double array to an double array.
     *
     * @param doubleObjects an Double[] array.
     * @return an double[] array
     */
    public static double[] toPrimitiveArray(Double[] doubleObjects) {
        double[] doubles = new double[doubleObjects.length];
        for (int index = 0; index < doubleObjects.length; index++) {
            if (doubleObjects[index] == null)
                doubles[index] = 0;
            else
                doubles[index] = doubleObjects[index];
        }
        return doubles;
    }


    /** Returns a random online player
     *
     * @return random online player
     */
    public static Player randomPlayer()
    {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        Random r = new Random();

        int n = r.nextInt((players.length));

        return players[n];
    }


    public static Player getAttackerFromEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            return (Player) ((Projectile) event.getDamager()).getShooter();
        }
        return null;
    }

    public static boolean checkVersion(final String... versions) {
        if (versions == null) {
            return false;
        }

        for (int i = 0; i < versions.length; i++) {
            if (Karma.VERSION.contains(versions[i])) return true;
        }

        return false;
    }
}
