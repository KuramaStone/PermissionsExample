package com.github.kuramastone.permissionTrial.schedulers;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import org.bukkit.plugin.java.JavaPlugin;

public class ExpirationScheduler {

    private PermissionsApi api;

    public ExpirationScheduler(PermissionsApi api) {
        this.api = api;
    }

    private void checkExpirations() {
        for (PlayerProfile profile : api.getProfiles()) {
            if(profile.hasGroupExpired()) {
                PermissionGroup next = profile.getGroupAfterExpiration() == null ? api.getDefaultGroup() : profile.getGroupAfterExpiration();
                profile.setPermissionGroup(next);
            }
        }
    }

    public void start(JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkExpirations, 0L, 20L);
    }

}
