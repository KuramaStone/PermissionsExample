package com.github.kuramastone.permissionTrial.commands.groupcmd;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.SubCommand;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import io.papermc.paper.entity.CollarColorable;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public class AddInheritGroupCommand extends SubCommand {
    public AddInheritGroupCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation() + 2) {
            sender.sendMessage("Usage: /%s [group] [group to inherit]".formatted(getFullCommandString()));
            return true;
        }

        String groupName = args[getArgumentLocation()];
        String otherGroup = args[getArgumentLocation() + 1];

        if (api.getGroupByName(groupName) == null) {
            sender.sendMessage(api.getMessage("commands.group_doesnt_exists", "{group}", groupName).build());
            return true;
        }
        if (api.getGroupByName(otherGroup) == null) {
            sender.sendMessage(api.getMessage("commands.group_doesnt_exists", "{group}", otherGroup).build());
            return true;
        }

        PermissionGroup parent = api.getGroupByName(groupName);
        PermissionGroup child = api.getGroupByName(otherGroup);

        if (groupName.equals(otherGroup)) {
            sender.sendMessage(api.getMessage("commands.cannot_inherit_self", "{group}", groupName, "{inheritGroup}", otherGroup).build());
            return true;
        }

        if (child.doesInheritFrom(parent)) {
            sender.sendMessage(api.getMessage("commands.circular_inheritance", "{group}", groupName, "{inheritGroup}", otherGroup).build());
            return true;
        }

        sender.sendMessage(api.getMessage("commands.%s.success".formatted(getSubcommand()), "{group}", groupName, "{inheritGroup}", otherGroup).build());
        parent.addInheritanceUnsynced(otherGroup);
        parent.syncRawInheritanceGroups(api);
        api.refreshPlayerPermissions(parent);


        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == getArgumentLocation() + 1) {
            return predictSuggestionStartingWith(args[getArgumentLocation()], getGroupSuggestions());
        }
        if (args.length == getArgumentLocation() + 2) {
            PermissionGroup pg = api.getGroupByName(args[getArgumentLocation()]);
            Collection<String> groups = getGroupSuggestions();
            if (pg != null)
                groups.remove(pg.getGroupName());
            return predictSuggestionStartingWith(args[getArgumentLocation() + 1], groups);
        }

        return tabCompleteChildren(sender, args);
    }
}
