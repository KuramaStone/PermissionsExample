package com.github.kuramastone.permissionTrial.mysql;

import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.utils.StringUtils;

import java.sql.*;
import java.util.*;

public class PermissionGroupsSQL {


    /**
     * Erases old data and inserts new group data
     * @param database
     * @param groupsByName
     * @throws SQLException
     */
    public static void saveAllGroups(SQLDatabase database, Map<String, PermissionGroup> groupsByName) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM `groups` WHERE groupName = ?";
        String insertQuery = "INSERT INTO `groups` (groupName, groupPrefix, inheritsFromTheseGroupNames, permissions) VALUES (?, ?, ?, ?)";
        String updateQuery = "UPDATE `groups` SET groupPrefix = ?, inheritsFromTheseGroupNames = ?, permissions = ? WHERE groupName = ?";
        String deleteQuery = "DELETE FROM `groups` WHERE groupName = ?";

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit

            try (
                    PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
                    PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                    PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                    PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)
            ) {


                // delete groups that are not present in the groupsByName map
                String fetchAllGroupsQuery = "SELECT groupName FROM `groups`";
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(fetchAllGroupsQuery)) {
                    while (rs.next()) {
                        String existingGroupName = rs.getString("groupName");
                        if (!groupsByName.containsKey(existingGroupName)) {
                            deleteStmt.setString(1, existingGroupName);
                            deleteStmt.executeUpdate();
                        }
                    }
                }

                // now we add the groups
                for (PermissionGroup group : groupsByName.values()) {
                    String groupName = group.getGroupName();
                    String groupPrefix = group.getGroupPrefix();

                    // Convert lists to comma-separated strings
                    String inheritsFromTheseRawGroups = String.join(",", group.getInheritsFromTheseRawGroups());
                    String permissionList = String.join(",", group.getGroupPermissions());

                    // Check if the group exists
                    selectStmt.setString(1, groupName);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            // Update the existing group
                            updateStmt.setString(1, groupPrefix);
                            updateStmt.setString(2, inheritsFromTheseRawGroups);
                            updateStmt.setString(3, permissionList);
                            updateStmt.setString(4, groupName);
                            updateStmt.executeUpdate();
                        }
                        else {
                            // Insert a new group
                            insertStmt.setString(1, groupName);
                            insertStmt.setString(2, groupPrefix);
                            insertStmt.setString(3, inheritsFromTheseRawGroups);
                            insertStmt.setString(4, permissionList);
                            insertStmt.executeUpdate();
                        }
                    }
                }

                connection.commit(); // Commit the transaction after all operations
            }
            catch (SQLException e) {
                connection.rollback(); // Roll back if there’s an error
                throw e;
            }
        }
    }

    /**
     * Used to load all groups from the database
     *
     * @param database
     * @return
     * @throws SQLException
     */
    public static Map<String, PermissionGroup> loadAllGroups(SQLDatabase database) throws SQLException {
        Map<String, PermissionGroup> groupsByName = new LinkedHashMap<>();

        try (Connection connection = database.getConnection()) {
            // Get all regions
            String getAllGroups = "SELECT id, groupName, groupPrefix, inheritsFromTheseGroupNames, permissions FROM `groups`";
            try (PreparedStatement stmt = connection.prepareStatement(getAllGroups);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String name = rs.getString("groupName");
                    String prefix = rs.getString("groupPrefix");

                    String compressedInheritanceList = rs.getString("inheritsFromTheseGroupNames");
                    List<String> inheritsFromTheseRawGroups = Arrays.asList(compressedInheritanceList.split(","));

                    String compressedPermissionList = rs.getString("permissions");
                    List<String> permissionList = Arrays.asList(compressedPermissionList.split(","));


                    groupsByName.put(name, new PermissionGroup(name, prefix, inheritsFromTheseRawGroups, permissionList));
                }
            }
        }

        return groupsByName;
    }

    public static void createSchema(SQLDatabase sqlDatabase) throws SQLException {
        String schema = """
                CREATE TABLE IF NOT EXISTS `groups` (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    groupName VARCHAR(255) NOT NULL,
                    groupPrefix VARCHAR(255),
                    inheritsFromTheseGroupNames TEXT,
                    permissions TEXT
                )
                """;

        // split schema into individual statements
        String[] statements = schema.split("(?<=;)\s*");

        try (Connection conn = sqlDatabase.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
