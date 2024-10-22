package com.github.kuramastone.permissionTrial.commands;

import com.github.kuramastone.permissionTrial.PermissionsApi;
import com.github.kuramastone.permissionTrial.commands.groupcmd.GroupCommand;
import com.github.kuramastone.permissionTrial.commands.playercmd.PlayerCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ParentCommand extends SubCommand {

    public ParentCommand(PermissionsApi api) {
        super(api, null, 0, "groupperms");
        /*
        Commands to implement:

        /groupperms group create GROUP_NAME
        /groupperms group remove GROUP_NAME
        /groupperms group setprefix GROUP_NAME prefix
        /groupperms group addperm GROUP_NAME permission.uwu
        /groupperms group remperm GROUP_NAME permission.uwu
        /groupperms group addinherit GROUP_NAME permission.uwu
        /groupperms group reminherit GROUP_NAME permission.uwu
        /groupperms group setdefault GROUP_NAME

        /groupperms player setgroup [player] [group] <expiration> <reverts to this group>

        /groupperms query
        /groupperms info


         */

        // subcommands
        registerSubCommand(new GroupCommand(api, this, 1, "group"));
        registerSubCommand(new PlayerCommand(api, this, 1, "player"));
        registerSubCommand(new InfoCommand(api,  this, 1, "info"));
        registerSubCommand(new QueryCommand(api,  this, 1, "query"));
        registerSubCommand(new SaveCommand(api, this, 1, "save"));
        registerSubCommand(new DescriptionCommand(api, this, 1, "help"));


        for (SubCommand cmd : getSubCommandsList()) {
            if (cmd.getDescription() == null) {
                cmd.setDescription(api.getMessage("commands.descriptions.%s".formatted(cmd.getSubcommand())).getText());
            }
        }
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length <= getArgumentLocation()) {
            subCommands.get("help").execute(sender, args);
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












