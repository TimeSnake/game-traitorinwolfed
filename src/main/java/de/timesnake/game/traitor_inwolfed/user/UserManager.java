/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.traitor_inwolfed.user;

import de.timesnake.basic.bukkit.util.Server;
import de.timesnake.basic.bukkit.util.user.User;
import de.timesnake.basic.bukkit.util.user.event.UserAttemptPickupItemEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDamageByUserEvent;
import de.timesnake.basic.bukkit.util.user.event.UserDeathEvent;
import de.timesnake.basic.bukkit.util.user.event.UserRespawnEvent;
import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractEvent;
import de.timesnake.basic.bukkit.util.user.inventory.UserInventoryInteractListener;
import de.timesnake.game.traitor_inwolfed.main.GameTraitorInwolfed;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServer;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedTeam;
import de.timesnake.library.basic.util.Status;
import de.timesnake.library.chat.ExTextColor;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class UserManager implements Listener, UserInventoryInteractListener {


  public UserManager() {
    Server.registerListener(this, GameTraitorInwolfed.getPlugin());
    Server.getInventoryEventManager()
        .addInteractListener(this, TraitorInwolfedServer.PLAYER_TRACKER);
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
        .filter(u -> u.getStatus().equals(Status.User.IN_GAME) && !user.equals(u))
        .min((u1, u2) -> (int) (u1.getLocation().distanceSquared(user.getLocation())
            - u2.getLocation().distanceSquared(user.getLocation())));

    if (other.isPresent()) {
      user.setCompassTarget(other.get().getLocation());
      user.sendActionBarText(Component.text("Distance: ", ExTextColor.WARNING)
          .append(Component.text(
              ((int) user.getLocation().distance(other.get().getLocation())),
              ExTextColor.VALUE)));
    } else {
      user.sendActionBarText(Component.text("No players nearby", ExTextColor.WARNING));
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
  public void onPlayerPickUpArrow(PlayerPickupArrowEvent e) {
    e.getArrow().remove();
    e.setCancelled(true);
  }

  @EventHandler
  public void onUserDamageByUser(UserDamageByUserEvent e) {
    TraitorInwolfedUser damager = ((TraitorInwolfedUser) e.getUserDamager());
    TraitorInwolfedUser user = ((TraitorInwolfedUser) e.getUser());

    if (damager.isKillDelayRunning()) {
      e.setDamage(0);
      e.setCancelDamage(true);
    } else {
      Material itemType = e.getUserDamager().getInventory().getItemInMainHand().getType();
      if (itemType.equals(Material.IRON_SWORD)
          || itemType.equals(Material.GOLDEN_SWORD)
          || (itemType.equals(Material.BOW)
          && e.getDamageCause().equals(DamageCause.PROJECTILE))) {
        e.setDamage(40);
        TraitorInwolfedTeam traitorTeam = TraitorInwolfedServer.getGame().getTraitorTeam();
        if (!damager.getTeam().equals(traitorTeam) && !user.getTeam().equals(traitorTeam)) {
          Server.runTaskLaterSynchrony(damager::kill, 1, GameTraitorInwolfed.getPlugin());
        }
      } else {
        e.setDamage(0);
      }
    }
  }
}
