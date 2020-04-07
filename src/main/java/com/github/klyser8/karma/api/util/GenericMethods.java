package com.github.klyser8.karma.api.util;

import com.github.klyser8.karma.KarmaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenericMethods {

    /**Returns true if the parsed string contains only numbers, letters
     * or '-' and '_' symbols.
     *
     * @param string any string.
     */
    public static boolean isAlphanumeric(String string) {
        return ((string != null && string.matches("[a-zA-Z0-9_-]+")));
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
    public static Player randomPlayer() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int index = new Random().nextInt(players.size());

        return players.get(index);
    }


    /**
     * Gets the attacker from a EntityDamageByEntityEvent. Returns null
     * if the attacker is null.
     *
     * @param event EntityDamageByEntityEvent
     * @return attacker (player)
     */
    public static Player getAttackerFromEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            return (Player) event.getDamager();
        }
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
            return (Player) ((Projectile) event.getDamager()).getShooter();
        }
        return null;
    }


    /**
     * Checks if the current version of the server is the one specified.
     *
     * @param versions versions to compare to the server version.
     * @return true if one of written versions is the server's version.
     */
    public static boolean checkVersion(final String... versions) {
        if (versions == null) {
            return false;
        }

        for (int i = 0; i < versions.length; i++) {
            if (KarmaPlugin.VERSION.contains(versions[i])) return true;
        }

        return false;
    }


}
