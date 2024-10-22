package com.github.kuramastone.permissionTrial.commands.groupcmd;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.SubCommand;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public class RemoveInheritGroupCommand extends SubCommand {
    public RemoveInheritGroupCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation() + 2) {
            sender.sendMessage("Usage: /%s [group] [group to inherit]".formatted(getFullCommandString()));
            return true;
        }

        String groupName = args[getArgumentLocation()];
        String otherGroup = args[getArgumentLocation()+1];

        if (api.getGroupByName(groupName) == null) {
            sender.sendMessage(api.getMessage("commands.group_doesnt_exists", "{group}", groupName).build());
            return true;
        }
        PermissionGroup pg = api.getGroupByName(groupName);

        sender.sendMessage(api.getMessage("commands.%s.success".formatted(getSubcommand()), "{group}", groupName, "{inheritGroup}", otherGroup).build());
        pg.removeInheritanceUnsynced(otherGroup);
        api.refreshPlayerPermissions(pg);


        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == getArgumentLocation() + 1) {
            return predictSuggestionStartingWith(args[getArgumentLocation()], getGroupSuggestions());
        }
        if (args.length == getArgumentLocation() + 2) {
            return predictSuggestionStartingWith(args[getArgumentLocation()+1], getInheritedGroupsSuggestions(api.getGroupByName(args[getArgumentLocation()])));
        }

        return tabCompleteChildren(sender, args);
    }
}
