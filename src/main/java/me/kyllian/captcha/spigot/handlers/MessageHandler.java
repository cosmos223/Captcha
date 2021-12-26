package me.kyllian.captcha.spigot.handlers;

import me.kyllian.captcha.spigot.CaptchaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class MessageHandler {

    private CaptchaPlugin plugin;

    private File file;
    private File file_ja;
    private FileConfiguration fileConfiguration;
    private FileConfiguration fileConfiguration_ja;

    public MessageHandler(CaptchaPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        file_ja = new File(plugin.getDataFolder(), "messages_ja.yml");
        if (!file_ja.exists()) plugin.saveResource("messages_ja.yml", false);
        reload();
    }

    public void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        fileConfiguration_ja = YamlConfiguration.loadConfiguration(file_ja);
    }

    public String getMessage(String path, String locale) {
        Object object = fileConfiguration.get(path);
        if (locale.equalsIgnoreCase("ja_jp")) {
            object = fileConfiguration_ja.get(path);
        }
        String finalString = "";
        if (object instanceof List) finalString = String.join("\n",(List<String>) object);
        else finalString += ((String) object).replace("\\n", "\n");
        return translateColor(finalString);
    }

    public void updateMessages() {

    }

    public static String translateColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
