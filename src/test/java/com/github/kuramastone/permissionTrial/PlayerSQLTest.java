package com.github.kuramastone.permissionTrial;

import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.mysql.PermissionGroupsSQL;
import com.github.kuramastone.permissionTrial.mysql.PlayerSQL;
import com.github.kuramastone.permissionTrial.mysql.SQLDatabase;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

/**
 * Tests player saving and loading
 */
public class PlayerSQLTest {

    private SQLDatabase sqlDatabase;
    private PermissionsApi api;

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
        Map<String, PermissionGroup> groupsByName = new LinkedHashMap<>();

        List<String> inheritsFrom = Arrays.asList("groupA", "groupB");
        List<String> permissions = Arrays.asList("perm1", "perm2");
        groupsByName.put("group1", new PermissionGroup("group1", "prefix1", inheritsFrom, permissions));
        groupsByName.put("group2", new PermissionGroup("group2", "prefix2", inheritsFrom, permissions));

        api = new PermissionsApi(groupsByName);
    }

    @Test
    public void testSavingAndLoadingGroups() throws SQLException {
        Map<UUID, PlayerProfile> dummies = new LinkedHashMap<>();
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        dummies.put(uuid1,  new PlayerProfile(api, uuid1, api.getGroupByName("group1"), -1, null));
        dummies.put(uuid2, new PlayerProfile(api, uuid2, api.getGroupByName("group2"), 123456789, "group1"));
        dummies.put(uuid2, new PlayerProfile(api, uuid2, null, 123456789, null));

        PlayerSQL.saveAllProfiles(sqlDatabase, dummies);
        Map<UUID, PlayerProfile> loadedProfiles = PlayerSQL.loadAllProfiles(api, sqlDatabase);

        Assertions.assertEquals(dummies.get(uuid1).toString(), loadedProfiles.get(uuid1).toString());
        Assertions.assertEquals(dummies.get(uuid2).toString(), loadedProfiles.get(uuid2).toString());
    }

}
