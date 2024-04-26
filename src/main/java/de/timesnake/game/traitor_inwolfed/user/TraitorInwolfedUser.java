/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.traitor_inwolfed.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.scoreboard.NameTagVisibility;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroup;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistGroupType;
import de.timesnake.basic.bukkit.util.user.scoreboard.TablistPlayer;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.loungebridge.util.server.LoungeBridgeServer;
import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.game.traitor_inwolfed.main.GameTraitorInwolfed;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServer;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServerManager;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedTeam;
import de.timesnake.library.basic.util.Status;
import net.kyori.adventure.text.Component;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class TraitorInwolfedUser extends GameUser {

  private BukkitTask killDelayTask;
  private int killDelay = TraitorInwolfedServer.KILL_DELAY_SEC;
  private final BossBar killDelayBossBar = Server.createBossBar("Kill Delay: §7" + this.killDelay,
      BarColor.WHITE, BarStyle.SOLID);

  public TraitorInwolfedUser(Player player) {
    super(player);
  }

  @Override
  public void onGameJoin() {
    super.onGameJoin();

    this.clearInventory();
    this.removePotionEffects();

    this.teleport(TraitorInwolfedServer.getMap().getSpawnLocation());

    TraitorInwolfedTeam team = this.getTeam();

    team.getItems().forEach(this::setItem);
    this.setPvpMode(true);

    TraitorInwolfedServer.getGameSideboard().updateScore4User(this,
        TraitorInwolfedServerManager.TEAM_LINE, team.getTDColor() + "§l" + team.getDisplayName());
  }

  public boolean changeToDetective() {
    if (!this.getTeam().equals(TraitorInwolfedServer.getGame().getInnocentTeam())) {
      return false;
    }

    TraitorInwolfedTeam detectiveTeam = TraitorInwolfedServer.getGame().getDetectiveTeam();

    this.setTeam(detectiveTeam, true);
    detectiveTeam.getItems().forEach(this::setItem);

    return true;
  }

  @Override
  public void onGameStart() {
    super.onGameStart();
    this.runKillDelay();
  }

  @Override
  public TraitorInwolfedTeam getTeam() {
    return (TraitorInwolfedTeam) super.getTeam();
  }

  @Override
  public void broadcastKillStreak() {

  }

  @Override
  public void addKill() {
    super.addKill();
    this.runKillDelay();
  }

  @Override
  public @Nullable ExLocation getRespawnLocation() {
    this.joinSpectator();
    TraitorInwolfedServer.checkGameEnd();
    return TraitorInwolfedServer.getMap().getSpectatorLocation();
  }

  public boolean isKillDelayRunning() {
    return this.killDelay > 0;
  }

  public void runKillDelay() {
    this.killDelay = TraitorInwolfedServer.KILL_DELAY_SEC;

    this.addBossBar(this.killDelayBossBar);

    this.killDelayTask = Server.runTaskTimerSynchrony(() -> {
      this.killDelayBossBar.setTitle("Kill Delay: §7" + this.killDelay);
      this.killDelayBossBar.setProgress(
          ((double) TraitorInwolfedServer.KILL_DELAY_SEC - this.killDelay)
              / TraitorInwolfedServer.KILL_DELAY_SEC);

      if (this.killDelay <= 0) {
        this.killDelayTask.cancel();
        this.removeBossBar(this.killDelayBossBar);
        this.showTitle(Component.empty(), Component.text("§cRecharged"),
            Duration.ofSeconds(1));
        this.setWalkSpeed(0.2f);
        return;
      }

      this.killDelay--;
    }, 0, 20, GameTraitorInwolfed.getPlugin());
  }

  @Override
  public TablistGroup getTablistGroup(TablistGroupType type) {
    if (de.timesnake.basic.game.util.game.TablistGroupType.GAME_TEAM.equals(type)) {
      return LoungeBridgeServer.getTablistGameTeam();
    }
    return super.getTablistGroup(type);
  }

  @Override
  public NameTagVisibility canSeeNameTagOf(TablistPlayer otherPlayer) {
    if (this.getTeam() == null) {
      return super.canSeeNameTagOf(otherPlayer);
    }

    if (this.getTeam().equals(TraitorInwolfedServer.getGame().getTraitorTeam())
        || this.hasStatus(Status.User.OUT_GAME, Status.User.SPECTATOR)) {
      return NameTagVisibility.ALWAYS;
    }
    return NameTagVisibility.NEVER;
  }
}
