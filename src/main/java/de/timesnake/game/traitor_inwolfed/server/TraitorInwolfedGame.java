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

import de.timesnake.basic.bukkit.util.exception.UnsupportedGroupRankException;
import de.timesnake.basic.game.util.Map;
import de.timesnake.basic.game.util.Team;
import de.timesnake.basic.game.util.TmpGame;
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
