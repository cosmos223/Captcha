package me.kyllian.captcha.spigot.sql;

import me.kyllian.captcha.spigot.CaptchaPlugin;
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

    private Connection connection;
    private String host, database, username, password;
    private int port;
    private Boolean isUseSQL = false;

    private File file;
    private FileConfiguration fileConfiguration;

    public StatusRecord(CaptchaPlugin plugin) {
        this.plugin = plugin;
        file = new File(
                plugin.getDataFolder(), "config_db.yml");

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
            try {
                openConnection();

                String sql = "CREATE TABLE IF NOT EXISTS players (uuid varchar(36) PRIMARY KEY, last_pass long, total_fails int, passed BIT);";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.executeUpdate();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadData() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public void setToSQL(Player player, long SolvedTime, int TotalFails, boolean isPassed) {
        if (isUseSQL) {
            try {
                openConnection();
                UUID uuid = player.getUniqueId();

                String sql = "INSERT INTO players (uuid, last_pass, total_fails, passed) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE last_pass = last_pass , total_fails = total_fails , passed = passed;";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setLong(2, SolvedTime);
                preparedStatement.setInt(3, TotalFails);
                preparedStatement.setBoolean(4, isPassed);

                preparedStatement.executeUpdate();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTotalFails(Player player) {
        if (isUseSQL) {
            try {
                openConnection();
                UUID uuid = player.getUniqueId();

                String sql = "UPDATE players SET total_fails = total_fails + 1 , passed = false WHERE uuid = ?;";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, uuid.toString());

                preparedStatement.executeUpdate();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSolvedTime(Player player, long SolvedTime) {
        if (isUseSQL) {
            try {
                openConnection();
                UUID uuid = player.getUniqueId();

                String sql = "UPDATE players SET last_pass = ? , passed = true WHERE uuid = ?;";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setLong(1, SolvedTime);
                preparedStatement.setString(2, uuid.toString());

                preparedStatement.executeUpdate();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getPassed(Player player) {
        try {
            openConnection();
            UUID uuid = player.getUniqueId();
            String sql = "SELECT passed FROM players WHERE uuid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("passed");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long getLastPass(Player player) {
        try {
            openConnection();
            UUID uuid = player.getUniqueId();
            String sql = "SELECT last_pass FROM players WHERE uuid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("last_pass");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    private void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }

    public boolean getUseSQL() {
        return isUseSQL;
    }

}
