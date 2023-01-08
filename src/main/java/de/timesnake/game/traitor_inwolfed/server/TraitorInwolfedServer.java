/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.traitor_inwolfed.server;

import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import java.util.List;
import org.bukkit.Material;

public class TraitorInwolfedServer extends LoungeBridgeServer {

    public static final String SIDEBOARD_TIME_TEXT = "§9§lTime";
    public static final String SIDEBOARD_MAP_TEXT = "§c§lMap";

    public static final ExItemStack GOLD = new ExItemStack(Material.SUNFLOWER).setDisplayName(
            "§6Gold Coin").immutable();

    public static final int SPAWNER_DELAY = 40;
    public static final int SPAWNER_RANGE = 10;

    public static final List<ExItemStack> SPAWNER_ITEMS = List.of(GOLD.cloneWithId());

    public static final ExItemStack PLAYER_TRACKER = new ExItemStack(Material.COMPASS).setSlot(7)
            .setDropable(false).immutable();
    public static final ExItemStack FOOD = new ExItemStack(Material.COOKED_BEEF, 32).setSlot(8)
            .immutable();
    public static final int KILL_DELAY = 25; // in seconds

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
