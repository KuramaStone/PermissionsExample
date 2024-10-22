package com.github.kuramastone.permissionTrial;

import com.github.kuramastone.permissionTrial.mysql.SQLDatabase;
import org.h2.engine.Database;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Tests if the database loads without issue
 */
public class SQLDatabaseTest {

    private SQLDatabase sqlDatabase;

    @BeforeEach
    public void setup() {
        boolean useH2 = true;

        String url;
        if (useH2)
            url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // In-memory database URL
        else
            url = "jdbc:mysql://localhost:3306/"; // local mysql server

        String databaseName = "testdb";
        String user = "regiondataplugin";
        String password = "securepassword123";

        sqlDatabase = new SQLDatabase(url, databaseName, user, password, useH2);
    }

    @Test
    public void testGetConnection() throws SQLException {
        Connection connection = sqlDatabase.getConnection();
        Assertions.assertNotNull(connection);
        Assertions.assertFalse(connection.isClosed());
        connection.close(); // Clean up
    }

    @Test
    public void testInitializeDatabase() throws SQLException {
        Assertions.assertDoesNotThrow(() -> {
            sqlDatabase.initializeDatabase();
        });
    }
}
