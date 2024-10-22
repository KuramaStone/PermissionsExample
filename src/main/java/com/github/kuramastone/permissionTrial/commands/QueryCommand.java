package com.github.kuramastone.permissionTrial.commands;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import com.github.kuramastone.permissionTrial.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class QueryCommand extends SubCommand {
    public QueryCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
        setPermission(PermissionsApi.queryCommandPermission);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation()) {
            sender.sendMessage("Usage: /%s".formatted(getFullCommandString()));
            return true;
        }

        if(!(sender instanceof Player player)) {
            sender.sendMessage(api.getMessage("commands.only_players").build());
            return true;
        }

        PlayerProfile profile = api.getOrCreatePlayerProfile(player.getUniqueId());

        PermissionGroup group = profile.getCurrentPermissionGroup();
        if(group == null) {
            sender.sendMessage(api.getMessage("commands.no_group").build());
            return true;
        }

        boolean isPermanent = profile.isCurrentGroupPermanent();

        if(isPermanent)  {
            sender.sendMessage(api.getMessage("commands.%s.success-permanent".formatted(getSubcommand()),
                    "{group}", group.getGroupName(),
                    "{prefix}", group.getGroupPrefix()).build());
        }
        else {
            sender.sendMessage(api.getMessage("commands.%s.success-temp".formatted(getSubcommand()),
                    "{group}", group.getGroupName(),
                    "{prefix}", group.getGroupPrefix(),
                    "{expiration}", StringUtils.millisecondsToReadable(profile.getExpirationTime() - System.currentTimeMillis()),
                    "{nextGroup}", profile.getGroupAfterExpiration() == null ? api.getDefaultGroup() : profile.getGroupAfterExpiration().getGroupName()).build());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if(args.length == getArgumentLocation() + 1) {
            return predictSuggestionStartingWith(args[getArgumentLocation()], getGroupSuggestions());
        }

        return tabCompleteChildren(sender, args);
    }
}
