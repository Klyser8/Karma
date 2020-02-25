package me.klyser8.karma.handlers;
import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.events.KarmaAlignmentChangeEvent;
import me.klyser8.karma.events.KarmaGainEvent;
import me.klyser8.karma.events.KarmaLossEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Merchant;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static me.klyser8.karma.util.UtilMethods.*;


public class KarmaHandler implements Listener {

    private static Map<Player, PassiveKarmaRunnable> karmaRunnableMap;
    protected Karma plugin;
    public KarmaHandler(Karma plugin) {
        this.plugin = plugin;
        karmaRunnableMap = new HashMap<>();
    }


    /**Sets a player's karma score to a specific number.
     *
     * @param player the chosen player.
     * @param amount number to set karma to.
     */
    public void setKarmaScore(Player player, double amount) {
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        if (amount > data.getKarmaScore()) {
            KarmaGainEvent event = new KarmaGainEvent(player, amount - data.getKarmaScore(), data.getKarmaScore(), KarmaSource.COMMAND);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                plugin.getStorageHandler().getPlayerData(player.getUniqueId()).setKarmaScore(amount, event.getSource());
        } else {
            KarmaLossEvent event = new KarmaLossEvent(player, data.getKarmaScore() - amount, data.getKarmaScore(), KarmaSource.COMMAND);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                plugin.getStorageHandler().getPlayerData(player.getUniqueId()).setKarmaScore(amount, event.getSource());
        }
        updateAlignments(player);
    }


    /**Adds an amount to a player's Karma score. Does not
     * accept negative numbers.
     *
     * @param player chosen player.
     * @param amount amount to add.
     * @param source source of the karma.
     */
    public void addKarmaScore(Player player, double amount, KarmaSource source) {
        boolean worldEnabled = !plugin.disabledWorldList.contains(player.getWorld().getName());
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        if (data.getLastSource() == source && source != KarmaSource.COMMAND && source != KarmaSource.VOTING && worldEnabled) {
            if (plugin.percentageDecreaseAmount < 100)
                amount = amount * (1 - (plugin.percentageDecreaseAmount / 100));
            else
                amount = 0;
        }
        KarmaGainEvent event = new KarmaGainEvent(player, amount, data.getKarmaScore(), source);
        Bukkit.getPluginManager().callEvent(event);
        if (amount <= 0)
            event.setCancelled(true);
        if (plugin.getKarmaLimitList().contains(player) && source != KarmaSource.COMMAND && source != KarmaSource.VOTING)
            event.setCancelled(true);
        if (!worldEnabled && source != KarmaSource.COMMAND && source != KarmaSource.VOTING) {
            event.setCancelled(true);
            sendDebugMessage(player.getName() + " did not gain Karma due to the world being disabled.");
        }
        if (!event.isCancelled()) {
            double oldKarma = data.getKarmaScore();
            data.setKarmaScore(oldKarma + event.getAmount(), event.getSource());
            data.setLastSource(event.getSource());
        }
        updateAlignments(player);
    }


    /**Subtracts an amount from a player's karma score.
     * Does not accept negative numbers.
     *
     * @param player chosen player.
     * @param amount amount to subtract.
     * @param source source of the karma.
     */
    public void subtractKarmaScore(Player player, double amount, KarmaSource source) {
        boolean worldEnabled = !plugin.disabledWorldList.contains(player.getWorld().getName());
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        if (source != KarmaSource.COMMAND && source == plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getLastSource() && worldEnabled) {
            if (plugin.percentageIncreaseAmount > 0)
                amount = amount * (1 + (plugin.percentageIncreaseAmount / 100));
            else
                amount = 0;
        }
        KarmaLossEvent event = new KarmaLossEvent(player, amount, data.getKarmaScore(), source);
        Bukkit.getPluginManager().callEvent(event);
        if (amount <= 0)
            event.setCancelled(true);
        if (!worldEnabled && source != KarmaSource.COMMAND) {
            event.setCancelled(true);
            sendDebugMessage(player.getName() + " did not lose Karma due to the world being disabled.");
        }
        if (!event.isCancelled()) {
            double oldKarma = data.getKarmaScore();
            data.setKarmaScore(oldKarma - event.getAmount(), event.getSource());
            data.setLastSource(event.getSource());
        }
        updateAlignments(player);
    }



