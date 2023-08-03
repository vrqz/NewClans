package me.mykindos.betterpvp.clans.clans.listeners;

import com.google.inject.Inject;
import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.clans.Clan;
import me.mykindos.betterpvp.clans.clans.ClanManager;
import me.mykindos.betterpvp.clans.clans.ClanRelation;
import me.mykindos.betterpvp.core.framework.delayedactions.events.PlayerDelayedTeleportEvent;
import me.mykindos.betterpvp.core.framework.events.scoreboard.ScoreboardUpdateEvent;
import me.mykindos.betterpvp.core.gamer.GamerManager;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.core.utilities.UtilServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

@BPvPListener
public class ClansMovementListener extends ClanListener {

    private final Clans clans;

    @Inject
    public ClansMovementListener(Clans clans, ClanManager clanManager, GamerManager gamerManager) {
        super(clanManager, gamerManager);
        this.clans = clans;
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {

        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {


            UtilServer.runTaskAsync(clans, () -> {
                Optional<Clan> clanToOptional = clanManager.getClanByLocation(e.getTo());
                Optional<Clan> clanFromOption = clanManager.getClanByLocation(e.getFrom());


                if (clanToOptional.isEmpty() && clanFromOption.isEmpty()) {
                    return;
                }


                if (clanFromOption.isEmpty() || clanToOptional.isEmpty()
                        || !clanFromOption.equals(clanToOptional)) {
                    displayOwner(e.getPlayer(), clanToOptional.orElse(null));

                    UtilServer.runTask(clans, () -> UtilServer.callEvent(new ScoreboardUpdateEvent(e.getPlayer())));

                }
            });

        }

    }

    public void displayOwner(Player player, Clan locationClan) {

        Component component = Component.empty().color(NamedTextColor.YELLOW).append(Component.text("Wilderness"));

        Clan clan = null;
        Optional<Clan> clanOptional = clanManager.getClanByPlayer(player);
        if (clanOptional.isPresent()) {
            clan = clanOptional.get();
        }

        String append = "";

        if (locationClan != null) {
            ClanRelation relation = clanManager.getRelation(clan, locationClan);
            component = Component.text(relation.getPrimaryAsChatColor() + locationClan.getName());

            if (locationClan.isAdmin()) {
                if (locationClan.isSafe()) {
                    component = Component.text(NamedTextColor.WHITE + locationClan.getName());
                    append = NamedTextColor.WHITE + " (" + NamedTextColor.AQUA + "Safe" + NamedTextColor.WHITE + ")";
                }
            } else if (relation == ClanRelation.ALLY_TRUST) {
                append = NamedTextColor.GRAY + " (" + NamedTextColor.YELLOW + "Trusted" + NamedTextColor.GRAY + ")";

            } else if (relation == ClanRelation.ENEMY) {
                if (clan != null) {
                    append = clan.getDominanceString(locationClan);
                }

            }
        }

        if (locationClan != null) {
            if (locationClan.getName().equals("Fields") || locationClan.getName().equals("Lake")) {
                append = NamedTextColor.RED.toString() + TextDecoration.BOLD + "                    Warning! "
                        + NamedTextColor.GRAY + TextDecoration.BOLD + "PvP Hotspot";
            }

            UtilMessage.simpleMessage(player, "Territory",
                    component.append(Component.text(append)),
                    clanManager.getClanTooltip(player, locationClan)
            );

        } else {
            UtilMessage.message(player, "Territory", component.append(Component.text(append)));
        }

    }

    @EventHandler
    public void onDelayedTeleport(PlayerDelayedTeleportEvent event) {
        if (event.isCancelled()) return;

        clanManager.getClanByLocation(event.getPlayer().getLocation()).ifPresentOrElse(clan -> {
            if (clan.isAdmin()) {
                if (clan.isSafe() && clan.getName().contains("Spawn") && event.getPlayer().getLocation().getY() > 110) {
                    return;
                }
            }

            UtilMessage.message(event.getPlayer(), "Clans", "You can only teleport to your clan home from spawn or the wilderness.");
            event.setCancelled(true);

        }, () -> {
            event.setDelayInSeconds(30);
        });

    }
}
