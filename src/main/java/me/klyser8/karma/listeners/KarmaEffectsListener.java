package me.klyser8.karma.listeners;

import me.klyser8.karma.Karma;
import me.klyser8.karma.enums.KarmaAlignment;
import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.events.KarmaGainEvent;
import me.klyser8.karma.events.KarmaLossEvent;
import me.klyser8.karma.handlers.KarmaEnumFetcher;
import me.klyser8.karma.storage.PlayerData;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static me.klyser8.karma.util.UtilMethods.*;

public class KarmaEffectsListener implements Listener {

    private Karma plugin;
    private int rudeHighBoundary;
    public KarmaEffectsListener(Karma plugin) {
        this.plugin = plugin;
        rudeHighBoundary = new KarmaEnumFetcher(plugin).getAlignmentHighBoundary(KarmaAlignment.RUDE);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Random random = new Random();
        double chance;
        PlayerData data = plugin.getStorageHandler().getPlayerData(event.getEntity().getKiller().getUniqueId());
        if (data == null) return;
        KarmaAlignment alignment = data.getKarmaAlignment();
        if (data.getKarmaScore() <= rudeHighBoundary) {
            switch (alignment) {
                case RUDE:
                    chance = plugin.getKarmaRepercussionMap().get("Drops")[0];
                    break;
                case MEAN:
                    chance = plugin.getKarmaRepercussionMap().get("Drops")[1];
                    break;
                case VILE:
                    chance = plugin.getKarmaRepercussionMap().get("Drops")[2];
                    break;
                case EVIL:
                    chance = plugin.getKarmaRepercussionMap().get("Drops")[3];
                    break;
                default:
                    chance = 0;
            }
            if (random.nextInt(100) < chance) {
                event.getDrops().clear();
            }
        } else {
            switch (alignment) {
                case KIND:
                    chance = plugin.getKarmaPerkMap().get("Drops")[0];
                    break;
                case GOOD:
                    chance = plugin.getKarmaPerkMap().get("Drops")[1];
                    break;
                case PURE:
                    chance = plugin.getKarmaPerkMap().get("Drops")[2];
                    break;
                case BEST:
                    chance = plugin.getKarmaPerkMap().get("Drops")[3];
                    break;
                default:
                    chance = 0;
            }
            if (random.nextInt(100) < chance) {
                if (event.getDrops().size() > 0) {
                    int dropNum = random.nextInt(event.getDrops().size());
                    event.getDrops().add(event.getDrops().get(dropNum));
                }
            }
        }
    }