    /**Resets a player's karma to 0.
     *
     * @param player the chosen player
     */
    public void clearKarmaScore(Player player) {
        plugin.getStorageHandler().getPlayerData(player.getUniqueId()).setKarmaScore(0.0, KarmaSource.COMMAND);
        updateAlignments(player);
    }


    /**Adds or removes Karma from a player's total
     * depending on whether the written amount is
     * positive or negative. If the amount is positive,
     * there will be an increase in the player's karma,
     * if it is negative there will be a decrease.
     *
     * @param player chosen player.
     * @param amount amount to change
     * @param source source of the karma.
     */
    public void changeKarmaScore(Player player, double amount, KarmaSource source) {
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        if (amount < 0) {
            subtractKarmaScore(player, Math.abs(amount), source);
        } else
            addKarmaScore(player, amount, source);
        KarmaAlignment oldAlignment = data.getKarmaAlignment();
        updateAlignments(player);
        if (oldAlignment != plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment()) {
            KarmaAlignmentChangeEvent event = new KarmaAlignmentChangeEvent(player, oldAlignment, plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment());
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    public void updateAlignments(Player player) {
        PlayerData data = Objects.requireNonNull(plugin.getStorageHandler().getPlayerData(player.getUniqueId()));
        KarmaAlignment oldAlignment = data.getKarmaAlignment();
        for (KarmaAlignment alignment : KarmaAlignment.values()) {
            //sendDebugMessage(plugin.getKarmaAlignmentThresholds().get(alignment));
            int lowBoundary = alignment.getLowBoundary();
            int highBoundary = alignment.getHighBoundary();
            if (lowBoundary <= data.getKarmaScore() && data.getKarmaScore() <= highBoundary) {
                if (oldAlignment != alignment) {
                    ArrayList<String> commands = plugin.getAlignmentCommandsMap().get(alignment);
                    for (String command : commands) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("<PLAYER>", player.getName()));
                    }
                }
                data.setKarmaAlignment(alignment, true);
                if (plugin.tablistAlignments) {
                    if (player.getPlayerListName().equalsIgnoreCase(player.getDisplayName())) {
                        player.setPlayerListName(color(alignment.getName() + "&r ") + player.getDisplayName());
                    } else {
                        if (plugin.showAlignments) {
                            player.setPlayerListName(color(alignment.getName() + "&r ") + player.getDisplayName());
                        } else {
                            CharSequence color = alignment.getName().subSequence(0, 2);
                            player.setPlayerListName(color(color + player.getDisplayName()));
                        }
                    }
                }
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.passiveKarmaGainEnabled)
                    new PassiveKarmaRunnable(event.getPlayer()).runTaskTimer(plugin, plugin.passiveKarmaGainTimer * 20, plugin.passiveKarmaGainTimer * 20);
                if (plugin.tablistAlignments)
                    updateAlignments(event.getPlayer());
            }
        }.runTaskLater(plugin, 5);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (plugin.passiveKarmaGainEnabled) {
            //sendDebugMessage(karmaRunnableMap.get(event.getPlayer()) + "");
            karmaRunnableMap.get(event.getPlayer()).cancel();
            karmaRunnableMap.remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            if (Karma.version.contains("1.15")) {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.CAT ||
                        event.getEntity().getType() == EntityType.DOLPHIN || event.getEntity().getType() == EntityType.PARROT ||
                        event.getEntity().getType() == EntityType.BEE) {
                    if (plugin.friendlyMobKillingEnabled) {
                        changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                    }
                }
            } else if (Karma.version.contains("1.14")) {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.CAT || event.getEntity().getType() == EntityType.DOLPHIN || event.getEntity().getType() == EntityType.PARROT) {
                    if (plugin.friendlyMobKillingEnabled) {
                        changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                    }
                }
            } else if (Karma.version.contains("1.13")) {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.DOLPHIN || event.getEntity().getType() == EntityType.PARROT || event.getEntity().getType() == EntityType.OCELOT) {
                    if (plugin.friendlyMobKillingEnabled) {
                        changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                    }
                }
            } else if (Karma.version.contains("1.12")) {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.PARROT || event.getEntity().getType() == EntityType.OCELOT) {
                    if (plugin.friendlyMobKillingEnabled) {
                        changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                    }
                }
            } else {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.OCELOT) {
                    if (plugin.friendlyMobKillingEnabled) {
                        changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                    }
                }
            }
            if (event.getEntity() instanceof Animals) {
                if (plugin.passiveMobKillingEnabled)
                    changeKarmaScore(event.getEntity().getKiller(), plugin.passiveMobKillingAmount, KarmaSource.MOB);
            } else if (event.getEntity() instanceof Monster) {
                if (plugin.monsterKillingEnabled) {
                    changeKarmaScore(event.getEntity().getKiller(), plugin.monsterKillingAmount, KarmaSource.MOB);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            if (plugin.playerKillingEnabled) {
                changeKarmaScore(event.getEntity().getKiller(), plugin.getPlayerAlignment(event.getEntity()).getKillPenalty(), KarmaSource.PLAYER);
            }
        }
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (plugin.villagerTradingEnabled) {
            if (event.getCurrentItem() == null)
                return;
            if (event.getClickedInventory().getType().equals(InventoryType.MERCHANT)) {
                if (event.getSlot() == 2 && !event.getCurrentItem().getType().equals(Material.AIR)) {
                    changeKarmaScore((Player) event.getWhoClicked(), plugin.villagerTradingAmount, KarmaSource.TRADE);
                }
            }
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (plugin.villagerHittingEnabled && (event.getEntity() instanceof Merchant)) {
            if (getAttackerFromEntityDamageEvent(event) != null) {
                Player player = null;
                if (event.getDamager() instanceof Projectile && !((Entity) ((Projectile) event.getDamager()).getShooter()).hasMetadata("NPC")) {
                    player = (Player) event.getDamager();
                } else if (event.getDamager() instanceof Player) {
                    player = (Player) event.getDamager();
                }
                if (player != null) {
                    changeKarmaScore(player, plugin.villagerHittingAmount, KarmaSource.ATTACK);
                }
            }
        }
        if (plugin.playerHittingEnabled && event.getEntity() instanceof Player && getAttackerFromEntityDamageEvent(event) != null) {
            Player victim = (Player) event.getEntity();
            Player attacker = getAttackerFromEntityDamageEvent(event);
            if (attacker != null) {
                changeKarmaScore(attacker, plugin.getPlayerAlignment(victim).getHitPenalty(), KarmaSource.PLAYER);
            }
        }
    }

    @EventHandler
    public void onAnimalTame(EntityTameEvent event) {
        if (plugin.entityTamedEnabled) {
            changeKarmaScore((Player) event.getOwner(), plugin.entityTamedAmount, KarmaSource.TAME);
        }
    }

    @EventHandler
    public void onEntityFed(PlayerInteractEntityEvent event) {
        if (!Karma.version.contains("1.8")) {
            if (plugin.entityFedEnabled) {
                if (event.getRightClicked() instanceof Animals && (!(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SADDLE)) ||
                        !(event.getPlayer().getInventory().getItemInOffHand().getType() == Material.SADDLE)) {
                    int oldAmountMain = event.getPlayer().getInventory().getItemInMainHand().getAmount();
                    int oldAmountOff = event.getPlayer().getInventory().getItemInOffHand().getAmount();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int newAmountMain = event.getPlayer().getInventory().getItemInMainHand().getAmount();
                            int newAmountOff = event.getPlayer().getInventory().getItemInOffHand().getAmount();
                            if (oldAmountMain > newAmountMain)
                                changeKarmaScore(event.getPlayer(), plugin.entityFedAmount, KarmaSource.FEED);
                            else if (oldAmountOff > newAmountOff)
                                changeKarmaScore(event.getPlayer(), plugin.entityFedAmount / 2, KarmaSource.FEED);
                        }
                    }.runTaskLater(plugin, 1);
                }
            }
        } else {
            if (plugin.entityFedEnabled) {
                if (event.getRightClicked() instanceof Animals && (!(event.getPlayer().getInventory().getItemInHand().getType() == Material.SADDLE))) {
                    int oldAmount = event.getPlayer().getInventory().getItemInHand().getAmount();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int newAmount = event.getPlayer().getInventory().getItemInHand().getAmount();
                            if (oldAmount > newAmount) {
                                //Material item = event.getPlayer().getInventory().getItemInHand().getType();
                                changeKarmaScore(event.getPlayer(), plugin.entityFedAmount, KarmaSource.FEED);
                            }
                        }
                    }.runTaskLater(plugin, 1);
                }
            }
        }
    }

    @EventHandler
    public void onFoodEaten(PlayerItemConsumeEvent event) {
        if (plugin.goldenCarrotConsumedEnabled) {
            if (event.getItem().getType() == Material.GOLDEN_CARROT) {
                changeKarmaScore(event.getPlayer(), plugin.goldenCarrotConsumedAmount, KarmaSource.FOOD);
            }
        }
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        if (plugin.placingBlocksEnabled) {
            if (plugin.getPlacedBlocksMap().containsKey(event.getBlockPlaced().getType())) {
                changeKarmaScore(event.getPlayer(), plugin.getPlacedBlocksMap().get(event.getBlockPlaced().getType()), KarmaSource.BLOCK);
            }
        }
    }

    @EventHandler
    public void onBlockDestroyed(BlockBreakEvent event) {
        if (plugin.breakingBlocksEnabled) {
            if (plugin.getBrokenBlocksMap().containsKey(event.getBlock().getType())) {
                changeKarmaScore(event.getPlayer(), plugin.getBrokenBlocksMap().get(event.getBlock().getType()), KarmaSource.BLOCK);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.chatAlignments) {
            if (plugin.showAlignments) {
                String alignment = plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).getKarmaAlignment().getName();
                event.setFormat(color(alignment + "&r" + " <" + event.getPlayer().getDisplayName() + "> ") + event.getMessage());
            } else {
                CharSequence color = plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).getKarmaAlignment().getName().subSequence(0, 2);
                event.setFormat(event.getFormat().replace("%1$s", color(color + event.getPlayer().getName() + "&r")));
                event.setFormat(event.getFormat().replace(event.getPlayer().getName(), color(color + event.getPlayer().getName() + "&r")));
            }
        }
        if (plugin.messageSentEnabled) {
            for (String word : plugin.getKarmaWordsMap().keySet()) {
                if (event.getMessage().toUpperCase().contains(word.toUpperCase())) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> changeKarmaScore(event.getPlayer(), plugin.getKarmaWordsMap().get(word), KarmaSource.CHAT));
                }
            }
        }
    }

    public class PassiveKarmaRunnable extends BukkitRunnable {

        Player player;
        public PassiveKarmaRunnable(Player player) {
            this.player = player;
            karmaRunnableMap.put(player, this);
        }

        @Override
        public void run() {
            changeKarmaScore(player, plugin.passiveKarmaGainAmount, KarmaSource.PASSIVE);
        }
    }
}
