/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.traitor_inwolfed.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.PacketPlayer;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.advanced.DeadPlayer;
import de.timesnake.basic.loungebridge.util.tool.scheduler.ResetableTool;
import de.timesnake.game.traitor_inwolfed.main.GameTraitorInwolfed;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServer;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedTeam;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.entities.entity.PlayerBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class DeadManager implements Listener, GameTool, ResetableTool {

  private static final double RADIUS = 3;

  private final Set<DeadBody> deadBodies = new HashSet<>();

  public DeadManager() {
    Server.registerListener(this, GameTraitorInwolfed.getPlugin());
  }

  @Override
  public void reset() {
    this.deadBodies.clear();
  }

  @EventHandler
  public void onUserDeath(UserDeathEvent e) {
    TraitorInwolfedUser user = (TraitorInwolfedUser) e.getUser();
    ExLocation location = user.getExLocation();
    DeadBody deadBody = new DeadBody(user, user.getTeam(), location);
    deadBody.spawn();
    this.deadBodies.add(deadBody);
  }

  @EventHandler
  public void onUserMove(AsyncUserMoveEvent e) {
    TraitorInwolfedUser user = ((TraitorInwolfedUser) e.getUser());
    Location location = e.getTo();

    if (user.isDead() || !user.getStatus().equals(Status.User.IN_GAME) || user.getTeam() == null) {
      return;
    }

    for (DeadBody deadBody : this.deadBodies) {
      if (deadBody.getLocation().getWorld().equals(location.getWorld())
          && deadBody.getLocation().distanceSquared(location) <= RADIUS * RADIUS) {
        deadBody.found(user, user.getTeam().equals(TraitorInwolfedServer.getGame().getDetectiveTeam()));
      }
    }
  }

  public static class DeadBody extends DeadPlayer {

    private Team team;
    private boolean found = false;
    private boolean identified = false;

    public DeadBody(User user, Team team, ExLocation location) {
      super(user, location);
      this.team = team;
    }

    public Team getTeam() {
      return team;
    }

    public void found(User user, boolean identify) {
      if (!user.isSneaking() && !this.found) {
        user.sendActionBarText(Component.text("Sneak to find the body"));
        return;
      }

      if (!((TraitorInwolfedUser) user).getTeam().equals(TraitorInwolfedServer.getGame().getTraitorTeam())
          && this.getTeam().equals(TraitorInwolfedServer.getGame().getDetectiveTeam())) {
        if (((TraitorInwolfedUser) user).changeToDetective()) {
          this.team = TraitorInwolfedServer.getGame().getInnocentTeam();
          return;
        }
      }

      if (this.identified) {
        user.showTDTitle("", "§v" + this.name + "§w was a " + this.getTeam().getTDColor() +
            this.getTeam().getDisplayName(), Duration.ofSeconds(5), Duration.ZERO, Duration.ZERO);
        return;
      }

      if (identify) {
        this.identified = true;
        if (this.found) {
          Server.broadcastTDTitle("", "§v" + this.name + "§w was a " + this.getTeam().getTDColor() +
              this.getTeam().getDisplayName(), Duration.ofSeconds(5), Duration.ZERO, Duration.ZERO);
          TraitorInwolfedServer.broadcastGameTDMessage("§v" + this.name + "§w was a " + this.getTeam().getTDColor() +
              this.getTeam().getDisplayName());
        } else {
          Server.broadcastTDTitle("", this.getTeam().getTDColor() + this.getTeam().getDisplayName()
              + " §v" + this.name + "§w was found dead", Duration.ofSeconds(5), Duration.ZERO, Duration.ZERO);
          TraitorInwolfedServer.broadcastGameTDMessage(this.getTeam().getTDColor() + this.getTeam().getDisplayName()
              + " §v" + this.name + "§w was found dead");
        }
        return;
      }

      if (this.found) {
        return;
      }
      this.found = true;

      Server.broadcastTDTitle("", "§v" + this.name + "§w was found dead", Duration.ofSeconds(5));
      TraitorInwolfedServer.broadcastGameTDMessage("§v" + this.name + "§w was found dead");
    }

    public void spawn() {
      ServerPlayer deadBody = PlayerBuilder.ofName(this.name, this.textures.getA(), this.textures.getB())
          .applyOnEntity(e -> {
            e.setLevel(this.location.getExWorld().getHandle());
            e.setPos(this.location.getX(), this.location.getY() + 0.2, this.location.getZ());
            e.setRot(120, 0);
            e.setNoGravity(true);
            e.setCustomName(net.minecraft.network.chat.Component.literal(this.name + " (dead)"));
            e.setCustomNameVisible(true);
            e.setPose(Pose.SLEEPING);

            if (this.team.equals(TraitorInwolfedServer.getGame().getDetectiveTeam())) {
              e.setItemSlot(EquipmentSlot.HEAD, TraitorInwolfedTeam.DETECTIVE_HELMET.getHandle());
            }
          })
          .build();

      this.bodyEntity = new PacketPlayer(deadBody, location);

      Server.getEntityManager().registerEntity(this.bodyEntity);
    }

    @Override
    protected void onEntityBuild(PlayerBuilder<?, ?> builder) {
      builder.applyOnEntity(e -> {
        if (this.team.equals(TraitorInwolfedServer.getGame().getDetectiveTeam())) {
          e.setItemSlot(EquipmentSlot.HEAD, TraitorInwolfedTeam.DETECTIVE_HELMET.getHandle());
        }
      });
    }
  }
}
