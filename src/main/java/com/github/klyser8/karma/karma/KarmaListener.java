package com.github.klyser8.karma.karma;

import com.github.klyser8.karma.KarmaPlugin;
import com.github.klyser8.karma.api.KarmaManager;
import com.github.klyser8.karma.api.util.GenericMethods;
import com.github.klyser8.karma.lang.KarmaEnumFetcher;
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

import static me.mattstudios.mf.base.components.MfUtil.color;

public class KarmaListener extends KarmaManager implements Listener {

    private KarmaEnumFetcher fetcher;
    public KarmaListener(KarmaPlugin plugin) {
        super();
        fetcher = new KarmaEnumFetcher(plugin);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        if (plugin.isFriendlyMobKillingEnabled()) {
            //if (Karma.VERSION.contains("1.15")) {
                if (event.getEntity() instanceof Tameable ||
                        ((KarmaPlugin.VERSION.contains("1.13") || KarmaPlugin.VERSION.contains("1.14") || KarmaPlugin.VERSION.contains("1.15")) && event.getEntity() instanceof Dolphin) ||
                        (KarmaPlugin.VERSION.contains("1.15") &&event.getEntity() instanceof Bee)) {
                    changeKarmaScore(event.getEntity().getKiller(), plugin.getFriendlyMobKillingAmount(), KarmaSource.MOB);
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
            if (!plugin.isPassiveMobKillingEnabled()) return;
            changeKarmaScore(event.getEntity().getKiller(), plugin.getPassiveMobKillingAmount(), KarmaSource.MOB);
        } else if (event.getEntity() instanceof Monster) {
            if (!plugin.isMonsterKillingEnabled()) return;
            changeKarmaScore(event.getEntity().getKiller(), plugin.getMonsterKillingAmount(), KarmaSource.MOB);
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null || !plugin.isPlayerKillingEnabled()) return;
        changeKarmaScore(event.getEntity().getKiller(), fetcher.getAlignmentKillPenalty(plugin.getPlayerAlignment(event.getEntity())), KarmaSource.PLAYER);
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (!plugin.isVillagerTradingEnabled()) return;
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().getType().equals(InventoryType.MERCHANT)) return;
        if (event.getSlot() != 2) return;
        if (event.getClickedInventory().getItem(2) == null) return;
        changeKarmaScore((Player) event.getWhoClicked(), plugin.getVillagerTradingAmount(), KarmaSource.TRADE);
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        Player attacker = GenericMethods.getAttackerFromEntityDamageEvent(event);
        if (attacker == null) return;
        if (plugin.isVillagerHittingEnabled() && (event.getEntity() instanceof Merchant)) {
            Player player = null;
            if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player &&
            !(((Projectile) event.getDamager()).getShooter() instanceof NPC)) {
                player = (Player) ((Projectile) event.getDamager()).getShooter();
            } else if (event.getDamager() instanceof Player) {
                player = (Player) event.getDamager();
            }
            if (player == null) return;
            changeKarmaScore(player, plugin.getVillagerHittingAmount(), KarmaSource.ATTACK);
        } else if (plugin.isPlayerHittingEnabled() && event.getEntity() instanceof Player && GenericMethods.getAttackerFromEntityDamageEvent(event) != null) {
            Player victim = (Player) event.getEntity();
            changeKarmaScore(attacker, fetcher.getAlignmentHitPenalty(plugin.getPlayerAlignment(victim)), KarmaSource.PLAYER);
        }
    }

    @EventHandler
    public void onAnimalTame(EntityTameEvent event) {
        if (!plugin.isEntityTamedEnabled()) return;
        changeKarmaScore((Player) event.getOwner(), plugin.getEntityTamedAmount(), KarmaSource.TAME);
    }

    @EventHandler
    public void onEntityFed(PlayerInteractEntityEvent event) {
        if (!plugin.isEntityFedEnabled() || !(event.getRightClicked() instanceof Animals)) return;
        if (!GenericMethods.checkVersion("1.8")) {
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
                        changeKarmaScore(event.getPlayer(), plugin.getEntityFedAmount(), KarmaSource.FEED);
                    else if (oldAmountOff > newAmountOff)
                        changeKarmaScore(event.getPlayer(), plugin.getEntityFedAmount() / 2, KarmaSource.FEED);
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
                    changeKarmaScore(event.getPlayer(), plugin.getEntityFedAmount(), KarmaSource.FEED);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    @EventHandler
    public void onFoodEaten(PlayerItemConsumeEvent event) {
        if (!plugin.isGoldenCarrotConsumedEnabled() || event.getItem().getType() != Material.GOLDEN_CARROT) return;
        changeKarmaScore(event.getPlayer(), plugin.getGoldenCarrotConsumedAmount(), KarmaSource.FOOD);
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        if (!plugin.isPlacingBlocksEnabled() || !plugin.getPlacedBlocksMap().containsKey(event.getBlockPlaced().getType())) return;
        changeKarmaScore(event.getPlayer(), plugin.getPlacedBlocksMap().get(event.getBlockPlaced().getType()), KarmaSource.BLOCK);
    }

    @EventHandler
    public void onBlockDestroyed(BlockBreakEvent event) {
        if (!plugin.isBreakingBlocksEnabled() || !plugin.getBrokenBlocksMap().containsKey(event.getBlock().getType())) return;
        changeKarmaScore(event.getPlayer(), plugin.getBrokenBlocksMap().get(event.getBlock().getType()), KarmaSource.BLOCK);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.isChatAlignments()) {
            KarmaEnumFetcher fetcher = new KarmaEnumFetcher(plugin);
            if (plugin.isShowAlignments()) {
                String alignment = fetcher.getAlignmentName(plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).getKarmaAlignment());
                event.setFormat(color(alignment + "&r" + " <" + event.getPlayer().getDisplayName() + "> ") + event.getMessage());
            } else {
                CharSequence color = fetcher.getAlignmentName(plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId()).getKarmaAlignment()).subSequence(0, 2);
                event.setFormat(event.getFormat().replace("%1$s", color(color + event.getPlayer().getName() + "&r")));
                event.setFormat(event.getFormat().replace(event.getPlayer().getName(), color(color + event.getPlayer().getName() + "&r")));
            }
        }
        if (!plugin.isMessageSentEnabled()) return;
        for (String word : plugin.getKarmaWordsMap().keySet()) {
            if (!event.getMessage().toUpperCase().contains(word.toUpperCase())) continue;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> changeKarmaScore(event.getPlayer(), plugin.getKarmaWordsMap().get(word), KarmaSource.CHAT));
        }
    }
}
