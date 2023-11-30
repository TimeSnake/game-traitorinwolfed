/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.traitor_inwolfed.server;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.chat.ChatColor;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.scoreboard.*;
import de.timesnake.basic.bukkit.util.user.scoreboard.ExSideboard.LineId;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.server.EndMessage;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServerManager;
import de.timesnake.basic.loungebridge.util.server.TablistManager;
import de.timesnake.basic.loungebridge.util.tool.advanced.MapTimerTool;
import de.timesnake.basic.loungebridge.util.tool.advanced.TimerTool;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.basic.loungebridge.util.user.TablistTeam;
import de.timesnake.database.util.game.DbGame;
import de.timesnake.database.util.game.DbTmpGame;
import de.timesnake.game.traitor_inwolfed.main.Plugin;
import de.timesnake.game.traitor_inwolfed.user.DeadManager;
import de.timesnake.game.traitor_inwolfed.user.TraitorInwolfedUser;
import de.timesnake.game.traitor_inwolfed.user.UserManager;
import de.timesnake.library.chat.ExTextColor;
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

  public static final LineId<String> TEAM_LINE = LineId.of("team", "", true, Object::toString);

  public static TraitorInwolfedServerManager getInstance() {
    return (TraitorInwolfedServerManager) LoungeBridgeServerManager.getInstance();
  }

  private DeadManager deadManager;
  private UserManager userManager;

  private ExSideboard gameSideboard;
  private ExSideboard spectatorSideboard;

  private TimerTool timerTool;
  private AntiCampTeleporter antiCampTeleporter;

  public void onTraitorInwolfedEnable() {
    super.onLoungeBridgeEnable();

    this.deadManager = new DeadManager();
    this.userManager = new UserManager();

    super.getToolManager().add(this.deadManager);

    this.setTeamMateDamage(true);

    this.gameSideboard = Server.getScoreboardManager()
        .registerExSideboard(new ExSideboardBuilder()
            .name("ti")
            .title("§6§l" + this.getGame().getDisplayName())
            .lineSpacer()
            .addLine(LineId.TIME)
            .addLine(TEAM_LINE)
            .addLine(LineId.MAP));

    this.spectatorSideboard = Server.getScoreboardManager()
        .registerExSideboard(new ExSideboardBuilder()
            .name("ti_spec")
            .title("§6§l" + this.getGame().getDisplayName())
            .lineSpacer()
            .addLine(LineId.TIME)
            .addLine(LineId.MAP));

    this.timerTool = new MapTimerTool() {
      @Override
      public void onTimerUpdate() {
        updateSideboardTime();
        if (this.getTime() % 60 == 0 && this.getTime() > 0) {
          Server.broadcastNote(Instrument.PLING, Note.natural(1, Note.Tone.A));
          Server.broadcastTitle(Component.empty(),
              Component.text(this.getTime() / 60 + " min left", ExTextColor.PUBLIC),
              Duration.ofSeconds(2));
        }

        if (this.getTime() == 60) {
          Server.getInGameUsers()
              .forEach(u -> u.addPotionEffect(PotionEffectType.GLOWING, 60 * 20, 0));
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
  public TablistManager initTablistManager() {
    return new TablistManager() {
      @Override
      protected TablistTeam loadGameTeam() {
        return new TablistTeam("0", "game", "", ChatColor.WHITE, ChatColor.WHITE) {
          @Override
          public NameTagVisibility isNameTagVisibleBy(TablistablePlayer player,
                                                      TablistableGroup otherGroup) {
            TraitorInwolfedTeam traitorTeam = TraitorInwolfedServer.getGame()
                .getTraitorTeam();
            if (traitorTeam.equals(((TraitorInwolfedUser) player).getTeam())
                && traitorTeam.equals(otherGroup)) {
              return NameTagVisibility.ALWAYS;
            }

            if (TraitorInwolfedServer.getTablistSpectatorTeam().equals(otherGroup)) {
              return NameTagVisibility.ALWAYS;
            }
            return NameTagVisibility.NEVER;
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
  public void onMapLoad() {
    super.onMapLoad();
    this.updateSideboardMap();
    this.updateSideboardTime();
  }

  @Override
  public void onGameStart() {
    BossBar traitorBossBar = Server.createBossBar("§cTraitors: " + String.join(", ",
            TraitorInwolfedServer.getGame().getTraitorTeam().getInGameUsers().stream().map(User::getName).toList()),
        BarColor.RED, BarStyle.SOLID);

    for (User user : TraitorInwolfedServer.getGame().getTraitorTeam().getInGameUsers()) {
      user.addBossBar(traitorBossBar);
    }
  }

  @Override
  public boolean checkGameEnd() {
    if (this.getGame().getTraitorTeam().getInGameUsers().isEmpty()
        || (this.getGame().getInnocentTeam().getInGameUsers().isEmpty()
        && this.getGame().getDetectiveTeam().getInGameUsers().isEmpty())) {
      this.stopGame();
      return true;
    }

    return false;
  }

  @Override
  public void onGameStop() {
    Team traitorTeam = this.getGame().getTraitorTeam();
    Team innocentTeam = this.getGame().getInnocentTeam();
    Team detectiveTeam = this.getGame().getDetectiveTeam();

    EndMessage endMessage = new EndMessage();

    if (this.timerTool.getTime() == 0 || !innocentTeam.getInGameUsers().isEmpty() || !detectiveTeam.getInGameUsers().isEmpty()) {
      if (traitorTeam.getInGameUsers().isEmpty()) {
        endMessage.winner(innocentTeam);
        for (User user : detectiveTeam.getUsers()) {
          user.addCoins(TraitorInwolfedServer.WIN_COINS, true);
        }
      }
    } else if (!traitorTeam.getInGameUsers().isEmpty()) {
      endMessage.winner(traitorTeam);
    }

    endMessage.addExtra(traitorTeam.getTDColor() + traitorTeam.getDisplayName() + "s§p: "
        + String.join(", ", traitorTeam.getUsers().stream().map(User::getTDChatName).toList()));

    endMessage.send();
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
    this.gameSideboard.updateScore(LineId.MAP, this.getMap().getDisplayName());
    this.spectatorSideboard.updateScore(LineId.MAP, this.getMap().getDisplayName());
  }

  public void updateSideboardTime() {
    this.gameSideboard.updateScore(LineId.TIME, this.timerTool.getTime());
    this.spectatorSideboard.updateScore(LineId.TIME, this.timerTool.getTime());
  }

  @Override
  public ExSideboard getSpectatorSideboard() {
    return this.spectatorSideboard;
  }

  public DeadManager getDeadManager() {
    return deadManager;
  }

  @Override
  public ExSideboard getGameSideboard() {
    return gameSideboard;
  }
}
