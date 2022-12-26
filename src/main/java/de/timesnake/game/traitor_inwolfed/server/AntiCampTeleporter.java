/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.game.traitor_inwolfed.server;

import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.util.tool.AntiCampTool;

public class AntiCampTeleporter extends AntiCampTool {

    public AntiCampTeleporter() {
        super(40, 4);
    }

    @Override
    public void teleport(User user) {
        ExLocation location = TraitorInwolfedServer.getMap().getRandomTeleport();
        if (location == null) {
            return;
        }
        user.teleport(location);
    }
}
