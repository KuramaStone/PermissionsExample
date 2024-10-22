package com.github.kuramastone.permissionTrial;

import com.github.kuramastone.permissionTrial.commands.ParentCommand;
import com.github.kuramastone.permissionTrial.listeners.PlayerListener;
import com.github.kuramastone.permissionTrial.papi.GroupPlaceholderExpansion;
import com.github.kuramastone.permissionTrial.schedulers.ExpirationScheduler;
import com.github.kuramastone.permissionTrial.schedulers.SaveScheduler;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

public final class PermissionTrial extends JavaPlugin {

    public static PermissionTrial instance;
    public static Logger logger;
    private PermissionsApi api;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        createAPI();

        registerSchedulers();
        registerListeners();
        registerCommands();
        registerPapi();
    }

    /**
     * Attempts to create the api instance, and if it fails (most likely due to database failure), it will shutdown the plugin.
     *
     * @return
     */
    private boolean createAPI() {
        /*
            This could be tidier with ways to reconnect the database, but I'm honestly quite busy and need to work on a Forge Mod commission.
         */
        try {
            api = new PermissionsApi();
        }
        catch (Exception e) {
            logger.severe("Unable to load. Please enter a proper database.");
            e.printStackTrace();
            setEnabled(false);
            return false;
        }

        return true;
    }

    private void registerCommands() {
        // all subcommands register within this.
        new ParentCommand(api).register(this);
    }

    /**
     * Only registers if the {@link me.clip.placeholderapi.PlaceholderAPI}
     */
    private void registerPapi() {
        //register placeholder
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new GroupPlaceholderExpansion(api).register();
        }
    }

    private void registerListeners() {
        new PlayerListener(api).register(this);
    }

    private void registerSchedulers() {
        // check for expired permission groups
        new ExpirationScheduler(api).start(this);
        // periodically save to databases
        new SaveScheduler(api).start(this);
    }

    @Override
    public void onDisable() {
        try {
            api.saveAll();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
