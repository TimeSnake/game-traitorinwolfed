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

package de.timesnake.game.traitor_inwolfed.main;

import de.timesnake.basic.bukkit.util.ServerManager;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class GameTraitorInwolfed extends JavaPlugin {

    public static GameTraitorInwolfed getPlugin() {
        return plugin;
    }

    private static GameTraitorInwolfed plugin;

    @Override
    public void onLoad() {
        ServerManager.setInstance(new TraitorInwolfedServerManager());
    }

    @Override
    public void onEnable() {
        plugin = this;

        TraitorInwolfedServerManager.getInstance().onTraitorInwolfedEnable();
    }
}
