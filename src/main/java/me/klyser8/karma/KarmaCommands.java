package me.klyser8.karma;

import me.klyser8.karma.enums.KarmaSource;
import me.klyser8.karma.enums.Keyword;
import me.klyser8.karma.handlers.KarmaHandler;
import me.klyser8.karma.handlers.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

import static me.klyser8.karma.enums.Command.*;
import static me.klyser8.karma.enums.Message.*;
import static me.klyser8.karma.util.UtilMethods.*;

public class KarmaCommands implements CommandExecutor, TabCompleter {

    private Karma plugin;
    private KarmaHandler karma;
    public KarmaCommands(Karma plugin, KarmaHandler karma) {
        this.plugin = plugin;
        this.karma = karma;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("karma")) {
            if (sender instanceof Player) {
                PlayerData data = plugin.getStorageHandler().getPlayerData(((Player) sender).getUniqueId());
                if (args.length == 0) {

                    //View self karma command
                    if (sender.hasPermission("karma.command.view")) {
                        String scoreMessage = KARMA_VIEW_SCORE_SELF.getMessage().replace("<NUMBER>", String.valueOf(data.getKarmaScore()));
                        sender.sendMessage(color(scoreMessage));
                        String alignmentMessage = KARMA_VIEW_ALIGNMENT_SELF.getMessage().replace("<ALIGNMENT>", data.getKarmaAlignment().getName());
                        sender.sendMessage(color(alignmentMessage));
                    } else
                        sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                } else {

                    //Help karma command
                    if (args[0].equalsIgnoreCase(KARMA_HELP_COMMAND.getCommand()) || args[0].equalsIgnoreCase("?")) {
                        if (sender.hasPermission("karma.command.help"))
                            helpCommandPlayer(sender);
                        else
                            sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                    }

                    //Reload karma plugin command
                    else if (args[0].equalsIgnoreCase(KARMA_RELOAD_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.reload")) {
                            plugin.reload();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                karma.updateAlignments(player);
                            }
                            sender.sendMessage(color(KARMA_RELOAD.getMessage()));
                        } else
                            sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                    }

                    //Save karma data command
                    else if (args[0].equalsIgnoreCase(KARMA_SAVE_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.save")) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                plugin.getStorageHandler().savePlayerData(player.getUniqueId());
                            }
                            sender.sendMessage(color(KARMA_SAVE.getMessage()));
                        } else
                            sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                    }


                    //List karma command

                    else if (args[0].equalsIgnoreCase(KARMA_LIST_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.list")) {
                            //Sorting players from highest to lowest karma to start with.
                            Map<PlayerData, Double> map = new HashMap<>();
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                PlayerData pData = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
                                map.put(pData, pData.getKarmaScore());
                            }
                            LinkedHashMap<PlayerData, Double> reverseSortedMap = new LinkedHashMap<>();
                            map.entrySet().stream()
                                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                    .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
                            for (PlayerData pData : reverseSortedMap.keySet()) {
                                sender.sendMessage(color(KARMA_VIEW_LIST_PLAYER.getMessage()
                                        .replace("<PLAYER>", pData.getPlayer().getName())
                                        .replace("<SCORE>", String.valueOf(reverseSortedMap.get(pData)))
                                        .replace("<ALIGNMENT>", pData.getKarmaAlignment().getName())));
                            }
                        }
                    }
                    //View karma command
                    else if (args[0].equalsIgnoreCase(KARMA_VIEW_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.view")) {
                            if (args.length == 1) {
                                String scoreMessage = KARMA_VIEW_SCORE_SELF.getMessage().replace("<NUMBER>", String.valueOf(data.getKarmaScore()));
                                sender.sendMessage(color(scoreMessage));
                                String alignmentMessage = KARMA_VIEW_ALIGNMENT_SELF.getMessage().replace("<ALIGNMENT>", String.valueOf(data.getKarmaAlignment().getName()));
                                sender.sendMessage(color(alignmentMessage));
                            } else if (args.length == 2) {
                                if (Bukkit.getPlayerExact(args[1]) != null) {
                                    if (Bukkit.getPlayerExact(args[1]).isOnline()) {
                                        Player player = Bukkit.getPlayer(args[1]);
                                        String scoreMessage = KARMA_VIEW_SCORE.getMessage().replace("<NUMBER>", String.valueOf(plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaScore())).replace("<PLAYER>", player.getName());
                                        sender.sendMessage(color(scoreMessage));
                                        String alignmentMessage = KARMA_VIEW_ALIGNMENT.getMessage().replace("<ALIGNMENT>", plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment().getName()).replace("<PLAYER>", player.getName());
                                        sender.sendMessage(color(alignmentMessage));
                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                } else
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            }
                        } else
                            sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                    }

                    //Karma add command
                    else if (args[0].equalsIgnoreCase(KARMA_ADD_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.add")) {
                            if (args.length == 1) {
                                sender.sendMessage(color(KARMA_ERROR_MISUSE.getMessage()));
                            } else if (args.length == 2) {
                                if (isDouble(args[1]) && plugin.getStorageHandler().getPlayerData(((Player) sender).getUniqueId()).getKarmaScore() + Double.parseDouble(args[1]) < 2147483647) {
                                    karma.addKarmaScore(((Player) sender), Double.parseDouble(args[1]), KarmaSource.COMMAND);
                                    String message = KARMA_ADDED_SUCCESSFULLY_SELF.getMessage().replace("<NUMBER>", args[1]);
                                    sender.sendMessage(color(message));
                                } else {
                                    sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                                }
                            } else if (args.length == 3) {
                                if (Bukkit.getPlayerExact(args[2]) != null) {
                                    Player player = Bukkit.getPlayerExact(args[2]);
                                    if (isDouble(args[1]) && plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaScore() + Double.parseDouble(args[1]) < 2147483647) {
                                        if (Bukkit.getPlayerExact(args[2]).isOnline()) {
                                            karma.addKarmaScore(player, Double.parseDouble(args[1]), KarmaSource.COMMAND);
                                            String message = KARMA_ADDED_SUCCESSFULLY.getMessage().replace("<NUMBER>", args[1]).replace("<PLAYER>", player.getName());
                                            sender.sendMessage(color(message));
                                        } else
                                            sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                                } else {
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                }
                            } else
                                sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                        }
                    }

                    //Karma remove command
                    else if (args[0].equalsIgnoreCase(KARMA_REMOVE_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.remove")) {
                            if (args.length == 1) {
                                sender.sendMessage(color(KARMA_ERROR_MISUSE.getMessage()));
                            } else if (args.length == 2) {
                                if (isDouble(args[1]) && plugin.getStorageHandler().getPlayerData(((Player) sender).getUniqueId()).getKarmaScore() - Double.parseDouble(args[1]) > -2147483647) {
                                    karma.subtractKarmaScore(((Player) sender), Double.parseDouble(args[1]), KarmaSource.COMMAND);
                                    String message = KARMA_REMOVED_SUCCESSFULLY_SELF.getMessage().replace("<NUMBER>", args[1]);
                                    sender.sendMessage(color(message));
                                } else {
                                    sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                                }
                            } else if (args.length == 3) {
                                if (Bukkit.getPlayerExact(args[2]) != null) {
                                    Player player = Bukkit.getPlayerExact(args[2]);
                                    if (isDouble(args[1]) && plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaScore() - Double.parseDouble(args[1]) > -2147483647) {
                                        if (Bukkit.getPlayerExact(args[2]).isOnline()) {
                                            karma.subtractKarmaScore(player, Double.parseDouble(args[1]), KarmaSource.COMMAND);
                                            String message = KARMA_REMOVED_SUCCESSFULLY.getMessage().replace("<NUMBER>", args[1]).replace("<PLAYER>", player.getName());
                                            sender.sendMessage(color(message));
                                        } else
                                            sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));

                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                                } else {
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                }
                            }
                        } else
                            sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                    }

                    //Set karma command
                    else if (args[0].equalsIgnoreCase(KARMA_SET_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.set")) {
                            if (args.length == 1) {
                                sender.sendMessage(color(KARMA_ERROR_MISUSE.getMessage()));
                            } else if (args.length == 2) {
                                if ((isNegativeDouble(args[1]) || (isDouble(args[1])) && Double.parseDouble(args[1]) < 2147483647 && Double.parseDouble(args[1]) > -2147483647)) {
                                    karma.setKarmaScore(((Player) sender), Double.parseDouble(args[1]));
                                    String message = KARMA_SET_SUCCESSFULLY_SELF.getMessage().replace("<NUMBER>", args[1]);
                                    sender.sendMessage(color(message));
                                } else {
                                    sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                                }
                            } else if (args.length == 3) {
                                if (Bukkit.getPlayerExact(args[2]) != null) {
                                    if (Bukkit.getPlayerExact(args[2]).isOnline()) {
                                        if ((isNegativeDouble(args[1]) || isDouble(args[1])) && Double.parseDouble(args[1]) < 2147483647 && Double.parseDouble(args[1]) > -2147483647) {
                                            Player player = Bukkit.getPlayerExact(args[2]);
                                            karma.setKarmaScore(player, Double.parseDouble(args[1]));
                                            String message = KARMA_SET_SUCCESSFULLY.getMessage().replace("<NUMBER>", args[1]).replace("<PLAYER>", player.getName());
                                            sender.sendMessage(color(message));
                                        } else
                                            sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                } else {
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                }
                            }
                        } else
                            sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                    }

                    //Clear karma command
                    else if (args[0].equalsIgnoreCase(KARMA_CLEAR_COMMAND.getCommand())) {
                        if (sender.hasPermission("karma.command.clear")) {
                            if (args.length == 1) {
                                karma.clearKarmaScore(((Player) sender));
                                sender.sendMessage(color(KARMA_CLEARED_SUCCESSFULLY_SELF.getMessage()));
                            } else if (args.length == 2) {
                                if (Bukkit.getPlayerExact(args[1]) != null) {
                                    if (Bukkit.getPlayerExact(args[1]).isOnline()) {
                                        Player player = Bukkit.getPlayerExact(args[1]);
                                        karma.clearKarmaScore(player);
                                        String message = KARMA_CLEARED_SUCCESSFULLY.getMessage().replace("<PLAYER>", player.getName());
                                        sender.sendMessage(color(message));
                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                } else
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            }
                        } else
                            sender.sendMessage(color(KARMA_ERROR_NO_PERMISSION.getMessage()));
                    } else {
                        sender.sendMessage(color(KARMA_ERROR_UNKNOWN_COMMAND.getMessage()));
                    }
                }
            } else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
                if (args.length > 0) {

                    //Help karma command
                    if (args[0].equalsIgnoreCase(KARMA_HELP_COMMAND.getCommand()) || args[0].equalsIgnoreCase("?")) {
                        helpCommandConsole(sender);
                    }

                    //Reload karma plugin command
                    else if (args[0].equalsIgnoreCase(KARMA_RELOAD_COMMAND.getCommand())) {
                        plugin.reload();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            karma.updateAlignments(player);
                        }
                        sender.sendMessage(color(KARMA_RELOAD.getMessage()));
                    }

                    //Save karma data command
                    else if (args[0].equalsIgnoreCase(KARMA_SAVE_COMMAND.getCommand())) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            plugin.getStorageHandler().savePlayerData(player.getUniqueId());
                        }
                        sender.sendMessage(color(KARMA_SAVE.getMessage()));
                    }

                    //List karma command
                    else if (args[0].equalsIgnoreCase(KARMA_LIST_COMMAND.getCommand())) {
                        //Sorting players from highest to lowest karma to start with.
                        Map<PlayerData, Double> map = new HashMap<>();
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            PlayerData pData = plugin.getStorageHandler().getPlayerData(player.getUniqueId());
                            map.put(pData, pData.getKarmaScore());
                        }
                        LinkedHashMap<PlayerData, Double> reverseSortedMap = new LinkedHashMap<>();
                        map.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
                        for (PlayerData pData : reverseSortedMap.keySet()) {
                            sender.sendMessage(color(KARMA_VIEW_LIST_PLAYER.getMessage()
                                    .replace("<PLAYER>", pData.getPlayer().getName())
                                    .replace("<SCORE>", String.valueOf(reverseSortedMap.get(pData)))
                                    .replace("<ALIGNMENT>", pData.getKarmaAlignment().getName())));
                        }
                    }

                    //View karma command
                    else if (args[0].equalsIgnoreCase(KARMA_VIEW_COMMAND.getCommand())) {
                        if (args.length == 2) {
                            if (Bukkit.getPlayerExact(args[1]) != null) {
                                if (Bukkit.getPlayerExact(args[1]).isOnline()) {
                                    Player player = Bukkit.getPlayer(args[1]);
                                    String scoreMessage = KARMA_VIEW_SCORE.getMessage().replace("<NUMBER>", String.valueOf(plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaScore())).replace("<PLAYER>", player.getName());
                                    sender.sendMessage(color(scoreMessage));
                                    String alignmentMessage = KARMA_VIEW_ALIGNMENT.getMessage()
                                            .replace("<ALIGNMENT>", String.valueOf(plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaAlignment().getName()))
                                            .replace("<PLAYER>", player.getName());
                                    sender.sendMessage(color(alignmentMessage));
                                } else
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            } else
                                sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                        }
                    }

                    //Karma add command
                    else if (args[0].equalsIgnoreCase(KARMA_ADD_COMMAND.getCommand())) {
                        if (args.length == 1) {
                            sender.sendMessage(color(KARMA_ERROR_MISUSE.getMessage()));
                        } else if (args.length == 3) {
                            if (Bukkit.getPlayerExact(args[2]) != null) {
                                Player player = Bukkit.getPlayerExact(args[2]);
                                if (isDouble(args[1]) && plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaScore() + Double.parseDouble(args[1]) < 2147483647) {
                                    if (Bukkit.getPlayerExact(args[2]).isOnline()) {
                                        karma.addKarmaScore(player, Double.parseDouble(args[1]), KarmaSource.COMMAND);
                                        String message = KARMA_ADDED_SUCCESSFULLY.getMessage().replace("<NUMBER>", args[1]).replace("<PLAYER>", player.getName());
                                        sender.sendMessage(color(message));
                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                                } else
                                    sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                            } else {
                                sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            }
                        }
                    }

                    //Karma remove command
                    else if (args[0].equalsIgnoreCase(KARMA_REMOVE_COMMAND.getCommand())) {
                        if (args.length == 1) {
                            sender.sendMessage(color(KARMA_ERROR_MISUSE.getMessage()));
                        } else if (args.length == 3) {
                            if (Bukkit.getPlayerExact(args[2]) != null) {
                                Player player = Bukkit.getPlayerExact(args[2]);
                                if (isDouble(args[1]) && plugin.getStorageHandler().getPlayerData(player.getUniqueId()).getKarmaScore() - Double.parseDouble(args[1]) > -2147483647) {
                                    if (Bukkit.getPlayerExact(args[2]).isOnline()) {
                                        karma.subtractKarmaScore(player, Double.parseDouble(args[1]), KarmaSource.COMMAND);
                                        String message = KARMA_REMOVED_SUCCESSFULLY.getMessage().replace("<NUMBER>", args[1]).replace("<PLAYER>", player.getName());
                                        sender.sendMessage(color(message));
                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));

                                } else
                                    sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                            } else {
                                sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            }
                        }
                    }

                    //Set karma command
                    else if (args[0].equalsIgnoreCase(KARMA_SET_COMMAND.getCommand())) {
                        if (args.length == 1) {
                            sender.sendMessage(color(KARMA_ERROR_MISUSE.getMessage()));
                        } else if (args.length == 3) {
                            if (Bukkit.getPlayerExact(args[2]) != null) {
                                if (Bukkit.getPlayerExact(args[2]).isOnline()) {
                                    if ((isNegativeDouble(args[1]) || isDouble(args[1])) && Double.parseDouble(args[1]) < 2147483647 && Double.parseDouble(args[1]) > -2147483647) {
                                        Player player = Bukkit.getPlayerExact(args[2]);
                                        karma.setKarmaScore(player, Double.parseDouble(args[1]));
                                        String message = KARMA_SET_SUCCESSFULLY.getMessage().replace("<NUMBER>", args[1]).replace("<PLAYER>", player.getName());
                                        sender.sendMessage(color(message));
                                    } else
                                        sender.sendMessage(color(KARMA_ERROR_INVALID_NUMBER.getMessage()));
                                } else
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            } else {
                                sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            }
                        }
                    }

                    //Clear karma command
                    else if (args[0].equalsIgnoreCase(KARMA_CLEAR_COMMAND.getCommand())) {
                        if (args.length == 2) {
                            if (Bukkit.getPlayerExact(args[1]) != null) {
                                if (Bukkit.getPlayerExact(args[1]).isOnline()) {
                                    Player player = Bukkit.getPlayerExact(args[1]);
                                    karma.clearKarmaScore(player);
                                    String message = KARMA_CLEARED_SUCCESSFULLY.getMessage().replace("<PLAYER>", player.getName());
                                    sender.sendMessage(color(message));
                                } else
                                    sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                            } else
                                sender.sendMessage(color(KARMA_ERROR_UNKNOWN_PLAYER.getMessage()));
                        }
                    } else {
                        sender.sendMessage(color(KARMA_ERROR_UNKNOWN_COMMAND.getMessage()));
                    }
                }
            }
        }
        return true;
    }

    private void helpCommandPlayer(CommandSender sender) {
        String helpHelp = KARMA_HELP_HELP.getMessage().replace("<COMMAND>", "/Karma " + KARMA_HELP_COMMAND.getCommand());
        String helpReload = KARMA_HELP_RELOAD.getMessage().replace("<COMMAND>", "/Karma " + KARMA_RELOAD_COMMAND.getCommand());
        String helpSave = KARMA_HELP_SAVE.getMessage().replace("<COMMAND>", "/Karma " + KARMA_SAVE_COMMAND.getCommand());
        String helpList = KARMA_HELP_LIST.getMessage().replace("<COMMAND>", "/Karma " + KARMA_LIST_COMMAND.getCommand());
        String helpView = KARMA_HELP_VIEW.getMessage().replace("<COMMAND>", "/Karma " + KARMA_VIEW_COMMAND.getCommand() + " [" + Keyword.PLAYER.getWord() + "]");
        String helpAdd = KARMA_HELP_ADD.getMessage().replace("<COMMAND>", "/Karma " + KARMA_ADD_COMMAND.getCommand() + " <" + Keyword.NUMBER.getWord() + "> " + "[" + Keyword.PLAYER.getWord() + "]");
        String helpRemove = KARMA_HELP_REMOVE.getMessage().replace("<COMMAND>", "/Karma " + KARMA_REMOVE_COMMAND.getCommand() + " <" + Keyword.NUMBER.getWord() + "> " + "[" + Keyword.PLAYER.getWord() + "]");
        String helpSet = KARMA_HELP_SET.getMessage().replace("<COMMAND>", "/Karma " + KARMA_SET_COMMAND.getCommand() + " <" + Keyword.NUMBER.getWord() + "> " + "[" + Keyword.PLAYER.getWord() + "]");
        String helpClear = KARMA_HELP_CLEAR.getMessage().replace("<COMMAND>", "/Karma " + KARMA_CLEAR_COMMAND.getCommand() + " [" + Keyword.PLAYER.getWord() + "]");

        sender.sendMessage(color("&5--------- &r" + KARMA_HELP_COMMAND.getCommand().toUpperCase() + " &5---------------------------"));
        if (sender.hasPermission("karma.command.help"))
            sender.sendMessage(color(helpHelp));
        if (sender.hasPermission("karma.command.reload"))
            sender.sendMessage(color(helpReload));
        if (sender.hasPermission("karma.command.save"))
            sender.sendMessage(color(helpSave));
        if (sender.hasPermission("karma.command.list"))
            sender.sendMessage(color(helpList));
        if (sender.hasPermission("karma.command.view"))
            sender.sendMessage(color(helpView));
        if (sender.hasPermission("karma.command.add"))
            sender.sendMessage(color(helpAdd));
        if (sender.hasPermission("karma.command.remove"))
            sender.sendMessage(color(helpRemove));
        if (sender.hasPermission("karma.command.set"))
            sender.sendMessage(color(helpSet));
        if (sender.hasPermission("karma.command.clear"))
            sender.sendMessage(color(helpClear));
    }

    private void helpCommandConsole(CommandSender sender) {
        String helpHelp = KARMA_HELP_HELP.getMessage().replace("<COMMAND>", "/Karma " + KARMA_HELP_COMMAND.getCommand());
        String helpReload = KARMA_HELP_RELOAD.getMessage().replace("<COMMAND>", "/Karma " + KARMA_RELOAD_COMMAND.getCommand());
        String helpSave = KARMA_HELP_SAVE.getMessage().replace("<COMMAND>", "/Karma " + KARMA_SAVE_COMMAND.getCommand());
        String helpList = KARMA_HELP_LIST.getMessage().replace("<COMMAND>", "/Karma " + KARMA_LIST_COMMAND.getCommand());
        String helpView = KARMA_HELP_VIEW.getMessage().replace("<COMMAND>", "/Karma " + KARMA_VIEW_COMMAND.getCommand() + " [" + Keyword.PLAYER.getWord() + "]");
        String helpAdd = KARMA_HELP_ADD.getMessage().replace("<COMMAND>", "/Karma " + KARMA_ADD_COMMAND.getCommand() + " <" + Keyword.NUMBER.getWord() + "> " + "[" + Keyword.PLAYER.getWord() + "]");
        String helpRemove = KARMA_HELP_REMOVE.getMessage().replace("<COMMAND>", "/Karma " + KARMA_REMOVE_COMMAND .getCommand()+ " <" + Keyword.NUMBER.getWord() + "> " + "[" + Keyword.PLAYER.getWord() + "]");
        String helpSet = KARMA_HELP_SET.getMessage().replace("<COMMAND>", "/Karma " + KARMA_SET_COMMAND.getCommand() + " <" + Keyword.NUMBER.getWord() + "> " + "[" + Keyword.PLAYER.getWord() + "]");
        String helpClear = KARMA_HELP_CLEAR.getMessage().replace("<COMMAND>", "/Karma " + KARMA_CLEAR_COMMAND.getCommand() + " [" + Keyword.PLAYER.getWord() + "]");

        sender.sendMessage(color("&5------------- &r" + KARMA_HELP_COMMAND.getCommand().toUpperCase() + " &5-----------------------"));
        sender.sendMessage(color(helpHelp));
        sender.sendMessage(color(helpReload));
        sender.sendMessage(color(helpSave));
        sender.sendMessage(color(helpList));
        sender.sendMessage(color(helpView));
        sender.sendMessage(color(helpAdd));
        sender.sendMessage(color(helpRemove));
        sender.sendMessage(color(helpSet));
        sender.sendMessage(color(helpClear));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (command.getName().equalsIgnoreCase("karma")) {
                List<String> commands = new ArrayList<>();
                List<String> tabList = new ArrayList<>();
                if (args.length == 1) {
                    //Commands executable at any time
                    if (sender.hasPermission("karma.command.reload"))
                        commands.add(KARMA_RELOAD_COMMAND.getCommand());
                    if (sender.hasPermission("karma.command.help"))
                        commands.add(KARMA_HELP_COMMAND.getCommand());
                    if (sender.hasPermission("karma.command.save"))
                        commands.add(KARMA_SAVE_COMMAND.getCommand());
                    //Commands executable only when in guild
                    if (sender.hasPermission("karma.command.add"))
                        commands.add(KARMA_ADD_COMMAND.getCommand());
                    if (sender.hasPermission("karma.command.remove"))
                        commands.add(KARMA_REMOVE_COMMAND.getCommand());
                    if (sender.hasPermission("karma.command.set"))
                        commands.add(KARMA_SET_COMMAND.getCommand());
                    if (sender.hasPermission("karma.command.clear"))
                        commands.add(KARMA_CLEAR_COMMAND.getCommand());
                    if (sender.hasPermission("karma.command.view"))
                        commands.add(KARMA_VIEW_COMMAND.getCommand());
                    if (sender.hasPermission("karma.command.list")) {
                        commands.add(KARMA_VIEW_COMMAND.getCommand());
                    }
                } else if (args.length == 2) {
                    if ((args[0].equalsIgnoreCase(KARMA_VIEW_COMMAND.getCommand()) && sender.hasPermission("karma.command.view")) ||
                            (args[0].equalsIgnoreCase(KARMA_CLEAR_COMMAND.getCommand()) && sender.hasPermission("karma.command.clear")) ||
                            (args[0].equalsIgnoreCase(KARMA_SET_COMMAND.getCommand())) && sender.hasPermission("karma.command.view")) {
                        for (Player player : Bukkit.getOnlinePlayers())
                            commands.add(player.getName());
                    }
                } else if (args.length == 3) {
                    for (Player player : Bukkit.getOnlinePlayers())
                        commands.add(player.getName());
                }
                if (!args[args.length - 1].equals("")) {
                    for (String option : commands) {
                        if (option.startsWith(args[args.length - 1].toLowerCase())) {
                            tabList.add(option);
                        }
                    }
                } else {
                    tabList = commands;
                }
                Collections.sort(tabList);
                return tabList;
            }
        } else if (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
            if (command.getName().equalsIgnoreCase("karma")) {
                List<String> commands = new ArrayList<>();
                List<String> tabList = new ArrayList<>();
                if (args.length == 1) {
                    commands.add(KARMA_RELOAD_COMMAND.getCommand());
                    commands.add(KARMA_HELP_COMMAND.getCommand());
                    commands.add(KARMA_SAVE_COMMAND.getCommand());
                    commands.add(KARMA_ADD_COMMAND.getCommand());
                    commands.add(KARMA_REMOVE_COMMAND.getCommand());
                    commands.add(KARMA_SET_COMMAND.getCommand());
                    commands.add(KARMA_CLEAR_COMMAND.getCommand());
                    commands.add(KARMA_VIEW_COMMAND.getCommand());
                    commands.add(KARMA_LIST_COMMAND.getCommand());
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase(KARMA_VIEW_COMMAND.getCommand()) || args[0].equalsIgnoreCase(KARMA_CLEAR_COMMAND.getCommand()) ||
                            args[0].equalsIgnoreCase(KARMA_SET_COMMAND.getCommand())) {
                        for (Player player : Bukkit.getOnlinePlayers())
                            commands.add(player.getName());
                    }
                }
                else if (args.length == 3 || args[1].equalsIgnoreCase(KARMA_VIEW_COMMAND.getCommand()) || args[1].equalsIgnoreCase(KARMA_CLEAR_COMMAND.getCommand())) {
                    for (Player player : Bukkit.getOnlinePlayers())
                        commands.add(player.getName());
                }
                if (!args[args.length - 1].equals("")) {
                    for (String option : commands) {
                        if (option.startsWith(args[args.length - 1].toLowerCase())) {
                            tabList.add(option);
                        }
                    }
                } else {
                    tabList = commands;
                }
                Collections.sort(tabList);
                return tabList;
            }
        }
        return null;
    }
}
