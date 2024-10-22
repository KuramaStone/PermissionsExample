package com.github.kuramastone.permissionTrial.utils;

import com.github.kuramastone.permissionTrial.PermissionTrial;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/*
Found at https://gist.github.com/KingAlterIV/168eb0670151476c144b2cd70373e71e and modified to my needs and version.
 */
public class Nametag {

    private final Player player;
    private WrapperPlayServerTeams.ScoreBoardTeamInfo teamInfo;

    public Nametag(Player player) {
        this.player = player;
        teamInfo = new WrapperPlayServerTeams
                .ScoreBoardTeamInfo(Component.empty(), Component.empty(), Component.empty(),
                WrapperPlayServerTeams.NameTagVisibility.ALWAYS, WrapperPlayServerTeams.CollisionRule.ALWAYS,
                NamedTextColor.WHITE, WrapperPlayServerTeams.OptionData.NONE);


    }

    public Nametag setPrefix(Component prefix) {
        this.teamInfo.setPrefix(prefix);
        return this;
    }

    public Nametag setSuffix(Component suffix) {
        this.teamInfo.setSuffix(suffix);
        return this;
    }

    public void broadcast() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendTo(p);
        }
    }

    public void sendTo(Player viewer) {
        try {
            String teamName = player.getEntityId() + "." + UUID.randomUUID().toString().replace("-", "");
            List<String> team = Collections.singletonList(player.getName());
            WrapperPlayServerTeams packet = new WrapperPlayServerTeams(teamName, WrapperPlayServerTeams.TeamMode.CREATE, teamInfo, team);
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
        }
        catch (Exception e) {
            PermissionTrial.logger.severe("Cannot send nametag packet to viewer...");
            e.printStackTrace();
        }
    }
}