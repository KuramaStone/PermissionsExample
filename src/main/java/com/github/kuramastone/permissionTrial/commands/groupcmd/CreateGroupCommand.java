package com.github.kuramastone.permissionTrial.commands.groupcmd;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CreateGroupCommand extends SubCommand {
    public CreateGroupCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation()+1) {
            sender.sendMessage("Usage: /%s [group]".formatted(getFullCommandString()));
            return true;
        }

        String groupName = args[getArgumentLocation()];

        if(api.getGroupByName(groupName) != null) {
            sender.sendMessage(api.getMessage("commands.group_already_exists", "{group}", groupName).build());
            return true;
        }

        sender.sendMessage(api.getMessage("commands.%s.success".formatted(getSubcommand()), "{group}", groupName).build());
        api.createGroup(groupName);

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == getArgumentLocation() + 1) {
            return predictSuggestionStartingWith(args[getArgumentLocation()], getGroupSuggestions());
        }

        return tabCompleteChildren(sender, args);
    }
}
