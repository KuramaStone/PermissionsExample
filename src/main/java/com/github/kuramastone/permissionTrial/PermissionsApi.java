package com.github.kuramastone.permissionTrial;

import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.mysql.PermissionGroupsSQL;
import com.github.kuramastone.permissionTrial.mysql.PlayerSQL;
import com.github.kuramastone.permissionTrial.mysql.SQLDatabase;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import com.github.kuramastone.permissionTrial.utils.ComponentEditor;
import com.github.kuramastone.permissionTrial.utils.ConfigOptions;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class PermissionsApi {

    public static String groupCommandPermission = "permissions.commands.group";
    public static String playerCommandPermission = "permissions.commands.player";
    public static String infoCommandPermission = "permissions.commands.info";
    public static String queryCommandPermission = "permissions.commands.query";
    /**
     * Loaded groups by groupName
     */
    private Map<String, PermissionGroup> groupsByName;

    /**
     * Loaded PlayerProfiles by UUID
     */
    private Map<UUID, PlayerProfile> profileMap;
    /**
     * Database to saveAll/load to
     */
    private SQLDatabase sqlDatabase;

    /**
     * Storage for config options
     */
    private ConfigOptions configOptions;

    /**
     * Default group for new players
     */
    private PermissionGroup defaultGroup;

    public PermissionsApi() {
        loadConfigs();
        loadDatabase();
        loadFromDatabase();
    }

    /**
     * Calls syncAllPermissionGroups() before updating @{@link PlayerProfile} s
     * Used when modifying a @{@link PermissionGroup}'s permissions/inheritance.
     * Reassigns players permissions if they are a member of this group (either directly or through inheritance)
     *
     * @param pg
     */
    public void refreshPlayerPermissions(PermissionGroup pg) {
        syncAllPermissionGroups();

        for (PlayerProfile profile : getPlayersInGroup(pg)) {
                profile.refreshGroupStatus();
        }
    }

    private Set<PlayerProfile> getPlayersInGroup(PermissionGroup group) {
        Set<PlayerProfile> set = new HashSet<>();

        for(PlayerProfile profile : getProfiles()) {
            if (profile.contains(group)) {
                set.add(profile);
            }
        }

        return set;
    }

    /**
     * Loads saved groups from MySQL
     */
    public void loadConfigs() {
        configOptions = new ConfigOptions();
        configOptions.load();
    }

    /**
     * Update each permission group. This causes the String inherited groups to attach to the @{@link PermissionGroup} instances here.
     */
    public void syncAllPermissionGroups() {
        for (PermissionGroup pg : this.groupsByName.values()) {
            pg.syncRawInheritanceGroups(this);
        }
    }

    /**
     * Loads the database.
     */
    private void loadDatabase() {

        String url;
        if (configOptions.databaseUseH2)
            url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"; // In-memory database URL
        else
            url = configOptions.databaseURL; // local mysql server

        String databaseName = configOptions.databaseName;
        String user = configOptions.databaseUser;
        String password = configOptions.databasePassword;
        sqlDatabase = new SQLDatabase(url, databaseName, user, password, configOptions.databaseUseH2);
    }

    private void loadFromDatabase() {
        // load from database
        try {
            this.groupsByName = PermissionGroupsSQL.loadAllGroups(sqlDatabase);
            syncAllPermissionGroups();
            this.profileMap = PlayerSQL.loadAllProfiles(this, sqlDatabase); // must load AFTER permission groups
        }
        catch (SQLException e) {
            throw new RuntimeException("Unable to load SQL databases!", e);
        }
    }

    /**
     * Used for SQL testing.
     *
     * @param groupsByName
     */
    public PermissionsApi(Map<String, PermissionGroup> groupsByName) {
        this.groupsByName = groupsByName;
    }

    /**
     * Gets a stored profile, creates a new one if it doesnt exist
     * @param uuid
     * @return
     */
    public PlayerProfile getOrCreatePlayerProfile(UUID uuid) {
        return profileMap.computeIfAbsent(uuid, (u_) -> new PlayerProfile(this, uuid, getDefaultGroup(), -1, null));
    }

    public PermissionGroup getGroupByName(String rawGroupName) {
        if(rawGroupName.isEmpty() || rawGroupName == null) {
            return null;
        }
        return groupsByName.get(rawGroupName);
    }

    /**
     * Gets the message as stored in the config via key. Allows easy replacements.
     * @param key Key to search in the yaml file. Should be found at messages.[key] in the config.yml
     * @param replacements Must be a multiple of two. Will replace the first key with the second value
     * @return
     */
    public ComponentEditor getMessage(String key, Object... replacements) {
        ComponentEditor edit = configOptions.messages.getOrDefault(key, new ComponentEditor(key)).copy();

        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("A key was not provided with a replacement.");
        }

        for (int i = 0; i < replacements.length; i += 2) {
            edit = edit.replace(replacements[i + 0].toString(), replacements[i + 1].toString());
        }

        return edit;
    }

    public Collection<PlayerProfile> getProfiles() {
        return this.profileMap.values();
    }

    public Collection<PermissionGroup> getGroups() {
        return this.groupsByName.values();
    }

    public PermissionGroup createGroup(String groupName) {
        PermissionGroup pg = new PermissionGroup(groupName, "[]", new ArrayList<>(), new ArrayList<>());
        pg.syncRawInheritanceGroups(this);
        this.groupsByName.put(groupName, pg);
        return pg;
    }

    public PermissionGroup removeGroup(String groupName) {
        return this.groupsByName.remove(groupName);
    }

    public PermissionGroup getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(PermissionGroup defaultGroup) {
        this.defaultGroup = defaultGroup;
    }

    public void saveAll() throws SQLException {
        saveGroups();
        saveProfiles();
    }

    public synchronized void saveProfiles() throws SQLException {
        PlayerSQL.saveAllProfiles(sqlDatabase, this.profileMap);
    }

    public synchronized void saveGroups() throws SQLException {
        PermissionGroupsSQL.saveAllGroups(sqlDatabase, this.groupsByName);
    }

    /**
     * Broadcast new nametag after rank update
     * @param group
     */
    public void updatePrefixes(PermissionGroup group) {
        if(group == null) {
            return;
        }

        for(PlayerProfile profile : getPlayersInGroup(group)) {
            profile.broadcastNametag();
        }


    }
}













