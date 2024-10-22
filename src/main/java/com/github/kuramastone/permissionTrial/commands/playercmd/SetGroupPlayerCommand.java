package com.github.kuramastone.permissionTrial.commands.playercmd;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.SubCommand;
import com.github.kuramastone.permissionTrial.groups.PermissionGroup;
import com.github.kuramastone.permissionTrial.players.PlayerProfile;
import com.github.kuramastone.permissionTrial.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SetGroupPlayerCommand extends SubCommand {
    public SetGroupPlayerCommand(PermissionsApi api, SubCommand parent, int argumentLocation, String subcommand) {
        super(api, parent, argumentLocation, subcommand);
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < getArgumentLocation() + 2) {
            sender.sendMessage("Usage: /%s [player] [group]".formatted(getFullCommandString()));
            sender.sendMessage("Usage: /%s [player] [group] [expiration] [next group]".formatted(getFullCommandString()));
            return true;
        }

        String playerName;
        PermissionGroup group;
        long expirationTime;
        PermissionGroup nextGroup;

        if (args.length == getArgumentLocation() + 2) {
            playerName = args[getArgumentLocation()];
            group = api.getGroupByName(args[getArgumentLocation() + 1]);
            expirationTime = -1L;
            nextGroup = null;
        }
        else {
            if (args.length >= getArgumentLocation() + 3) {

                playerName = args[getArgumentLocation()];
                group = api.getGroupByName(args[getArgumentLocation() + 1]);
                try {
                    expirationTime = StringUtils.readableTimeToMilliseconds(args[getArgumentLocation() + 2]);
                }
                catch (Exception e) {
                    sender.sendMessage(api.getMessage("commands.unknown_time", "{value}", args[getArgumentLocation() + 2]).build());
                    return true;
                }
                nextGroup = api.getGroupByName(args[getArgumentLocation() + 3]);
            }
            else {
                sender.sendMessage("Usage: /%s [player] [group] [expiration] [next group]".formatted(getFullCommandString()));
                return true;
            }
        }


        if (group == null) {
            sender.sendMessage(api.getMessage("commands.group_doesnt_exists").build());
            return true;
        }

        String expireString = expirationTime == -1 ? "indefinite" : StringUtils.millisecondsToReadable(expirationTime);
        String nextGroupString = nextGroup == null ? "null" : nextGroup.getGroupName();

        PlayerProfile profile = api.getOrCreatePlayerProfile(Bukkit.getOfflinePlayer(playerName).getUniqueId());
        profile.setPermissionGroup(group, expirationTime == -1 ? -1 : System.currentTimeMillis() + expirationTime, nextGroup);
        if (nextGroup != null)
            sender.sendMessage(api.getMessage("commands.%s.success-full".formatted(getSubcommand()),
                    "{player}", profile.getName(), "{group}", group.getGroupName(), "{time}", expireString, "{nextGroup}", nextGroupString).build());
        else
            sender.sendMessage(api.getMessage("commands.%s.success-short".formatted(getSubcommand()),
                    "{player}", profile.getName(), "{group}", group.getGroupName(), "{time}", expireString, "{nextGroup}", nextGroupString).build());

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == getArgumentLocation() + 1) {
            return predictSuggestionStartingWith(args[getArgumentLocation()], getPlayerSuggestions(sender));
        }
        if (args.length == getArgumentLocation() + 2) {
            return predictSuggestionStartingWith(args[getArgumentLocation() + 1], getGroupSuggestions());
        }
        if (args.length == getArgumentLocation() + 3) {
            return List.of("1d", "1h", "1m", "1s", "1000");
        }
        if (args.length == getArgumentLocation() + 4) {
            return predictSuggestionStartingWith(args[getArgumentLocation() + 3], getGroupSuggestions());
        }

        return tabCompleteChildren(sender, args);
    }
}
