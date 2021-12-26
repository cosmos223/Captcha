package me.kyllian.captcha.spigot.listeners;

import me.kyllian.captcha.spigot.CaptchaPlugin;
import me.kyllian.captcha.spigot.captchas.SolveState;
import me.kyllian.captcha.spigot.player.PlayerData;
import me.kyllian.captcha.spigot.utilities.Mode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private CaptchaPlugin plugin;

    public PlayerQuitListener(CaptchaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = plugin.getPlayerDataHandler().getPlayerDataFromPlayer(player);
        if (!playerData.hasAssignedCaptcha()) return;
        plugin.getCaptchaHandler().removeAssignedCaptcha(player, SolveState.LEAVE);
        event.setQuitMessage(plugin.getConfig().getString("captcha-settings.not-passed-quit-message").replace("%player%", event.getPlayer().getName()));
        plugin.playerJoinMessages.remove(event.getPlayer());
    }
}
