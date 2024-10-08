/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.traitor_inwolfed.server;

import de.timesnake.basic.bukkit.util.user.inventory.ExItemStack;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.database.util.game.DbTeam;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

public class TraitorInwolfedTeam extends Team {

  public static final ExItemStack DETECTIVE_HELMET = ExItemStack.getLeatherArmor(
          Material.LEATHER_HELMET, Color.BLUE)
      .setSlot(EquipmentSlot.HEAD).unbreakable().setDropable(false).immutable();

  public static final ExItemStack ARROW = new ExItemStack(Material.ARROW).setDropable(false)
      .immutable();

  public static final List<ExItemStack> INNOCENT_ITEMS = List.of(
      new ExItemStack(Material.IRON_SWORD).addExEnchantment(Enchantment.SHARPNESS, 10)
          .unbreakable().setSlot(0).setDropable(false).immutable(),
      new ExItemStack(Material.BOW).setSlot(1).addExEnchantment(Enchantment.POWER, 10)
          .setDropable(false).immutable(),
      TraitorInwolfedServer.PLAYER_TRACKER,
      TraitorInwolfedServer.FOOD
  );
  public static final List<ExItemStack> DETECTIVE_ITEMS = List.of(
      new ExItemStack(Material.GOLDEN_SWORD).addExEnchantment(Enchantment.SHARPNESS, 10)
          .unbreakable().setSlot(0).setDropable(false).immutable(),
      new ExItemStack(Material.BOW).setSlot(1).addExEnchantment(Enchantment.POWER, 10)
          .setDropable(false).immutable(),
      ARROW.cloneWithId().asQuantity(3).setSlot(6).immutable(),
      TraitorInwolfedServer.PLAYER_TRACKER,
      TraitorInwolfedServer.FOOD,
      DETECTIVE_HELMET
  );
  public static final List<ExItemStack> TRAITOR_ITEMS = List.of(
      new ExItemStack(Material.IRON_SWORD).addExEnchantment(Enchantment.SHARPNESS, 10)
          .unbreakable().setSlot(0).setDropable(false).immutable(),
      new ExItemStack(Material.BOW).setSlot(1).addExEnchantment(Enchantment.POWER, 10)
          .setDropable(false).immutable(),
      TraitorInwolfedServer.PLAYER_TRACKER,
      TraitorInwolfedServer.FOOD

  );


  public TraitorInwolfedTeam(DbTeam team) {
    super(team);
  }

  public List<ExItemStack> getItems() {
    if (this.equals(TraitorInwolfedServer.getGame().getInnocentTeam())) {
      return INNOCENT_ITEMS;
    } else if (this.equals(TraitorInwolfedServer.getGame().getDetectiveTeam())) {
      return DETECTIVE_ITEMS;
    } else if (this.equals(TraitorInwolfedServer.getGame().getTraitorTeam())) {
      return TRAITOR_ITEMS;
    }
    return new ArrayList<>(0);
  }
}
