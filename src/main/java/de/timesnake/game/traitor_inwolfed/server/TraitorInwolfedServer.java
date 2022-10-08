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

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import org.bukkit.Material;

import java.util.List;

public class TraitorInwolfedServer extends LoungeBridgeServer {

    public static final String SIDEBOARD_TIME_TEXT = "§9§lTime";
    public static final String SIDEBOARD_MAP_TEXT = "§c§lMap";

    public static final ExItemStack GOLD = new ExItemStack(Material.SUNFLOWER).setDisplayName("§6Gold Coin").immutable();

    public static final int SPAWNER_DELAY = 30;
    public static final int SPAWNER_RANGE = 10;

    public static final List<ExItemStack> SPAWNER_ITEMS = List.of(GOLD.cloneWithId());

    public static final ExItemStack PLAYER_TRACKER = new ExItemStack(Material.COMPASS).setSlot(7).setDropable(false).immutable();
    public static final ExItemStack FOOD = new ExItemStack(Material.COOKED_BEEF, 32).setSlot(8).immutable();


    public static TraitorInwolfedGame getGame() {
        return server.getGame();
    }

    public static TraitorInwolfedMap getMap() {
        return server.getMap();
    }

    public static boolean checkGameEnd() {
        return server.checkGameEnd();
    }

    public static Sideboard getGameSideboard() {
        return server.getGameSideboard();
    }

    private static final TraitorInwolfedServerManager server = TraitorInwolfedServerManager.getInstance();
}
