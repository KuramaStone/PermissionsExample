package com.github.kuramastone.permissionTrial.commands.playercmd;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.SubCommand;
import com.github.kuramastone.permissionTrial.commands.groupcmd.*;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PlayerCommand extends SubCommand {
    public PlayerCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
        setPermission(PermissionsApi.playerCommandPermission);


        registerSubCommand(new DescriptionCommand(api,  this, 2, "help"));
        registerSubCommand(new SetGroupPlayerCommand(api,  this, 2, "setgroup"));

        for(SubCommand cmd : getSubCommandsList()) {
            if(cmd.getDescription() == null) {
                cmd.setDescription(api.getMessage("commands.%s.description".formatted(cmd.getSubcommand())).getText());
            }
        }
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length <= getArgumentLocation()) {
            return subCommands.get("help").execute(sender, args);
        }
        else {
            executeChildren(sender, args);
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return tabCompleteChildren(sender, args);
    }

}
