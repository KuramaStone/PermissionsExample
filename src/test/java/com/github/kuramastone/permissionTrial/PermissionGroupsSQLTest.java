package com.github.kuramastone.permissionTrial;

import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.mysql.PermissionGroupsSQL;
import com.github.kuramastone.permissionTrial.mysql.SQLDatabase;
import com.github.kuramastone.permissionTrial.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.*;
/**
 * Tests PermissionGroup saving and loading
 */
public class PermissionGroupsSQLTest {

    private SQLDatabase sqlDatabase;
    private Map<String, PermissionGroup> groupsByName;

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

        // create some dummy data
        groupsByName = new LinkedHashMap<>();

        List<String> inheritsFrom = Arrays.asList("groupA", "groupB");
        List<String> permissions = Arrays.asList("perm1", "perm2");
        groupsByName.put("group1", new PermissionGroup("group1", "prefix1", inheritsFrom, permissions));
        groupsByName.put("group2", new PermissionGroup("group2", "prefix2", inheritsFrom, permissions));
    }

    @Test
    public void testSavingAndLoadingGroups() throws SQLException {
        PermissionGroupsSQL.saveAllGroups(sqlDatabase, groupsByName);
        Map<String, PermissionGroup> loadedGroups = PermissionGroupsSQL.loadAllGroups(sqlDatabase);


        //compare

        Set<String> allNames = new HashSet<>();
        allNames.addAll(loadedGroups.keySet());
        allNames.addAll(this.groupsByName.keySet());

        // compare each group
        for(String groupName : allNames) {
            Assertions.assertEquals(groupsByName.get(groupName), loadedGroups.get(groupName));
        }
    }

}
