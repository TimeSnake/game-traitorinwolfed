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

package de.timesnake.game.traitor_inwolfed.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.AsyncUserMoveEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.bukkit.util.world.ExLocation;
import de.timesnake.basic.bukkit.util.world.entity.PacketPlayer;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.loungebridge.util.tool.GameTool;
import de.timesnake.basic.loungebridge.util.tool.ResetableTool;
import de.timesnake.game.traitor_inwolfed.main.GameTraitorInwolfed;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServer;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedTeam;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.Tuple;
import de.timesnake.library.basic.util.chat.ExTextColor;
import de.timesnake.library.entities.entity.bukkit.ExPlayer;
import de.timesnake.library.reflection.wrapper.ExEntityPose;
import de.timesnake.library.reflection.wrapper.ExEnumItemSlot;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
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

        if (user.getTeam().equals(TraitorInwolfedServer.getGame().getTraitorTeam())) {
            return;
        }

        for (DeadBody deadBody : this.deadBodies) {
            if (deadBody.getLocation().getWorld().equals(location.getWorld()) && deadBody.getLocation().distanceSquared(location) <= RADIUS) {
                deadBody.found(user, user.getTeam().equals(TraitorInwolfedServer.getGame().getDetectiveTeam()));
            }
        }
    }

    public static class DeadBody {

        private final String name;
        private final Tuple<String, String> textures;
        private final ExLocation location;
        private Team team;
        private PacketPlayer bodyEntity;
        private boolean found = false;
        private boolean identified = false;

        public DeadBody(User user, Team team, ExLocation location) {
            this.name = user.getName();
            this.textures = user.asExPlayer().getTextureValueSiganture();
            this.team = team;
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public Team getTeam() {
            return team;
        }

        public Location getLocation() {
            return location;
        }

        public PacketPlayer getBodyEntity() {
            return bodyEntity;
        }

        public void found(TraitorInwolfedUser user, boolean identify) {
            if (this.getTeam().equals(TraitorInwolfedServer.getGame().getDetectiveTeam())) {
                if (user.changeToDetective()) {
                    this.team = TraitorInwolfedServer.getGame().getInnocentTeam();
                    return;
                }
            }

            if (this.identified) {
                user.showTitle(Component.empty(),
                        Component.text(this.name, ExTextColor.VALUE)
                                .append(Component.text(" was a ", ExTextColor.WARNING))
                                .append(Component.text(this.getTeam().getDisplayName(), this.team.getTextColor())),
                        Duration.ofSeconds(5));
                return;
            }

            if (identify) {
                this.identified = true;
                if (this.found) {
                    Server.broadcastTitle(Component.empty(),
                            Component.text(this.name, ExTextColor.VALUE)
                                    .append(Component.text(" was a ", ExTextColor.WARNING))
                                    .append(Component.text(this.getTeam().getDisplayName(), this.team.getTextColor())),
                            Duration.ofSeconds(5));
                    TraitorInwolfedServer.broadcastGameMessage(Component.text(this.name, ExTextColor.VALUE)
                            .append(Component.text(" was a ", ExTextColor.WARNING))
                            .append(Component.text(this.getTeam().getDisplayName(), this.team.getTextColor())));
                } else {
                    Server.broadcastTitle(Component.empty(),
                            Component.text(this.getTeam().getDisplayName(), this.team.getTextColor())
                                    .append(Component.text(" " + this.name, ExTextColor.WARNING))
                                    .append(Component.text(" was found dead", ExTextColor.WARNING)),
                            Duration.ofSeconds(5));
                    TraitorInwolfedServer.broadcastGameMessage(Component.text(this.getTeam().getDisplayName(), this.getTeam().getTextColor())
                            .append(Component.text(this.name, ExTextColor.VALUE))
                            .append(Component.text(" was found dead", ExTextColor.WARNING)));
                }
                return;
            }

            if (this.found) {
                return;
            }
            this.found = true;
            Server.broadcastTitle(Component.empty(), Component.text(this.name, ExTextColor.VALUE)
                            .append(Component.text(" was found dead", ExTextColor.WARNING)),
                    Duration.ofSeconds(5));
            TraitorInwolfedServer.broadcastGameMessage(Component.text(this.name, ExTextColor.VALUE)
                    .append(Component.text(" was found dead", ExTextColor.WARNING)));
        }

        public void spawn() {
            ExPlayer deadBody = new ExPlayer(this.location.getExWorld().getBukkitWorld(), this.name);

            deadBody.setTextures(this.textures.getA(), this.textures.getB());
            deadBody.setPositionRotation(this.location.getX(), this.location.getY() + 0.2, this.location.getZ(), 120, 0);
            deadBody.setNoGravity(true);
            deadBody.setCustomName(this.name);
            deadBody.setCustomNameVisible(true);

            deadBody.setPose(ExEntityPose.SLEEPING);

            if (this.team.equals(TraitorInwolfedServer.getGame().getDetectiveTeam())) {
                deadBody.setSlot(ExEnumItemSlot.HEAD, TraitorInwolfedTeam.DETECTIVE_HELMET);
            }

            this.bodyEntity = new PacketPlayer(deadBody, location);

            Server.getEntityManager().registerEntity(this.bodyEntity);
        }

        public void despawn() {
            Server.getEntityManager().unregisterEntity(this.bodyEntity);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeadBody deadBody = (DeadBody) o;
            return Objects.equals(name, deadBody.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
