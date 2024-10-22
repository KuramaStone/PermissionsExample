package com.github.kuramastone.permissionTrial.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLDatabase {

    private HikariConfig database;
    private String databaseName;
    private boolean useH2;

    /**
     *
     * @param url Url to connect to
     * @param databaseName Name of the database
     * @param user User identification
     * @param password Password for database
     * @param useH2 If true, this will connect to a local H2 database
     */
    public SQLDatabase(String url, String databaseName, String user, String password, boolean useH2) {
        this.databaseName = databaseName;
        this.useH2 = useH2;

        database = new HikariConfig();
        database.setJdbcUrl(url);
        database.setUsername(user);
        database.setPassword(password);
        database.setMaximumPoolSize(4);
        if (useH2)
            database.setDriverClassName("org.h2.Driver"); // Specify the H2 driver
        else
            database.setDriverClassName("com.mysql.cj.jdbc.Driver"); // Use normal MySQL
        database.setConnectionTimeout(30000L); // 30 seconds
        database.setDataSource(new HikariDataSource(database));

        try {
            initializeDatabase();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return database.getDataSource().getConnection();
    }

    /**
     * Prepares database for usage
     * @throws SQLException
     */
    public void initializeDatabase() throws SQLException {
        // Create database
        if (!useH2) {
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {

                String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS %s".formatted(databaseName);
                statement.execute(createDatabaseSQL);
                statement.execute("USE " + databaseName);
            }

        }

        // create tables
        PermissionGroupsSQL.createSchema(this);
        PlayerSQL.createSchema(this);
    }
}
