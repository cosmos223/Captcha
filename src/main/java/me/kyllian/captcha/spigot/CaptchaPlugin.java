package me.kyllian.captcha.spigot;

import me.kyllian.captcha.spigot.commands.CaptchaCommand;
import me.kyllian.captcha.spigot.handlers.*;
import me.kyllian.captcha.spigot.listeners.*;
import me.kyllian.captcha.spigot.listeners.login.LoginListener;
import me.kyllian.captcha.spigot.listeners.login.PlayerJoinListener;
import me.kyllian.captcha.spigot.map.MapHandlerFactory;
import me.kyllian.captcha.spigot.sql.StatusRecord;
import me.kyllian.captcha.spigot.utilities.SafeArea;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CaptchaPlugin extends JavaPlugin {

    private CaptchaHandler captchaHandler;
    private FontHandler fontHandler;
    private MapHandler mapHandler;
    private MessageHandler messageHandler;
    private PlayerDataHandler playerDataHandler;
    private SafeArea safeArea;
    private UpdateHandler updateHandler;

    public Map<Player, String> playerJoinMessages = new HashMap<>();

    private StatusRecord statusRecord;

    @Override
    public void onEnable() {
        super.onEnable();
//        Metrics metrics = new Metrics(this, 692);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "kyllian:captcha");

        if (!getServer().getPluginManager().isPluginEnabled(this))
            return;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        saveResource("background.png", false);
        saveResource("background_ja.png", false);

        captchaHandler = new CaptchaHandler(this);
        fontHandler = new FontHandler(this);
        mapHandler = new MapHandlerFactory(this).getMapHandler();
        messageHandler = new MessageHandler(this);
        playerDataHandler = new PlayerDataHandler(this);
        safeArea = new SafeArea(this);
        updateHandler = new UpdateHandler(this);

        statusRecord = new StatusRecord(this);

        loadListeners();

        new CaptchaCommand(this);

        Bukkit.getOnlinePlayers().forEach(playerDataHandler::loadPlayerDataFromPlayer);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        captchaHandler.removeAllCaptchas();
        statusRecord.closeConnection();
    }

    public void loadListeners() {
        new EntityDamageListener(this);
        new InventoryClickListener(this);
        new PlayerChatListener(this);
        new PlayerCommandPreprocessListener(this);
        new PlayerDeathListener(this);
        new PlayerDropItemListener(this);
        new PlayerInteractEntityListener(this);
        new PlayerInteractListener(this);
        new PlayerItemHeldListener(this);
        if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) new LoginListener(this);
        else new PlayerJoinListener(this);
        new PlayerMoveListener(this);
        new PlayerQuitListener(this);
        new PlayerRespawnListener(this);
        new PlayerSwapHandItemsListener(this);

    }

    public CaptchaHandler getCaptchaHandler() {
        return captchaHandler;
    }

    public FontHandler getFontHandler() {
        return fontHandler;
    }

    public MapHandler getMapHandler() {
        return mapHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public PlayerDataHandler getPlayerDataHandler() {
        return playerDataHandler;
    }

    public SafeArea getSafeArea() {
        return safeArea;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public StatusRecord getStatusRecord() {
        return statusRecord;
    }

    public int getMaxPlayerWithoutCaptcha() {
        int maxPlayer = this.getConfig().getInt("captcha-settings.max-player");
        if (maxPlayer == -1) {
            return Bukkit.getServer().getMaxPlayers() + 1;
        } else {
            return maxPlayer;
        }
    }

}
