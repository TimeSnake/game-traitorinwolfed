package de.timesnake.game.traitor_inwolfed.user;

import de.timesnake.basic.loungebridge.util.user.GameUser;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedServer;
import de.timesnake.game.traitor_inwolfed.server.TraitorInwolfedTeam;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TraitorInwolfedUser extends GameUser {

    public TraitorInwolfedUser(Player player) {
        super(player);
    }

    @Override
    public void joinGame() {
        this.clearInventory();
        this.removePotionEffects();

        this.teleport(TraitorInwolfedServer.getMap().getSpawnLocation());

        TraitorInwolfedTeam team = this.getTeam();

        team.getItems().forEach(this::setItem);
        this.setAttackSpeed(3);
        this.setAttackDamage(4);

        this.setSideboard(TraitorInwolfedServer.getGameSideboard());
        this.setSideboardScore(3, team.getChatColor() + "" + ChatColor.BOLD + team.getDisplayName());
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
    public TraitorInwolfedTeam getTeam() {
        return (TraitorInwolfedTeam) super.getTeam();
    }

    @Override
    public void broadcastKillstreak() {

    }
}
