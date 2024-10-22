package com.github.kuramastone.permissionTrial.commands.groupcmd;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GroupCommand extends SubCommand {
    public GroupCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
        setPermission(PermissionsApi.groupCommandPermission);


        registerSubCommand(new DescriptionCommand(api,  this, 2, "help"));
        registerSubCommand(new CreateGroupCommand(api,  this, 2, "create"));
        registerSubCommand(new RemoveGroupCommand(api,  this, 2, "remove"));
        registerSubCommand(new SetPrefixGroupCommand(api,  this, 2, "setprefix"));
        registerSubCommand(new AddPermGroupCommand(api,  this, 2, "addperm"));
        registerSubCommand(new RemovePermGroupCommand(api,  this, 2, "remperm"));
        registerSubCommand(new AddInheritGroupCommand(api,  this, 2, "addinherit"));
        registerSubCommand(new RemoveInheritGroupCommand(api,  this, 2, "reminherit"));
        registerSubCommand(new DefaultGroupCommand(api,  this, 2, "setdefault"));

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
