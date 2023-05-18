/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.game.traitor_inwolfed.server;

import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.game.util.game.Map;
import de.timesnake.basic.game.util.game.Team;
import de.timesnake.basic.loungebridge.util.game.TmpGame;
import de.timesnake.database.util.game.DbMap;
import de.timesnake.database.util.game.DbTeam;
import de.timesnake.database.util.game.DbTmpGame;

public class TraitorInwolfedGame extends TmpGame {

  private static final String INNOCENT_TEAM_NAME = "innocent";
  private static final String DETECTIVE_TEAM_NAME = "detective";
  private static final String TRAITOR_TEAM_NAME = "traitor";

  public TraitorInwolfedGame(DbTmpGame database, boolean loadWorlds) {
    super(database, loadWorlds);
  }

  @Override
  public Map loadMap(DbMap dbMap, boolean loadWorld) {
    return new TraitorInwolfedMap(dbMap, true);
  }

  @Override
  public Team loadTeam(DbTeam team) throws UnsupportedGroupRankException {
    return new TraitorInwolfedTeam(team);
  }

  public TraitorInwolfedTeam getInnocentTeam() {
    return this.getTeam(INNOCENT_TEAM_NAME);
  }

  public TraitorInwolfedTeam getDetectiveTeam() {
    return this.getTeam(DETECTIVE_TEAM_NAME);
  }

  public TraitorInwolfedTeam getTraitorTeam() {
    return this.getTeam(TRAITOR_TEAM_NAME);
  }

  @Override
  public TraitorInwolfedTeam getTeam(String team) {
    return (TraitorInwolfedTeam) super.getTeam(team);
  }
}
