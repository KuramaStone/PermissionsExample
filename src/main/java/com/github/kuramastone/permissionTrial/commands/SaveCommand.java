package com.github.kuramastone.permissionTrial.commands;

import com.github.kuramastone.permissionTrial.PermissionTrial;
import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.List;

public class SaveCommand extends SubCommand {
    
    private boolean isSaving = false;
    
    public SaveCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
        setPermission(PermissionsApi.saveCommandPermission);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation()) {
            sender.sendMessage("Usage: /%s".formatted(getFullCommandString()));
            return true;
        }
        
        if(isSaving) {
            sender.sendMessage(api.getMessage("commands.%s.success.already_saving".formatted(getSubcommand())).build());
            return true;
        }

        // run async to avoid stalling
        PermissionTrial.instance.getServer().getScheduler().
                runTaskAsynchronously(PermissionTrial.instance, () -> {
                    isSaving = true;
                    /*
                    From my experiments, sendMessage is thread-safe and should cause no issues.
                     */
                    sender.sendMessage(api.getMessage("commands.%s.success.start".formatted(getSubcommand())).build());
                    try {
                        api.saveAll();
                        sender.sendMessage(api.getMessage("commands.%s.success.completed".formatted(getSubcommand())).build());
                    }
                    catch (SQLException e) {
                        sender.sendMessage(api.getMessage("commands.%s.success.error".formatted(getSubcommand())).build());
                        isSaving = false;
                        throw new RuntimeException(e);
                    }
                    isSaving = false;
                });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == getArgumentLocation() + 1) {
            return predictSuggestionStartingWith(args[getArgumentLocation()], getGroupSuggestions());
        }

        return tabCompleteChildren(sender, args);
    }
}
