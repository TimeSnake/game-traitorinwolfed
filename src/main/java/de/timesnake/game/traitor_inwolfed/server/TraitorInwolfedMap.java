package de.timesnake.game.traitor_inwolfed.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.loungebridge.util.game.ItemSpawner;
import de.timesnake.basic.loungebridge.util.game.ResetableMap;
import de.timesnake.basic.loungebridge.util.tool.Timeable;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.game.traitor_inwolfed.main.Plugin;
import org.bukkit.GameRule;

import java.util.List;

public class TraitorInwolfedMap extends Map implements ResetableMap, Timeable {

    public static final int DEFAULT_TIME = 5 * 60;

    private static final int SPECTATOR_LOCATION_INDEX = 0;
    private static final int SPAWN_LOCATION_INDEX = 1;

    private static final int ITEM_SPAWNERS_START_INDEX = 100;
    private static final int ITEM_SPAWNERS_END_INDEX = 200;

    private static final int TELEPORTER_START_INDEX = 200;
    private static final int TELEPORTER_END_INDEX = 300;

    private final List<ExLocation> teleporterLocations;

    private final int time;

    public TraitorInwolfedMap(DbMap map, boolean loadWorld) {
        super(map, loadWorld);

        int time = DEFAULT_TIME;

        for (String info : super.getInfo()) {
            String key = info.split("=")[0];
            String value = info.split("=")[1];

            if (key.equalsIgnoreCase("time")) {
                try {
                    time = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    Server.printWarning(Plugin.TRAITOR_INWOLFED, "Can not load time of map " + this.getName(),
                            "Map");
                }
            }
        }

        this.time = time;

        this.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        this.getWorld().setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        this.getWorld().setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        this.getWorld().allowBlockBreak(false);
        this.getWorld().allowBlockPlace(false);
        this.getWorld().allowCakeEat(true);
        this.getWorld().allowEntityExplode(false);
        this.getWorld().allowFireSpread(false);
        this.getWorld().allowFluidPlace(false);
        this.getWorld().allowFluidCollect(false);
        this.getWorld().allowEntityBlockBreak(false);
        this.getWorld().allowPlaceInBlock(false);
        this.getWorld().setExceptService(true);

        for (int index : this.getLocationsIds(ITEM_SPAWNERS_START_INDEX, ITEM_SPAWNERS_END_INDEX)) {
            System.out.println(index);
            TraitorInwolfedServer.getToolManager().add(this, new ItemSpawner(index, TraitorInwolfedServer.SPAWNER_DELAY,
                    TraitorInwolfedServer.SPAWNER_RANGE, TraitorInwolfedServer.SPAWNER_ITEMS));
        }

        this.teleporterLocations = this.getLocations(TELEPORTER_START_INDEX, TELEPORTER_END_INDEX);
        if (this.teleporterLocations.isEmpty()) {
            Server.printWarning(Plugin.TRAITOR_INWOLFED, "No teleporter locations found for map " + this.getName());
        }
    }

    public ExLocation getSpawnLocation() {
        return super.getLocation(SPAWN_LOCATION_INDEX);
    }

    public ExLocation getSpectatorLocation() {
        return super.getLocation(SPECTATOR_LOCATION_INDEX);
    }

    public ExLocation getRandomTeleport() {
        return this.teleporterLocations.get(Server.getRandom().nextInt(this.teleporterLocations.size()));
    }

    @Override
    public int getTime() {
        return time;
    }
}
