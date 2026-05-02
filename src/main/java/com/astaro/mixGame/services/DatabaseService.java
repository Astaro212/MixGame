package com.astaro.mixGame.services;

import com.astaro.mixGame.MixGame;
import com.astaro.mixGame.data.PlayerStats;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DatabaseService {
    private final MixGame plugin;
    private Connection connection;
    private String url;

    public DatabaseService(MixGame plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        var dbSettings = plugin.getSettings().db();

        try {
            if (dbSettings.type().equalsIgnoreCase("MySQL")) {
                url = "jdbc:mysql://" + dbSettings.host() + ":" + dbSettings.port() + "/" + dbSettings.database() + "?autoReconnect=true";
                connection = DriverManager.getConnection(url, dbSettings.username(), dbSettings.password());
            } else {
                File dbFile = new File(plugin.getDataFolder(), "database.db");
                url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url);
            }
            createTable();
            plugin.getLogger().info("База данных подключена (" + dbSettings.type() + ")");
        } catch (SQLException e) {
            plugin.getLogger().severe(" Ошибка подключения к БД: " + e.getMessage());
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS MixGame (" +
                "username VARCHAR(100) PRIMARY KEY, " +
                "points INT DEFAULT 0, won INT DEFAULT 0, lost INT DEFAULT 0);";
        execute(sql);
    }


    public void updateStatsAsync(String username, int points, int won, int lost) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = getSql(plugin.getSettings().db().type().equalsIgnoreCase("MySQL"));
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setInt(2, points);
                ps.setInt(3, won);
                ps.setInt(4, lost);
                ps.executeUpdate();
                if (plugin.getPlaceholderService() != null) {
                    plugin.getPlaceholderService().invalidateCache(username);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private String getSql(boolean mysql) {
        if (mysql) {
            return "INSERT INTO MixGame (username, points, won, lost) VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE points = points + VALUES(points), won = won + VALUES(won), lost = lost + VALUES(lost);";
        }
        return "INSERT INTO MixGame (username, points, won, lost) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(username) DO UPDATE SET points = points + excluded.points, won = won + excluded.won, lost = lost + excluded.lost;";

    }

    public CompletableFuture<List<String>> getTop(String column, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> top = new ArrayList<>();
            String sql = "SELECT username, " + column + " FROM MixGame ORDER BY " + column + " DESC LIMIT ?;";
            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, limit);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    top.add(rs.getString("username") + ":" + rs.getInt(column));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return top;
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public CompletableFuture<Optional<PlayerStats>> getUserStats(String username) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT points, won, lost FROM MixGame WHERE username = ? LIMIT 1;";

            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new PlayerStats(
                                rs.getInt("points"),
                                rs.getInt("won"),
                                rs.getInt("lost")
                        ));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Ошибка при получении статистики " + username + ": " + e.getMessage());
            }
            return Optional.empty();
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }


    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    private void execute(String sql) {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
