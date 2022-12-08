/*
 * workspace.game-traitorinwolfed.main
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

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.NameTagVisibility;
import de.timesnake.basic.bukkit.util.user.scoreboard.Sideboard;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistableGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistablePlayer;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.server.TablistManager;
import de.timesnake.basic.loungebridge.util.tool.TimerTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.basic.loungebridge.util.user.TablistTeam;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.traitor_inwolfed.main.Plugin;
import de.timesnake.game.traitor_inwolfed.user.DeadManager;
import de.timesnake.game.traitor_inwolfed.user.TraitorInwolfedUser;
import de.timesnake.game.traitor_inwolfed.user.UserManager;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.extension.util.chat.Chat;
import net.kyori.adventure.text.Component;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class TraitorInwolfedServerManager extends LoungeBridgeServerManager<TraitorInwolfedGame> {

    public static final float WIN_COINS = 10;

    public static TraitorInwolfedServerManager getInstance() {
        return (TraitorInwolfedServerManager) LoungeBridgeServerManager.getInstance();
    }

    private boolean gameRunning;

    private DeadManager deadManager;
    private UserManager userManager;

    private Sideboard gameSideboard;
    private Sideboard spectatorSideboard;

    private TimerTool timerTool;
    private AntiCampTeleporter antiCampTeleporter;

    public void onTraitorInwolfedEnable() {
        super.onLoungeBridgeEnable();

        this.deadManager = new DeadManager();
        this.userManager = new UserManager();

        super.getToolManager().add(this.deadManager);

        this.setTeamMateDamage(true);

        this.gameSideboard = Server.getScoreboardManager().registerSideboard("traitor_inwolfed",
                ChatColor.GOLD + "" + ChatColor.BOLD + this.getGame().getDisplayName());

        this.gameSideboard.setScore(6, TraitorInwolfedServer.SIDEBOARD_TIME_TEXT);
        // time
        this.gameSideboard.setScore(4, "§f----------------");
        // team
        this.gameSideboard.setScore(2, "§r§f----------------");
        this.gameSideboard.setScore(1, TraitorInwolfedServer.SIDEBOARD_MAP_TEXT);
        // map

        this.spectatorSideboard = Server.getScoreboardManager().registerSideboard("traitor_inwolfed",
                ChatColor.GOLD + "" + ChatColor.BOLD + this.getGame().getDisplayName());
        this.spectatorSideboard.setScore(4, TraitorInwolfedServer.SIDEBOARD_TIME_TEXT);
        // time
        this.spectatorSideboard.setScore(2, "§r§f----------------");
        this.spectatorSideboard.setScore(1, TraitorInwolfedServer.SIDEBOARD_MAP_TEXT);
        // map

        this.timerTool = new TimerTool() {
            @Override
            public void onTimerUpdate() {
                TraitorInwolfedServerManager.this.updateSideboardTime();
                if (this.getTime() % 60 == 0 && this.getTime() > 0) {
                    Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.A));
                    Server.broadcastTitle(Component.empty(),
                            Component.text(this.getTime() / 60 + " min left", ExTextColor.PUBLIC),
                            Duration.ofSeconds(2));
                }

                if (this.getTime() == 60) {
                    Server.getInGameUsers().forEach(u -> u.addPotionEffect(PotionEffectType.GLOWING, 60 * 20, 0));
                }
            }

            @Override
            public void onTimerEnd() {
                TraitorInwolfedServerManager.this.stopGame();
            }
        };
        this.getToolManager().add(this.timerTool);

        this.antiCampTeleporter = new AntiCampTeleporter();
        this.getToolManager().add(this.antiCampTeleporter);
    }

    @Override
    protected TraitorInwolfedGame loadGame(DbGame dbGame, boolean loadWorlds) {
        return new TraitorInwolfedGame((DbTmpGame) dbGame, loadWorlds);
    }

    @Override
    public GameUser loadUser(Player player) {
        return new TraitorInwolfedUser(player);
    }

    @Override
    public TablistManager loadTablistManager() {
        return new TablistManager() {
            @Override
            protected TablistTeam loadGameTeam() {
                return new TablistTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE) {
                    @Override
                    public NameTagVisibility isNameTagVisibleBy(TablistablePlayer player, TablistableGroup otherGroup) {
                        TraitorInwolfedTeam traitorTeam = TraitorInwolfedServer.getGame().getTraitorTeam();
                        if (traitorTeam.equals(((TraitorInwolfedUser) player).getTeam()) && traitorTeam.equals(otherGroup)) {
                            return NameTagVisibility.ALWAYS;
                        }

                        if (TraitorInwolfedServer.getTablistSpectatorTeam().equals(otherGroup)) {
                            return NameTagVisibility.ALWAYS;
                        }
                        return NameTagVisibility.NEVER;
                    }

                    @Override
                    public NameTagVisibility isNameTagVisible(TablistablePlayer player) {
                        return NameTagVisibility.ALWAYS;
                    }
                };
            }
        };
    }

    @Override
    public Plugin getGamePlugin() {
        return Plugin.TRAITOR_INWOLFED;
    }

    @Override
    public boolean isGameRunning() {
        return this.gameRunning;
    }

    @Override
    @Deprecated
    public void broadcastGameMessage(String s) {
        super.broadcastMessage(Plugin.TRAITOR_INWOLFED, s);
    }

    @Override
    public void broadcastGameMessage(Component message) {
        super.broadcastMessage(Plugin.TRAITOR_INWOLFED, message);
    }

    @Override
    public void onMapLoad() {
        super.onMapLoad();
        this.updateSideboardMap();
        this.updateSideboardTime();
    }

    @Override
    public void onGameStart() {
        this.gameRunning = true;

        String traitorNames = Chat.listToString(TraitorInwolfedServer.getGame().getTraitorTeam().getInGameUsers()
                .stream().map(User::getName).toList());

        BossBar traitorBossBar = Server.createBossBar("§cTraitors: " + traitorNames, BarColor.RED, BarStyle.SOLID);

        for (User user : TraitorInwolfedServer.getGame().getTraitorTeam().getInGameUsers()) {
            user.addBossBar(traitorBossBar);
        }

        for (User user : TraitorInwolfedServer.getInGameUsers()) {
            ((TraitorInwolfedUser) user).runKillDelay();
        }
    }

    public boolean checkGameEnd() {
        if (this.getGame().getTraitorTeam().getInGameUsers().size() == 0
                || (this.getGame().getInnocentTeam().getInGameUsers().size() == 0
                && this.getGame().getDetectiveTeam().getInGameUsers().size() == 0)) {
            this.stopGame();
            return true;
        }

        return false;
    }

    @Override
    public void onGameStop() {
        if (!this.isGameRunning()) return;
        this.gameRunning = false;
        this.broadcastGameMessage(Chat.getLongLineSeparator());

        Team traitorTeam = this.getGame().getTraitorTeam();
        Team innocentTeam = this.getGame().getInnocentTeam();
        Team detectiveTeam = this.getGame().getDetectiveTeam();

        if (this.timerTool.getTime() == 0 || innocentTeam.getInGameUsers().size() > 0 || detectiveTeam.getInGameUsers().size() > 0) {
            if (traitorTeam.getInGameUsers().size() == 0) {
                this.broadcastWinner(innocentTeam);
                for (User user : detectiveTeam.getUsers()) {
                    user.addCoins(WIN_COINS, true);
                }
            } else {
                this.broadcastWinner(null);
            }
        } else if (traitorTeam.getInGameUsers().size() > 0) {
            this.broadcastWinner(traitorTeam);
        } else {
            this.broadcastGameMessage(Component.text("Game ended", ExTextColor.PUBLIC));
        }

        this.broadcastGameMessage(Component.empty());

        this.broadcastGameMessage(Component.text(traitorTeam.getDisplayName() + "s ", traitorTeam.getTextColor())
                .append(Component.text(" :", ExTextColor.PUBLIC))
                .append(Chat.listToComponent(traitorTeam.getUsers().stream().map(User::getChatNameComponent).toList(),
                        ExTextColor.VALUE, ExTextColor.PUBLIC)));

        this.broadcastGameMessage(Chat.getLongLineSeparator());
    }

    private void broadcastWinner(Team team) {
        if (team != null) {
            Server.broadcastTitle(Component.text(team.getDisplayName() + "s", team.getTextColor())
                    .append(Component.text(" win", ExTextColor.PUBLIC)), Component.empty(), Duration.ofSeconds(5));
            this.broadcastGameMessage(Component.text(team.getDisplayName() + "s", team.getTextColor())
                    .append(Component.text(" wins", ExTextColor.PUBLIC)));
            for (User user : team.getUsers()) {
                user.addCoins(WIN_COINS, true);
            }
        } else {
            Server.broadcastTitle(Component.text("Game has ended", ExTextColor.PUBLIC), Component.empty(),
                    Duration.ofSeconds(5));
            this.broadcastGameMessage(Component.text(" Game has ended", ExTextColor.PUBLIC));
        }

    }

    @Override
    public void onGameUserQuit(GameUser gameUser) {
        this.checkGameEnd();
    }

    @Override
    public void onGameUserQuitBeforeStart(GameUser gameUser) {
        this.checkGameEnd();
    }

    @Override
    public boolean isRejoiningAllowed() {
        return true;
    }

    @Override
    public void onGameReset() {

    }

    @Override
    public ExLocation getSpectatorSpawn() {
        return this.getMap().getSpectatorLocation();
    }

    @Override
    public TraitorInwolfedMap getMap() {
        return (TraitorInwolfedMap) super.getMap();
    }

    public void updateSideboardMap() {
        this.gameSideboard.setScore(0, this.getMap().getDisplayName());
        this.spectatorSideboard.setScore(0, this.getMap().getDisplayName());
    }

    public void updateSideboardTime() {
        this.gameSideboard.setScore(5, Chat.getTimeString(this.timerTool.getTime()));
        this.spectatorSideboard.setScore(3, Chat.getTimeString(this.timerTool.getTime()));
    }

    @Override
    public Sideboard getSpectatorSideboard() {
        return this.spectatorSideboard;
    }

    public DeadManager getDeadManager() {
        return deadManager;
    }

    public UserManager getTraitorInwolfedUserManager() {
        return userManager;
    }

    public Sideboard getGameSideboard() {
        return gameSideboard;
    }
}
