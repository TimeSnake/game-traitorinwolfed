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

package de.timesnake.game.traitor_inwolfed.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.ExItemStack;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.*;
import de.timesnake.game.traitor_inwolfed.main.GameTraitorInwolfed;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServer;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedTeam;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.basic.util.chat.ExTextColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class UserManager implements Listener, UserInventoryInteractListener {


    public UserManager() {
        Server.registerListener(this, GameTraitorInwolfed.getPlugin());
        Server.getInventoryEventManager().addInteractListener(this, TraitorInwolfedServer.PLAYER_TRACKER);
    }

    @EventHandler
    public void onUserDeath(UserDeathEvent e) {
        e.setBroadcastDeathMessage(false);
        e.getDrops().clear();
        e.setAutoRespawn(true);

        User killer = Server.getUser(e.getUser().getKiller());

        if (killer != null) {
            ((TraitorInwolfedUser) killer).runKillDelay();
        }
    }

    @EventHandler
    public void onUserRespawn(UserRespawnEvent e) {
        TraitorInwolfedUser user = ((TraitorInwolfedUser) e.getUser());
        user.joinSpectator();
        e.setRespawnLocation(TraitorInwolfedServer.getMap().getSpectatorLocation());
        TraitorInwolfedServer.checkGameEnd();
    }

    @Override
    public void onUserInventoryInteract(UserInventoryInteractEvent event) {
        TraitorInwolfedUser user = ((TraitorInwolfedUser) event.getUser());

        Optional<TraitorInwolfedUser> other = user.getLocation().getNearbyPlayers(100).stream()
                .map(p -> ((TraitorInwolfedUser) Server.getUser(p)))
                .filter(u -> u.getStatus().equals(Status.User.IN_GAME)
                             && !u.getTeam().equals(TraitorInwolfedServer.getGame().getTraitorTeam())
                             && user.getWorld().equals(u.getWorld()))
                .min((u1, u2) -> (int) (u1.getLocation().distanceSquared(user.getLocation())
                                        - u2.getLocation().distanceSquared(user.getLocation())));

        if (other.isPresent()) {
            user.setCompassTarget(other.get().getLocation());
            user.sendActionBarText(Component.text("Distance: ", ExTextColor.WARNING)
                    .append(Component.text(((int) user.getLocation().distance(other.get().getLocation())), ExTextColor.VALUE)));
        }

    }

    @EventHandler
    public void onUserPickupItem(UserAttemptPickupItemEvent e) {
        ExItemStack item = ExItemStack.getItem(e.getItem().getItemStack(), false);

        if (item == null) {
            return;
        }

        TraitorInwolfedUser user = ((TraitorInwolfedUser) e.getUser());

        if (item.equals(TraitorInwolfedServer.GOLD)) {
            Server.runTaskLaterSynchrony(() -> {
                ExItemStack costs = TraitorInwolfedServer.GOLD.cloneWithId().asQuantity(4);
                if (user.containsAtLeast(costs)) {
                    user.removeCertainItemStack(costs);
                    user.addItem(TraitorInwolfedTeam.ARROW.cloneWithId());
                }
            }, 1, GameTraitorInwolfed.getPlugin());
        }
    }

    @EventHandler
    public void onUserDamageByUser(UserDamageByUserEvent e) {
        TraitorInwolfedUser damager = ((TraitorInwolfedUser) e.getUserDamager());

        if (damager.isKillDelayRunning()) {
            e.setDamage(0);
            e.setCancelDamage(true);
        } else {
            if (e.getUserDamager().getInventory().getItemInMainHand().getType().equals(Material.IRON_SWORD)
                || e.getUserDamager().getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_SWORD)
                || e.getUserDamager().getInventory().getItemInMainHand().getType().equals(Material.BOW)) {
                e.setDamage(40);
            } else {
                e.setDamage(0);
            }
        }
    }
}
