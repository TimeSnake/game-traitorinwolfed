/*
 * game-traitor-inwolfed.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
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
