package com.github.kuramastone.permissionTrial.groups;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import org.bukkit.permissions.Permission;

import java.util.*;

public class PermissionGroup {

    /**
     * Identifier for the group
     */
    private String groupName;

    /**
     * Prefix for chat and when joining
     */
    private String groupPrefix;

    /**
     * What other group this inherits from. Stored as identifiers for each group.
     */
    private Set<String> inheritsFromTheseRawGroups;

    /**
     * What other group this inherits from. Will be synced after all groups are loaded
     */
    private Set<PermissionGroup> inheritsFromTheseGroups;

    /**
     * Permissions given to members of this group
     */
    private Set<String> groupPermissions;

    public PermissionGroup(String groupName, String groupPrefix, List<String> inheritsFromTheseRawGroups, List<String> groupPermissions) {
        this.groupName = groupName;
        this.groupPrefix = groupPrefix;
        this.inheritsFromTheseRawGroups = new HashSet<>(inheritsFromTheseRawGroups);  // get rid of any unmodifiable
        this.groupPermissions = new HashSet<>(groupPermissions); // get rid of any unmodifiable

        // remove empty entries
        this.inheritsFromTheseRawGroups.removeIf(str -> str == null || str.isEmpty());
    }

    /**
     * Loads groups stored as strings as their proper PermissionGroup objects
     */
    public void syncRawInheritanceGroups(PermissionsApi api) {
        inheritsFromTheseGroups = new HashSet<>();

        for (String rawGroupName : this.inheritsFromTheseRawGroups) {
            inheritsFromTheseGroups.add(api.getGroupByName(rawGroupName));
        }

    }

    /**
     * Adds this permission to the list
     *
     * @param permission
     */
    public void addGroupPermission(String permission) {
        if (!this.groupPermissions.contains(permission))
            this.groupPermissions.add(permission);
    }

    /**
     * Remove permission from list
     *
     * @param permission
     * @return True if list contained this permission
     */
    public boolean removeGroupPermission(String permission) {
        return this.groupPermissions.remove(new Permission(permission));
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupPrefix() {
        return groupPrefix;
    }

    public void setGroupPrefix(String groupPrefix) {
        this.groupPrefix = groupPrefix;
    }

    public Set<String> getInheritsFromTheseRawGroups() {
        return inheritsFromTheseRawGroups;
    }

    public void setInheritsFromTheseRawGroups(Set<String> inheritsFromTheseRawGroups) {
        this.inheritsFromTheseRawGroups = inheritsFromTheseRawGroups;
    }

    public Set<PermissionGroup> getInheritsFromTheseGroups() {
        return inheritsFromTheseGroups;
    }

    public void setInheritsFromTheseGroups(HashSet<PermissionGroup> inheritsFromTheseGroups) {
        this.inheritsFromTheseGroups = inheritsFromTheseGroups;
    }

    public Set<String> getGroupPermissions() {
        return groupPermissions;
    }

    public void setGroupPermissions(Set<String> groupPermissions) {
        this.groupPermissions = groupPermissions;
    }

    @Override
    public String toString() {
        return "PermissionGroup{" +
                "groupName='" + groupName + '\'' +
                ", groupPrefix='" + groupPrefix + '\'' +
                ", inheritsFromTheseRawGroups=" + inheritsFromTheseRawGroups +
                ", groupPermissions=" + groupPermissions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionGroup that = (PermissionGroup) o;

        return Objects.equals(groupName, that.groupName) && Objects.equals(groupPrefix, that.groupPrefix) && Objects.equals(inheritsFromTheseRawGroups, that.inheritsFromTheseRawGroups) && Objects.equals(groupPermissions, that.groupPermissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, groupPrefix, inheritsFromTheseRawGroups, groupPermissions);
    }

    public void addInheritanceUnsynced(String otherGroup) {
        this.inheritsFromTheseRawGroups.add(otherGroup);
    }

    public void removeInheritanceUnsynced(String otherGroup) {
        this.inheritsFromTheseRawGroups.remove(otherGroup);
    }

    public boolean doesInheritFrom(PermissionGroup pg) {
        if (this.inheritsFromTheseGroups.contains(pg)) {
            return true;
        }

        for (PermissionGroup child : this.inheritsFromTheseGroups) {
            if (child.doesInheritFrom(pg)) {
                return true;
            }
        }

        return false;
    }
}
















