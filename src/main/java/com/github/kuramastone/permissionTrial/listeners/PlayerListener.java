package com.github.kuramastone.permissionTrial.listeners;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import com.github.kuramastone.permissionTrial.utils.ComponentEditor;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    private PermissionsApi api;

    public PlayerListener(PermissionsApi api) {
        this.api = api;
    }

    /**
     * Decorate join message with prefix
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void modifyJoinMessage(PlayerJoinEvent event) {
        PlayerProfile profile = api.getOrCreatePlayerProfile(event.getPlayer().getUniqueId());

        PermissionGroup group = profile.getCurrentPermissionGroup();
        if (group != null) {
            Component original = event.joinMessage();
            Component decorated = ComponentEditor.decorateComponent(group.getGroupPrefix()).append(original);
            event.joinMessage(decorated);
        }
    }
    /**
     * Decorate quit message with prefix
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void modifyQuitMessage(PlayerQuitEvent event) {
        PlayerProfile profile = api.getOrCreatePlayerProfile(event.getPlayer().getUniqueId());

        PermissionGroup group = profile.getCurrentPermissionGroup();
        if (group != null) {
            Component original = event.quitMessage();
            Component decorated = ComponentEditor.decorateComponent(group.getGroupPrefix()).append(original);
            event.quitMessage(decorated);
        }
    }

    /**
     * Decorate chat with prefix
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void modifyChatMessage(AsyncPlayerChatEvent event) {
        PlayerProfile profile = api.getOrCreatePlayerProfile(event.getPlayer().getUniqueId());

        PermissionGroup group = profile.getCurrentPermissionGroup();
        if (group != null) {
            event.setFormat(ChatColor.translateAlternateColorCodes('&', group.getGroupPrefix().replace("\\s", " ") + "&r") + event.getFormat());
        }
    }


    /**
     * Broadcast the player nametag and call onJoin
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void callOnJoin(PlayerJoinEvent event) {
        PlayerProfile profile = api.getOrCreatePlayerProfile(event.getPlayer().getUniqueId());
        profile.onJoin();
        profile.receiveAllNametags(api); // now receive the nametags
    }

    /**
     * Call PlayerProfile#onLogOut
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void callOnLogout(PlayerQuitEvent event) {
        PlayerProfile profile = api.getOrCreatePlayerProfile(event.getPlayer().getUniqueId());
        profile.onLogOut();
    }

    public void register(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
