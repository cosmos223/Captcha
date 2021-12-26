package me.kyllian.captcha.spigot.listeners.login;

import me.kyllian.captcha.shared.data.StateData;
import me.kyllian.captcha.spigot.CaptchaPlugin;
import me.kyllian.captcha.spigot.player.PlayerData;
import me.kyllian.captcha.spigot.utilities.Mode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class PlayerJoinListener implements Listener {

    private CaptchaPlugin plugin;

    public PlayerJoinListener(CaptchaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerData playerData = plugin.getPlayerDataHandler().getPlayerDataFromPlayer(player);
        Mode mode = Mode.valueOf(plugin.getConfig().getString("captcha-settings.mode"));
        if (mode == Mode.FIRSTJOIN && !(player.hasPlayedBefore() && playerData.hasPassed())) {
            plugin.playerJoinMessages.put(event.getPlayer(), event.getJoinMessage());
            event.setJoinMessage(plugin.getConfig().getString("captcha-settings.not-passed-join-message").replace("%player%", event.getPlayer().getName()));
        } else if (mode == Mode.NONE && plugin.getServer().getOnlinePlayers().size() >= plugin.getMaxPlayerWithoutCaptcha() && !(player.hasPlayedBefore() && playerData.hasPassed())) {
            plugin.playerJoinMessages.put(event.getPlayer(), event.getJoinMessage());
            event.setJoinMessage(plugin.getConfig().getString("captcha-settings.not-passed-join-message").replace("%player%", event.getPlayer().getName()));
        } else {
            new BukkitRunnable() {
                public void run() {
                    notifyBungee(player, false);
                }
            }.runTaskLater(plugin, 5);
            return;
        }
        new BukkitRunnable() {
            public void run() {
                plugin.getPlayerDataHandler().loadPlayerDataFromPlayer(player);
                plugin.getCaptchaHandler().login(player);
            }
        }.runTaskLater(plugin, 10);
    }

    private void notifyBungee(Player player, boolean state) {
        StateData data = new StateData(player.getUniqueId().toString(), state);
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(data);
            so.flush();
            player.sendPluginMessage(plugin, "kyllian:captcha", bo.toByteArray());
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