    @EventHandler
    public void onExperienceGain(PlayerExpChangeEvent event) {
        PlayerData data = plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId());
        KarmaAlignment alignment = data.getKarmaAlignment();
        int ogExp = event.getAmount();
        double amount;
        if (data.getKarmaScore() <= rudeHighBoundary) {
            switch (alignment) {
                case RUDE:
                    amount = plugin.getKarmaRepercussionMap().get("Experience")[0];
                    break;
                case MEAN:
                    amount = plugin.getKarmaRepercussionMap().get("Experience")[1];
                    break;
                case VILE:
                    amount = plugin.getKarmaRepercussionMap().get("Experience")[2];
                    break;
                case EVIL:
                    amount = plugin.getKarmaRepercussionMap().get("Experience")[3];
                    break;
                default:
                    amount = 1;
            }
            event.setAmount((int) (event.getAmount() * amount));
        } else {
            switch (alignment) {
                case KIND:
                    amount = plugin.getKarmaPerkMap().get("Experience")[0];
                    break;
                case GOOD:
                    amount = plugin.getKarmaPerkMap().get("Experience")[1];
                    break;
                case PURE:
                    amount = plugin.getKarmaPerkMap().get("Experience")[2];
                    break;
                case BEST:
                    amount = plugin.getKarmaPerkMap().get("Experience")[3];
                    break;
                default:
                    amount = 1;
            }
            event.setAmount((int) Math.round(event.getAmount() * amount));
        }
        debugMessage(event.getPlayer().getName() + " original xp gained: " + ogExp, "Updated xp gained: " + event.getAmount());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if ((event.getBlock().getType().toString().contains("_ORE") &&
                event.getPlayer().getGameMode() != GameMode.CREATIVE)) {
            World world = event.getBlock().getWorld();
            Location blockLoc = event.getBlock().getLocation().add(0.5, 0, 0.5);
            Random random = new Random();
            PlayerData data = plugin.getStorageHandler().getPlayerData(event.getPlayer().getUniqueId());
            KarmaAlignment alignment = data.getKarmaAlignment();
            double chance;
            if (data.getKarmaScore() <= rudeHighBoundary) {
                switch (alignment) {
                    case RUDE:
                        chance = plugin.getKarmaRepercussionMap().get("Mining")[0];
                        break;
                    case MEAN:
                        chance = plugin.getKarmaRepercussionMap().get("Mining")[1];
                        break;
                    case VILE:
                        chance = plugin.getKarmaRepercussionMap().get("Mining")[2];
                        break;
                    case EVIL:
                        chance = plugin.getKarmaRepercussionMap().get("Mining")[3];
                        break;
                    default:
                        chance = 0;
                }
                if (random.nextInt(100) > chance) return;
                if (checkVersion("1.15", "1.14", "1.13"))
                    event.setDropItems(false);
                else {
                    event.setCancelled(true);
                    event.getBlock().setType(Material.AIR);
                }
                if (!checkVersion("1.15", "1.14", "1.13")) {
                    if (event.getBlock().getType() != Material.valueOf("QUARTZ_ORE"))
                        world.dropItemNaturally(blockLoc, new ItemStack(Material.COBBLESTONE, 1));
                    else
                        world.dropItemNaturally(blockLoc, new ItemStack(Material.NETHERRACK, 1));
                } else {
                    if (event.getBlock().getType() != Material.NETHER_QUARTZ_ORE)
                        world.dropItemNaturally(blockLoc, new ItemStack(Material.COBBLESTONE, 1));
                    else
                        world.dropItemNaturally(blockLoc, new ItemStack(Material.NETHERRACK, 1));
                }
                event.setExpToDrop(0);
            } else {
                switch (alignment) {
                    case KIND:
                        chance = plugin.getKarmaPerkMap().get("Mining")[0];
                        break;
                    case GOOD:
                        chance = plugin.getKarmaPerkMap().get("Mining")[1];
                        break;
                    case PURE:
                        chance = plugin.getKarmaPerkMap().get("Mining")[2];
                        break;
                    case BEST:
                        chance = plugin.getKarmaPerkMap().get("Mining")[3];
                        break;
                    default:
                        chance = 0;
                }
                if (random.nextInt(100) > chance) return;
                if ((event.getBlock().getType() != Material.GOLD_ORE && event.getBlock().getType() != Material.IRON_ORE) &&
                        (event.getPlayer().getItemInHand().getType() == Material.DIAMOND_PICKAXE ||
                                event.getPlayer().getItemInHand().getType() == Material.IRON_PICKAXE) &&
                        !event.getPlayer().getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                    Material material = world.getBlockAt(blockLoc).getType();
                    if (checkVersion("1.15", "1.14"))
                        world.playSound(blockLoc, Sound.BLOCK_BELL_RESONATE, 1, 1.3F);
                    else if (checkVersion("1.13")) {
                        new BukkitRunnable() {
                            int timer = 1;
                            @Override
                            public void run() {
                                if (timer >= 4)
                                    cancel();
                                world.playSound(blockLoc, Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                                timer++;
                            }
                        }.runTaskTimer(plugin, 6, 6);
                    } else {
                        new BukkitRunnable() {
                            int timer = 1;

                            @Override
                            public void run() {
                                if (timer >= 4)
                                    cancel();
                                if (!checkVersion("1.8"))
                                    world.playSound(blockLoc, Sound.valueOf("BLOCK_NOTE_BASS"), 1, 1);
                                else
                                    world.playSound(blockLoc, Sound.valueOf("NOTE_BASS"), 1, 1);
                                timer++;
                            }
                        }.runTaskTimer(plugin, 6, 6);
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            debugMessage("Mineral re-spawned at location", blockLoc);
                            world.getBlockAt(blockLoc).setType(material);
                            if (!checkVersion("1.8"))
                                world.playSound(blockLoc, Sound.BLOCK_STONE_PLACE, 1, 0.8F);
                            else
                                world.playSound(blockLoc, Sound.valueOf("DIG_STONE"), 1, 0.8F);
                        }
                    }.runTaskLater(plugin, 30);

                }
            }
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Random random = new Random();
        double chance;
        PlayerData data = plugin.getStorageHandler().getPlayerData(event.getEnchanter().getUniqueId());
        KarmaAlignment alignment = data.getKarmaAlignment();
        boolean isVersionNew = !(checkVersion("1.8", "1.9", "1.10"));
        if (data.getKarmaScore() <= rudeHighBoundary) {
            switch (alignment) {
                case RUDE:
                    chance = plugin.getKarmaRepercussionMap().get("Enchanting")[0];
                    break;
                case MEAN:
                    chance = plugin.getKarmaRepercussionMap().get("Enchanting")[1];
                    break;
                case VILE:
                    chance = plugin.getKarmaRepercussionMap().get("Enchanting")[2];
                    break;
                case EVIL:
                    chance = plugin.getKarmaRepercussionMap().get("Enchanting")[3];
                    break;
                default:
                    chance = 0;
            }
            if (random.nextInt(100) > chance) return;
            List<Enchantment> enchantments = new ArrayList<>(event.getEnchantsToAdd().keySet());
            if (enchantments.size() > 1) {
                int removedEnchant = random.nextInt(event.getEnchantsToAdd().size());
                event.getEnchantsToAdd().remove(enchantments.get(removedEnchant));
                debugMessage("Enchantment removed from " + event.getEnchanter().getName() + "'s tool", "" + enchantments.get(removedEnchant));
                if (chance > chance / 2 || !isVersionNew) return;
                event.getEnchantsToAdd().put(Enchantment.VANISHING_CURSE, 1);
            } else if (isVersionNew)
                event.getEnchantsToAdd().put(Enchantment.VANISHING_CURSE, 1);
        } else {
            switch (alignment) {
                case KIND:
                    chance = plugin.getKarmaPerkMap().get("Enchanting")[0];
                    break;
                case GOOD:
                    chance = plugin.getKarmaPerkMap().get("Enchanting")[1];
                    break;
                case PURE:
                    chance = plugin.getKarmaPerkMap().get("Enchanting")[2];
                    break;
                case BEST:
                    chance = plugin.getKarmaPerkMap().get("Enchanting")[3];
                    break;
                default:
                    chance = 0;
            }
            if (random.nextInt(100) > chance) return;
            Enchantment[] enchantments = Enchantment.values();
            boolean enchantable = false;
            if (event.getItem().getType() != Material.BOOK) {
                while (!enchantable) {
                    int rnd = random.nextInt(Enchantment.values().length);
                    if (!enchantments[rnd].canEnchantItem(event.getItem()) || enchantments[rnd].getKey().getKey().toLowerCase().contains("curse")) return;
                    Enchantment[] itemEnchants = event.getEnchantsToAdd().keySet().toArray(new Enchantment[0]);
                    boolean canEnchant = true;
                    for (Enchantment ench : itemEnchants) {
                        if (ench.conflictsWith(enchantments[rnd])) {
                            canEnchant = false;
                            break;
                        }
                    }
                    if (!canEnchant) return;
                    enchantable = true;
                    debugMessage("Extra Enchantment to " + event.getEnchanter().getName() + "'s tool", enchantments[rnd].toString());
                    event.getEnchantsToAdd().put(enchantments[rnd], random.nextInt(enchantments[rnd].getMaxLevel()) + enchantments[rnd].getStartLevel());
                }
            } else {
                int rnd = random.nextInt(Enchantment.values().length);
                debugMessage("Extra Enchantment to " + event.getEnchanter().getName() + "'s tool", enchantments[rnd].toString());
                if (isVersionNew) {
                    if (!enchantments[rnd].getKey().getKey().toLowerCase().contains("curse"))
                        event.getEnchantsToAdd().put(enchantments[rnd], random.nextInt(enchantments[rnd].getMaxLevel()) + enchantments[rnd].getStartLevel());
                } else
                    event.getEnchantsToAdd().put(enchantments[rnd], random.nextInt(enchantments[rnd].getMaxLevel()) + enchantments[rnd].getStartLevel());
            }
        }
    }

    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
        World world = event.getWorld();
        Player player = randomPlayer();
        if (player == null) return;
        if (!world.isThundering() || player.getGameMode() == GameMode.CREATIVE && player.getGameMode() == GameMode.SPECTATOR) return;
        Random random = new Random();
        KarmaAlignment alignment = plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment();
        double chance;
        switch (alignment) {
            case RUDE:
                chance = plugin.getKarmaRepercussionMap().get("Lightning")[0];
                break;
            case MEAN:
                chance = plugin.getKarmaRepercussionMap().get("Lightning")[1];
                break;
            case VILE:
                chance = plugin.getKarmaRepercussionMap().get("Lightning")[2];
                break;
            case EVIL:
                chance = plugin.getKarmaRepercussionMap().get("Lightning")[3];
                break;
            default:
                chance = 0;
        }
        if (random.nextInt(100) > chance || event.getLightning().getLocation().distance(player.getLocation()) < 20) return;
        int time = random.nextInt(100) + 100;
        debugMessage("Lightning will strike near " + player.getName() + " in " + time / 20 + " seconds, unless they take cover.");
        final Location[] oldLoc = {null};
        new BukkitRunnable() {
            @Override
            public void run() {
                oldLoc[0] = player.getLocation();
            }
        }.runTaskLater(plugin, time - 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!world.isThundering() || player.getLocation().getBlock().getLightFromSky() != 15) return;
                world.strikeLightning(oldLoc[0]);
            }
        }.runTaskLater(plugin, time);
    }

    @EventHandler
    public void onKarmaGain(KarmaGainEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        KarmaAlignment alignment = plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment();
        double multiplier;
        if (event.getSource() != KarmaSource.COMMAND && event.getSource() != KarmaSource.VOTING) {
            if (event.getOldKarma() < rudeHighBoundary) {
                switch (alignment) {
                    case RUDE:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Gained")[0];
                        break;
                    case MEAN:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Gained")[1];
                        break;
                    case VILE:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Gained")[2];
                        break;
                    case EVIL:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Gained")[3];
                        break;
                    default:
                        multiplier = 1;
                }
            } else {
                switch (alignment) {
                    case KIND:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Gained")[0];
                        break;
                    case GOOD:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Gained")[1];
                        break;
                    case PURE:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Gained")[2];
                        break;
                    case BEST:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Gained")[3];
                        break;
                    default:
                        multiplier = 1;
                }
            }
            if (data.getLastSource() == event.getSource() && plugin.decreaseMultiplierEnabled)
                multiplier *= plugin.decreaseMultiplierAmount;
            event.setAmount(event.getAmount() * multiplier);
        }
    }

    @EventHandler
    public void onKarmaLose(KarmaLossEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
        KarmaAlignment alignment = plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment();
        double multiplier;
        if (event.getSource() != KarmaSource.COMMAND && event.getSource() != KarmaSource.VOTING) {
            if (event.getOldKarma() < rudeHighBoundary) {
                switch (alignment) {
                    case EVIL:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Lost")[3];
                        break;
                    case VILE:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Lost")[2];
                        break;
                    case MEAN:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Lost")[1];
                        break;
                    case RUDE:
                        multiplier = plugin.getKarmaRepercussionMap().get("Karma Lost")[0];
                        break;
                    default:
                        multiplier = 1;
                }
            } else {
                switch (alignment) {
                    case KIND:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Lost")[0];
                        break;
                    case GOOD:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Lost")[1];
                        break;
                    case PURE:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Lost")[2];
                        break;
                    case BEST:
                        multiplier = plugin.getKarmaPerkMap().get("Karma Lost")[3];
                        break;
                    default:
                        multiplier = 1;
                }
            }
            if (data.getLastSource() == event.getSource() && plugin.increaseMultiplierEnabled)
                multiplier *= plugin.increaseMultiplierAmount;
            event.setAmount(event.getAmount() * multiplier);
        }
    }


}
