package me.kyllian.captcha.spigot.sql;

import com.zaxxer.hikari.HikariDataSource;
import me.kyllian.captcha.spigot.CaptchaPlugin;
import me.kyllian.captcha.spigot.map.MapHandlerNew;
import me.kyllian.captcha.spigot.map.MapHandlerOld;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class StatusRecord {

    private CaptchaPlugin plugin;
    private String host, database, username, password;
    private int port;
    private Boolean isUseSQL = false;

    private File file;
    private FileConfiguration fileConfiguration;

    private HikariDataSource hikari;

    private String hikariDataSourceClassName = "com.mysql.jdbc.Driver";

    public StatusRecord(CaptchaPlugin plugin) {

        this.plugin = plugin;
        file = new File(
                plugin.getDataFolder(), "config_db.yml");

        String version = Bukkit.getVersion();
        if (version.contains("1.17") || version.contains("1.16")) hikariDataSourceClassName = "com.mysql.cj.jdbc.Driver";
        else hikariDataSourceClassName = "com.mysql.jdbc.Driver";

        try {
            if (!file.exists()) {
                file.createNewFile();
                fileConfiguration = YamlConfiguration.loadConfiguration(file);
                fileConfiguration.set("use-mysql", false);
                fileConfiguration.set("host", "localhost");
                fileConfiguration.set("port", 3306);
                fileConfiguration.set("database", "captcha_mc");
                fileConfiguration.set("username", "root");
                fileConfiguration.set("password", "password");
                try {
                    fileConfiguration.save(file);
                } catch (IOException exception) {
                    Bukkit.getLogger().info("[Captcha] An error occured when saving the config_db file, please report the following error:");
                    exception.printStackTrace();
                }
            } else {
                fileConfiguration = YamlConfiguration.loadConfiguration(file);
            }
        } catch (IOException exception) {
            Bukkit.getLogger().info("[Captcha] An error occured, please report the following error:");
            exception.printStackTrace();
        }

        isUseSQL = fileConfiguration.getBoolean("use-mysql");
        host = fileConfiguration.getString("host");
        port = fileConfiguration.getInt("port");
        database = fileConfiguration.getString("database");
        username = fileConfiguration.getString("username");
        password = fileConfiguration.getString("password");

        if (isUseSQL) {
            hikari = new HikariDataSource();
            hikari.setDriverClassName(hikariDataSourceClassName);
            hikari.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
            hikari.addDataSourceProperty("user", username);
            hikari.addDataSourceProperty("password", password);

            try(Connection connection = hikari.getConnection();
                Statement statement = connection.createStatement();){
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS players (uuid varchar(36) PRIMARY KEY, last_pass long, total_fails int, passed BIT);");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadData() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);

        isUseSQL = fileConfiguration.getBoolean("use-mysql");
        host = fileConfiguration.getString("host");
        port = fileConfiguration.getInt("port");
        database = fileConfiguration.getString("database");
        username = fileConfiguration.getString("username");
        password = fileConfiguration.getString("password");

        closeConnection();

        if (isUseSQL) {
            hikari = new HikariDataSource();
            hikari.setDriverClassName(hikariDataSourceClassName);
            hikari.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
            hikari.addDataSourceProperty("user", username);
            hikari.addDataSourceProperty("password", password);

            //接続を確認
            try (Connection connection = hikari.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT 1;")){
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void setToSQL(Player player, long SolvedTime, int TotalFails, boolean isPassed) {
        UUID uuid = player.getUniqueId();
        if (isUseSQL) {
            try (Connection connection = hikari.getConnection();
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO players (uuid, last_pass, total_fails, passed) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE last_pass = last_pass , total_fails = total_fails , passed = passed;")){
                statement.setString(1, uuid.toString());
                statement.setLong(2, SolvedTime);
                statement.setInt(3, TotalFails);
                statement.setBoolean(4, isPassed);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTotalFails(Player player) {
        if (isUseSQL) {
            try (Connection connection = hikari.getConnection();
                 PreparedStatement update = connection.prepareStatement("UPDATE players SET total_fails = total_fails + 1 , passed = false WHERE uuid = ?;")) {
                update.setString(1, player.getUniqueId().toString());
                update.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSolvedTime(Player player, long SolvedTime) {
        if (isUseSQL) {
            try (Connection connection = hikari.getConnection();
                 PreparedStatement update = connection.prepareStatement("UPDATE players SET last_pass = ? , passed = true WHERE uuid = ?;")){
                update.setLong(1, SolvedTime);
                update.setString(2, player.getUniqueId().toString());
                update.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getPassed(Player player) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT passed FROM players WHERE uuid = ?;")) {
            select.setString(1, player.getUniqueId().toString());
            ResultSet result = select.executeQuery();
            if (result.next())
                return result.getBoolean("passed");
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long getLastPass(Player player) {
        try (Connection connection = hikari.getConnection();
             PreparedStatement select = connection.prepareStatement("SELECT last_pass FROM players WHERE uuid = ?;")) {
            select.setString(1, player.getUniqueId().toString());
            ResultSet result = select.executeQuery();
            if (result.next())
                return result.getLong("last_pass");
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean getUseSQL() {
        return isUseSQL;
    }

    public void closeConnection() {
        if (hikari != null)
            hikari.close();
    }

}
