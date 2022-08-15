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
