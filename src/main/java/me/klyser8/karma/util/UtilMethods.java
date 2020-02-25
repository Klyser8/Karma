package me.klyser8.karma.util;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.handlers.PlayerData;
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


    /**Turns minecraft color codes, such as &5, into ChatColor enums.
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
    public static void sendDebugMessage(String name, String value) {
        if (Karma.debugging)
            Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + name + ": " + ChatColor.DARK_PURPLE + value);
    }


    /**If the parameter 'Debugging' is set to true, this method sends a
     * message to the console.
     *
     * @param string value to check
     */
    public static void sendDebugMessage(String string) {
        if (Karma.debugging)
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_PURPLE + string);
    }


    /**Returns true if the parsed string contains only integers.
     *
     * @param string any string
     */
    public static boolean isInteger(String string) {
        return ((string != null && string.matches("[0-9]+")));
    }



    /**Returns true if the parsed string contains only doubles.
     *
     * @param string any string
     */
    public static boolean isDouble(String string) {
        return ((string != null && string.matches("[0-9.]+")));
    }


    /**Returns true if the parsed string contains only NEGATIVE doubles.
     *
     * @param string any string
     */
    public static boolean isNegativeDouble(String string) {
        return ((string != null && string.matches("[0-9.-]+")));
    }


    /**@param maximum the highest number to check for
     * @param minimum the lowest number to check for
     * @param value number to check
     *
     * @return true if value is between maximum and minimum.
     */
    public static boolean isBetween(int maximum, int minimum, int value) {
        return minimum > maximum ? value > maximum && value < minimum : value > minimum && value < maximum;
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

    public static void startAggroRunner(Karma plugin, Player player) {
        Random random = new Random();
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        boolean doBeesExist = Karma.version.contains("1.15");
        if (!plugin.getAggroRunnables().containsKey(player)) {
            plugin.getAggroRunnables().put(player, new BukkitRunnable() {
                double beeChance;
                double wolfChance;
                double pigmanChance;

                @Override
                public void run() {
                    if (data.getKarmaScore() < KarmaAlignment.RUDE.getHighBoundary()) {
                        KarmaAlignment alignment = data.getKarmaAlignment();
                        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
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
                            sendDebugMessage("Bee Chance", beeChance + "");
                            sendDebugMessage("Wolf Chance", wolfChance + "");
                            sendDebugMessage("Pigman Chance", pigmanChance + "");
                            for (Entity entity : player.getNearbyEntities(10, 5, 10)) {
                                if (entity instanceof Wolf && random.nextInt(100) < wolfChance) {
                                    if (!((Wolf) entity).isTamed()) {
                                        ((Wolf) entity).setAngry(true);
                                        ((Wolf) entity).setTarget(player);
                                    }
                                }
                                if (entity instanceof PigZombie && random.nextInt(100) < pigmanChance) {
                                    ((PigZombie) entity).setAngry(true);
                                    if (Karma.version.contains("1.13") || Karma.version.contains("1.14") || Karma.version.contains("1.15"))
                                        entity.getWorld().playSound(((PigZombie) entity).getEyeLocation(), Sound.ENTITY_ZOMBIE_PIGMAN_ANGRY, 1.0F, 2.0f);
                                    else
                                        entity.getWorld().playSound(((PigZombie) entity).getEyeLocation(), Sound.valueOf("ZOMBIE_PIG_ANGRY"), 1.0F, 2.0f);
                                }
                                if (doBeesExist && entity instanceof Bee && random.nextInt(100) < beeChance) {
                                    ((Bee) entity).setAnger(600);
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, 0, 300));
        }
    }
}
