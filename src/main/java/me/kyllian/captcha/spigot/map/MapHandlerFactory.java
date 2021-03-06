package me.kyllian.captcha.spigot.map;

import me.kyllian.captcha.spigot.CaptchaPlugin;
import me.kyllian.captcha.spigot.handlers.MapHandler;
import org.bukkit.Bukkit;

public class MapHandlerFactory {

    private CaptchaPlugin plugin;

    public MapHandlerFactory(CaptchaPlugin plugin) {
        this.plugin = plugin;
    }

    public MapHandler getMapHandler() {
        String version = Bukkit.getVersion();
        if (version.contains("1.18") || version.contains("1.17") || version.contains("1.16") || version.contains("1.15") || version.contains("1.14") || version.contains("1.13")) return new MapHandlerNew(plugin);
        else return new MapHandlerOld(plugin);
//        String version = Bukkit.getMinecraftVersion();
//        if ((Integer.parseInt(version.split("\\.")[1]) >= 13 && Integer.parseInt(version.split("\\.")[0]) == 1) || Integer.parseInt(version.split("\\.")[0]) >= 2) {
//            return new MapHandlerNew(plugin);
//        } else {
//            return new MapHandlerOld(plugin);
//        }
    }
}
