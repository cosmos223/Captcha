package me.kyllian.captcha.spigot.commands;

import me.kyllian.captcha.spigot.CaptchaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Locale;

public class CaptchaCommand implements CommandExecutor {

    private CaptchaPlugin plugin;

    public CaptchaCommand(CaptchaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("captcha").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!commandSender.hasPermission("captcha.reload")) {
                    if (commandSender instanceof Player) {
                        //コマンドを送信したのがプレイヤーだった場合
                        Player player = (Player) commandSender;
                        commandSender.sendMessage(plugin.getMessageHandler().getMessage("no-permission", player.getLocale()));
                    } else {
                        commandSender.sendMessage(plugin.getMessageHandler().getMessage("no-permission", "default"));
                    }
                    return true;
                }
                plugin.getCaptchaHandler().removeAllCaptchas();
                plugin.reloadConfig();
                plugin.getMessageHandler().reload();
                plugin.getMapHandler().loadData();
                plugin.getPlayerDataHandler().reloadPlayerData();

                plugin.getStatusRecord().reloadData();

                if (commandSender instanceof Player) {
                    Player commandSenderPlayer = (Player) commandSender;
                    commandSender.sendMessage(plugin.getMessageHandler().getMessage("reload", commandSenderPlayer.getLocale()));
                } else {
                    commandSender.sendMessage(plugin.getMessageHandler().getMessage("reload", "default"));
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("setsafearea")) {
                if (!(commandSender instanceof Player)) {
                    commandSender.sendMessage(plugin.getMessageHandler().getMessage("player-only", "default"));
                    return true;
                }
                Player player = (Player) commandSender;
                if (!commandSender.hasPermission("captcha.setsafearea")) {
                    commandSender.sendMessage(plugin.getMessageHandler().getMessage("no-permission", player.getLocale()));
                    return true;
                }
                plugin.getSafeArea().setSafeLocation(player.getLocation());
                player.sendMessage(plugin.getMessageHandler().getMessage("safe-area-set", player.getLocale()));
                return true;
            }
            if (commandSender.hasPermission("captcha.run")) {
                Player player = Bukkit.getPlayerExact(args[0]);
                if (player == null) {
                    if (commandSender instanceof Player) {
                        Player commandSenderPlayer = (Player) commandSender;
                        commandSender.sendMessage(plugin.getMessageHandler().getMessage("not-online", commandSenderPlayer.getLocale()));
                    } else {
                        commandSender.sendMessage(plugin.getMessageHandler().getMessage("not-online", "default"));
                    }
                    return true;
                }
                try {
                    plugin.getCaptchaHandler().assignCaptcha(player);
                    return true;
                } catch (IllegalStateException exc) {
                    if (commandSender instanceof Player) {
                        Player commandSenderPlayer = (Player) commandSender;
                        commandSender.sendMessage(plugin.getMessageHandler().getMessage("already-in-captcha", commandSenderPlayer.getLocale()));
                    } else {
                        commandSender.sendMessage(plugin.getMessageHandler().getMessage("already-in-captcha", "default"));
                    }
                    return true;
                }
            } else {
                if (commandSender instanceof Player) {
                    //コマンドを送信したのがプレイヤーだった場合
                    Player player = (Player) commandSender;
                    commandSender.sendMessage(plugin.getMessageHandler().getMessage("no-permission", player.getLocale()));
                } else {
                    commandSender.sendMessage(plugin.getMessageHandler().getMessage("no-permission", "default"));
                }
                return true;
            }
        }
        if (commandSender instanceof Player) {
            Player commandSenderPlayer = (Player) commandSender;
            commandSender.sendMessage(plugin.getMessageHandler().getMessage("help", commandSenderPlayer.getLocale()));
        } else {
            commandSender.sendMessage(plugin.getMessageHandler().getMessage("help", "default"));
        }
        return true;
    }
}
