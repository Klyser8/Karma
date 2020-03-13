package me.klyser8.karma.listeners;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.handlers.KarmaEnumFetcher;
import me.klyser8.karma.handlers.KarmaHandler;
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

import java.util.HashMap;
import java.util.Map;

import static me.klyser8.karma.util.UtilMethods.*;

public class KarmaListener extends KarmaHandler implements Listener {

    private KarmaEnumFetcher fetcher;
    public KarmaListener(Karma plugin) {
        super(plugin);
        fetcher = new KarmaEnumFetcher(plugin);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        if (plugin.friendlyMobKillingEnabled) {
            //if (Karma.VERSION.contains("1.15")) {
                if (event.getEntity() instanceof Wolf || event.getEntity() instanceof Cat || event.getEntity() instanceof Dolphin ||
                        event.getEntity() instanceof Parrot || event.getEntity() instanceof Bee) {
                    changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                    return;
                }
            /*} else if (Karma.VERSION.contains("1.14")) {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.CAT || event.getEntity().getType() == EntityType.DOLPHIN || event.getEntity().getType() == EntityType.PARROT) {
                    changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                }
            } else if (Karma.VERSION.contains("1.13")) {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.DOLPHIN || event.getEntity().getType() == EntityType.PARROT || event.getEntity().getType() == EntityType.OCELOT) {
                    changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                }
            } else if (Karma.VERSION.contains("1.12")) {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.PARROT || event.getEntity().getType() == EntityType.OCELOT) {
                    changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                }
            } else {
                if (event.getEntity().getType() == EntityType.WOLF || event.getEntity().getType() == EntityType.OCELOT) {
                    changeKarmaScore(event.getEntity().getKiller(), plugin.friendlyMobKillingAmount, KarmaSource.MOB);
                }
            }*/
        }
        if (event.getEntity() instanceof Animals) {
            if (!plugin.passiveMobKillingEnabled) return;
            changeKarmaScore(event.getEntity().getKiller(), plugin.passiveMobKillingAmount, KarmaSource.MOB);
        } else if (event.getEntity() instanceof Monster) {
            if (!plugin.monsterKillingEnabled) return;
            changeKarmaScore(event.getEntity().getKiller(), plugin.monsterKillingAmount, KarmaSource.MOB);
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null || !plugin.playerKillingEnabled) return;
        changeKarmaScore(event.getEntity().getKiller(), fetcher.getAlignmentKillPenalty(plugin.getPlayerAlignment(event.getEntity())), KarmaSource.PLAYER);
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (!plugin.villagerTradingEnabled) return;
        if (event.getCurrentItem() == null || event.getClickedInventory().getType().equals(InventoryType.MERCHANT)) return;
        if (event.getSlot() != 2 && event.getCurrentItem().getType().equals(Material.AIR)) return;
        changeKarmaScore((Player) event.getWhoClicked(), plugin.villagerTradingAmount, KarmaSource.TRADE);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        Player attacker = getAttackerFromEntityDamageEvent(event);
        if (attacker == null) return;
        if (plugin.villagerHittingEnabled && (event.getEntity() instanceof Merchant)) {
            Player player = null;
            if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player &&
            !((Entity) ((Projectile) event.getDamager()).getShooter()).hasMetadata("NPC")) {
                player = (Player) ((Projectile) event.getDamager()).getShooter();
            } else if (event.getDamager() instanceof Player) {
                player = (Player) event.getDamager();
            }
            if (player == null) return;
            changeKarmaScore(player, plugin.villagerHittingAmount, KarmaSource.ATTACK);
        } else if (plugin.playerHittingEnabled && event.getEntity() instanceof Player && getAttackerFromEntityDamageEvent(event) != null) {
            Player victim = (Player) event.getEntity();
            changeKarmaScore(attacker, fetcher.getAlignmentHitPenalty(plugin.getPlayerAlignment(victim)), KarmaSource.PLAYER);
        }
    }

    @EventHandler
    public void onAnimalTame(EntityTameEvent event) {
        if (!plugin.entityTamedEnabled) return;
        changeKarmaScore((Player) event.getOwner(), plugin.entityTamedAmount, KarmaSource.TAME);
    }

    @EventHandler
    public void onEntityFed(PlayerInteractEntityEvent event) {
        if (!plugin.entityFedEnabled || !(event.getRightClicked() instanceof Animals)) return;
        if (!checkVersion("1.8")) {
            if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SADDLE ||
                    event.getPlayer().getInventory().getItemInOffHand().getType() == Material.SADDLE) return;
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
        } else {
            if (event.getPlayer().getInventory().getItemInHand().getType() == Material.SADDLE) return;
            int oldAmount = event.getPlayer().getInventory().getItemInHand().getAmount();
            new BukkitRunnable() {
                @Override
                public void run() {
                    int newAmount = event.getPlayer().getInventory().getItemInHand().getAmount();
                    if (oldAmount == newAmount) return;
                    changeKarmaScore(event.getPlayer(), plugin.entityFedAmount, KarmaSource.FEED);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    @EventHandler
    public void onFoodEaten(PlayerItemConsumeEvent event) {
        if (!plugin.goldenCarrotConsumedEnabled || event.getItem().getType() != Material.GOLDEN_CARROT) return;
        changeKarmaScore(event.getPlayer(), plugin.goldenCarrotConsumedAmount, KarmaSource.FOOD);
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        if (!plugin.placingBlocksEnabled || !plugin.getPlacedBlocksMap().containsKey(event.getBlockPlaced().getType())) return;
        changeKarmaScore(event.getPlayer(), plugin.getPlacedBlocksMap().get(event.getBlockPlaced().getType()), KarmaSource.BLOCK);
    }

    @EventHandler
    public void onBlockDestroyed(BlockBreakEvent event) {
        if (!plugin.breakingBlocksEnabled || !plugin.getBrokenBlocksMap().containsKey(event.getBlock().getType())) return;
        changeKarmaScore(event.getPlayer(), plugin.getBrokenBlocksMap().get(event.getBlock().getType()), KarmaSource.BLOCK);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.chatAlignments) {
            if (plugin.showAlignments) {
                String alignment = plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).getKarmaAlignment().getDefaultName();
                event.setFormat(color(alignment + "&r" + " <" + event.getPlayer().getDisplayName() + "> ") + event.getMessage());
            } else {
                CharSequence color = plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).getKarmaAlignment().getDefaultName().subSequence(0, 2);
                event.setFormat(event.getFormat().replace("%1$s", color(color + event.getPlayer().getName() + "&r")));
                event.setFormat(event.getFormat().replace(event.getPlayer().getName(), color(color + event.getPlayer().getName() + "&r")));
            }
        }
        if (!plugin.messageSentEnabled) return;
        for (String word : plugin.getKarmaWordsMap().keySet()) {
            if (!event.getMessage().toUpperCase().contains(word.toUpperCase())) continue;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> changeKarmaScore(event.getPlayer(), plugin.getKarmaWordsMap().get(word), KarmaSource.CHAT));
        }
    }
}
