package com.github.kuramastone.permissionTrial.commands.groupcmd;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.SubCommand;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SetPrefixGroupCommand extends SubCommand {
    public SetPrefixGroupCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation() + 2) {
            sender.sendMessage("Usage: /%s [group] [prefix]".formatted(getFullCommandString()));
            return true;
        }

        String groupName = args[getArgumentLocation()];
        String prefix = args[getArgumentLocation()+1];

        if (api.getGroupByName(groupName) == null) {
            sender.sendMessage(api.getMessage("commands.group_doesnt_exists", "{group}", groupName).build());
            return true;
        }
        PermissionGroup group = api.getGroupByName(groupName);

        sender.sendMessage(api.getMessage("commands.%s.success".formatted(getSubcommand()), "{group}", groupName, "{prefix}", prefix).build());
        group.setGroupPrefix(prefix);
        api.updatePrefixes(group);


        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == getArgumentLocation() + 1) {
            return predictSuggestionStartingWith(args[getArgumentLocation()], getGroupSuggestions());
        }

        return tabCompleteChildren(sender, args);
    }
}
