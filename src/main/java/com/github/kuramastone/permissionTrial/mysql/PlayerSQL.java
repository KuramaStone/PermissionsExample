package com.github.kuramastone.permissionTrial.mysql;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSQL {

    // Method to saveAll all player profiles to the database
    public static void saveAllProfiles(SQLDatabase database, Map<UUID, PlayerProfile> profiles) throws SQLException {
        String insertQuery = "INSERT INTO player_profiles (uuid, expiration_time, current_permission_group, group_after_expiration) VALUES (?, ?, ?, ?)";
        String updateQuery = "UPDATE player_profiles SET expiration_time = ?, current_permission_group = ?, group_after_expiration = ? WHERE uuid = ?";

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit

            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                 PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {

                for (Map.Entry<UUID, PlayerProfile> entry : profiles.entrySet()) {
                    UUID uuid = entry.getKey();
                    PlayerProfile profile = entry.getValue();

                    // Check if the profile already exists
                    if (profileExists(connection, uuid)) {
                        // Update the existing profile
                        updateStmt.setLong(1, profile.getExpirationTime());
                        updateStmt.setString(2, profile.getCurrentPermissionGroup() == null ? "" : profile.getCurrentPermissionGroup().getGroupName());
                        updateStmt.setString(3, profile.getGroupAfterExpiration() == null ? "" : profile.getGroupAfterExpiration().getGroupName());
                        updateStmt.setString(4, uuid.toString());
                        updateStmt.executeUpdate();
                    } else {
                        // Insert a new profile
                        insertStmt.setString(1, uuid.toString());
                        insertStmt.setLong(2, profile.getExpirationTime());
                        insertStmt.setString(3, profile.getCurrentPermissionGroup() == null ? "" : profile.getCurrentPermissionGroup().getGroupName());
                        insertStmt.setString(4, profile.getGroupAfterExpiration() == null ? "" : profile.getGroupAfterExpiration().getGroupName());
                        insertStmt.executeUpdate();
                    }
                }

                connection.commit(); // Commit the transaction after all operations
            } catch (SQLException e) {
                connection.rollback(); // Roll back if there’s an error
                throw e; // Re-throw the exception
            }
        }
    }

    // Method to load all player profiles from the database
    public static Map<UUID, PlayerProfile> loadAllProfiles(PermissionsApi api, SQLDatabase database) throws SQLException {
        Map<UUID, PlayerProfile> profiles = new LinkedHashMap<>();

        try (Connection connection = database.getConnection()) {
            String query = "SELECT uuid, expiration_time, current_permission_group, group_after_expiration FROM player_profiles";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    long expirationTime = rs.getLong("expiration_time");
                    String currentPermissionGroup = rs.getString("current_permission_group");
                    String groupAfterExpiration = rs.getString("group_after_expiration");

                    PlayerProfile profile = new PlayerProfile(api, uuid, api.getGroupByName(currentPermissionGroup), expirationTime, groupAfterExpiration);
                    profiles.put(uuid, profile);
                }
            }
        }

        return profiles;
    }

    // Helper method to check if a profile exists
    private static boolean profileExists(Connection connection, UUID uuid) throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM player_profiles WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public static void createSchema(SQLDatabase sqlDatabase) throws SQLException {
        String schema = """
                CREATE TABLE IF NOT EXISTS player_profiles (
                    uuid CHAR(36) PRIMARY KEY,
                    expiration_time BIGINT NOT NULL,
                    current_permission_group VARCHAR(255) NOT NULL,
                    group_after_expiration VARCHAR(255)
                );
                """;

        try (Connection conn = sqlDatabase.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(schema);
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
