package online.ettarp.ettawhitelist.db;

import online.ettarp.ettawhitelist.EttaWhitelist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHandler {
    EttaWhitelist plugin;
    Connection connection;

    private final String HOSTNAME;
    private final String USERNAME;
    private final String PASSWORD;
    private final String DATABASE;

    public DBHandler(EttaWhitelist plugin) {
        this.plugin = plugin;

        HOSTNAME = "jdbc:mysql://" + plugin.getConfig().getString("db.hostname") + ":3306/";
        USERNAME = plugin.getConfig().getString("db.username");
        PASSWORD = plugin.getConfig().getString("db.password");
        DATABASE = plugin.getConfig().getString("db.database");

        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            newConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Connection getConnection() throws SQLException {
        if(connection == null) {
            newConnection();
        }

        return connection;
    }

    private void newConnection() throws SQLException {
        connection = DriverManager.getConnection(HOSTNAME + DATABASE, USERNAME, PASSWORD);
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }
}
