package com.github.kuramastone.permissionTrial.schedulers;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class SaveScheduler {

    public static long profileSavePeriod = 20 * 60 * 5;
    public static long groupSavePeriod = 20 * 60 * 10;

    private PermissionsApi api;

    public SaveScheduler(PermissionsApi api) {
        this.api = api;
    }

    private void saveProfiles() {
        try {
            api.saveProfiles();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveGroups() {
        try {
            api.saveGroups();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts two schedulers. One for saving {@PlayerProfile}, and one for saving {@PermissionGroup}s.
     * @param plugin
     */
    public void start(JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveProfiles, 0L, profileSavePeriod);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveGroups, 0L, groupSavePeriod);
    }

}
