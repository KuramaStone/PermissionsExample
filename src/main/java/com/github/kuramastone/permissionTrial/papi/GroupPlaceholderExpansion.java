package com.github.kuramastone.permissionTrial.papi;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import com.github.kuramastone.permissionTrial.utils.StringUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GroupPlaceholderExpansion extends PlaceholderExpansion {

    private final PermissionsApi api;

    public GroupPlaceholderExpansion(PermissionsApi api) {
        this.api = api;
    }


    @Override
    public String getAuthor() {
        return "kuramastone";
    }

    @Override
    public String getIdentifier() {
        return "groupperms";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Whether this expansion should persist and not be unregistered automatically
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        return parse(player == null ? null : player.getUniqueId(), params);
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        return parse(player == null ? null : player.getUniqueId(), params);
    }

    private String parse(UUID uuid, String params) {
        if (uuid == null) {
            return "Bad_UUID";
        }

        PlayerProfile pp = api.getOrCreatePlayerProfile(uuid);
        if (params.equalsIgnoreCase("group")) {

            if (pp.getCurrentPermissionGroup() != null) {
                return pp.getCurrentPermissionGroup().getGroupName();
            }
            else {
                return api.getDefaultGroup() == null ? "NULL" : api.getDefaultGroup().getGroupName();
            }
        }
        else if (params.equalsIgnoreCase("expiration")) {

            if (pp.getCurrentPermissionGroup() != null && !pp.isCurrentGroupPermanent()) {
                String result = StringUtils.millisecondsToReadable(pp.getExpirationTime() - System.currentTimeMillis());
                return result;
            }
            else {
                return "Never";
            }
        }

        return null;
    }
}
