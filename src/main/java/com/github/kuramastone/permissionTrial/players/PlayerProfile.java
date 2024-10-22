package com.github.kuramastone.permissionTrial.players;

import com.github.kuramastone.permissionTrial.PermissionTrial;
import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.utils.ComponentEditor;
import com.github.kuramastone.permissionTrial.utils.Nametag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerProfile {

    /**
     * A player's current group
     */
    @Nullable
    private PermissionGroup currentGroup;

    /**
     * Player group will revert to this after expiration expires
     */
    @Nullable
    private PermissionGroup groupAfterExpiration;

    /**
     * Time in milliseconds when group will expire
     */
    private long expirationTime = -1L;

    /**
     * Player identity
     */
    private UUID playerUUID;

    /**
     * Permission atttachment to track
     */
    private PermissionAttachment permissionAttachment;

    /**
     * Player Object
     */
    private Player player;

    /**
     * OfflinePlayer Object
     */
    private OfflinePlayer offlinePlayer;

    /**
     * Used for storing and tracking the player's nametag
     */
    private Nametag nametag;

    public PlayerProfile(PermissionsApi api, UUID playerUUID, PermissionGroup currentGroup, long expirationTime, @Nullable String groupAfterExpiration) {
        this.currentGroup = currentGroup == null ? api.getDefaultGroup() : currentGroup;
        this.expirationTime = expirationTime;
        this.playerUUID = playerUUID;
        this.groupAfterExpiration = (groupAfterExpiration == null || groupAfterExpiration.isEmpty()) ? api.getDefaultGroup() : api.getGroupByName(groupAfterExpiration);

        // This can be null during testing, so we check.
        if (Bukkit.getServer() != null)
            offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
    }

    /**
     * Builds a nametag for the player with the current group's prefix
     *
     * @return
     */
    public @Nullable Nametag getOrCreateNametag() {
        if (!offlinePlayer.isOnline()) {
            return null;
        }

        if (currentGroup == null) {
            nametag = null;
            return null;
        }

        if (nametag == null) {
            nametag = new Nametag(getPlayer());
        }
        nametag.setPrefix(ComponentEditor.decorateComponent(this.getCurrentPermissionGroup().getGroupPrefix()));

        return nametag;
    }

    /**
     * Broadcasts nametag to all players
     */
    public void broadcastNametag() {
        Nametag tag = getOrCreateNametag();
        if (tag != null)
            tag.broadcast();
    }

    /**
     * Removes old permissions and applies new ones
     *
     * @param newGroup
     */
    public void setPermissionGroup(PermissionGroup newGroup) {
        setPermissionGroup(newGroup, -1L, null);
    }

    /**
     * Removes old permissions and applies new ones
     *
     * @param newGroup
     * @param expirationTime Time that it will expire in milliseconds according to System.currentTimeMillis(). Set to -1 to make it last indefinitely.
     */
    public void setPermissionGroup(PermissionGroup newGroup, long expirationTime, @Nullable PermissionGroup nextGroup) {
        this.currentGroup = newGroup;
        this.expirationTime = expirationTime;
        this.groupAfterExpiration = nextGroup;

        refreshGroupStatus();
        broadcastNametag();
    }

    public void refreshGroupStatus() {
        if (currentGroup == null)
            return;
        Player player = getPlayer();

        // cant modify offline player persm
        if(!isOnline()) {
            return;
        }

        // remove all permissions by disposing of attachment
        if (permissionAttachment != null) {
            player.removeAttachment(permissionAttachment);
        }

        // add everything within current groups
        refreshGroupPermissions(currentGroup, true);


    }

    /**
     * Refreshes permissions that this player has
     */
    public void refreshGroupPermissions(PermissionGroup group, boolean removeOldPermissions) {
        if (group == null)
            return;
        Player player = getPlayer();

        if (player == null) {
            throw new RuntimeException("Cannot modify the permissions of an offline player!");
        }

        if (permissionAttachment != null && removeOldPermissions) {
            permissionAttachment.remove();
        }
        permissionAttachment = getPlayer().addAttachment(PermissionTrial.instance);

        // add base permissions
        for (String permission : group.getGroupPermissions()) {
            permissionAttachment.setPermission(permission, true);
            //PermissionTrial.logger.info("Adding permission %s from group %s".formatted(permission, group.getGroupName()));
        }

        // add inherited permissions
        for (PermissionGroup child : group.getInheritsFromTheseGroups()) {
            refreshGroupPermissions(child, false);
        }

    }

    public Player getPlayer() {
        // keep player object cached, but not if theyre offline
        if (offlinePlayer.isOnline()) {
            if (player == null) {
                player = offlinePlayer.getPlayer().getPlayer();
            }

            return player;
        }
        player = null;

        return null;
    }

    public PermissionGroup getCurrentPermissionGroup() {
        return currentGroup;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public @Nullable PermissionGroup getGroupAfterExpiration() {
        return groupAfterExpiration;
    }

    public UUID getUUID() {
        return playerUUID;
    }

    @Override
    public String toString() {
        return "PlayerProfile{" +
                "currentGroup=" + (currentGroup == null ? "" : currentGroup.getGroupName()) +
                ", groupAfterExpiration=" + (groupAfterExpiration == null ? null : groupAfterExpiration.getGroupName()) +
                ", expirationTime=" + expirationTime +
                ", playerUUID=" + playerUUID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerProfile that = (PlayerProfile) o;
        return Objects.equals(playerUUID, that.playerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(playerUUID);
    }

    public boolean contains(PermissionGroup pg) {
        if (currentGroup == pg) {
            return true;
        }
        if (currentGroup == null) {
            return false;
        }

        return currentGroup.doesInheritFrom(pg);
    }

    public String getName() {
        return offlinePlayer.getName();
    }

    public boolean hasGroupExpired() {
        if (isCurrentGroupPermanent()) {
            return false; // will never expire
        }

        return System.currentTimeMillis() >= expirationTime;
    }

    public void onJoin() {
        broadcastNametag();
        refreshGroupStatus();
    }

    public void onLogOut() {
        permissionAttachment = null;
    }

    public Nametag getNametag() {
        return nametag;
    }

    /**
     * Receives the nametags of all online profiles
     *
     * @param api
     */
    public void receiveAllNametags(PermissionsApi api) {
        for (PlayerProfile profile : api.getProfiles()) {
            if (profile.isOnline()) {
                Nametag tag = profile.getOrCreateNametag();
                if (tag != null)
                    tag.sendTo(getPlayer());
            }
        }
    }

    public boolean isOnline() {
        return offlinePlayer.isOnline();
    }

    public boolean isCurrentGroupPermanent() {
        return expirationTime == -1;
    }
}






