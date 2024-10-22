package com.github.kuramastone.permissionTrial.commands;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InfoCommand extends SubCommand {
    public InfoCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
        setPermission(PermissionsApi.infoCommandPermission);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation() + 1) {
            sender.sendMessage("Usage: /%s [group]".formatted(getFullCommandString()));
            return true;
        }

        String groupName = args[getArgumentLocation()];

        if (api.getGroupByName(groupName) == null) {
            sender.sendMessage(api.getMessage("commands.group_doesnt_exists", "{group}", groupName).build());
            return true;
        }

        PermissionGroup group = api.getGroupByName(groupName);

        StringBuilder permListBuilder = new StringBuilder();
        for (String perm : group.getGroupPermissions()) {
            permListBuilder.append("- ").append(perm).append("\n");
        }
        StringBuilder childListBuilder = new StringBuilder();
        for (PermissionGroup child : group.getInheritsFromTheseGroups()) {
            childListBuilder.append("- ").append(child.getGroupName()).append("\n");
        }
        String permList = "";
        String childList = "";
        if (!permListBuilder.isEmpty()) {
            permList = permListBuilder.substring(0, permListBuilder.length() - 1);
        }
        if (!childListBuilder.isEmpty()) {
            childList = childListBuilder.substring(0, childListBuilder.length() - 1);
        }

        sender.sendMessage(api.getMessage("commands.%s.success".formatted(getSubcommand()),
                "{group}", groupName,
                "{prefix}", group.getGroupPrefix() == null ? "NULL" : group.getGroupPrefix(),
                "{permissions}", permList,
                "{children}", childList).build());


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
