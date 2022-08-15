package me.mykindos.betterpvp.champions.champions.skills.skills.ranger.bow;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.champions.champions.skills.types.PrepareArrowSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

@Singleton
@BPvPListener
public class Volley extends PrepareArrowSkill {

    @Inject
    public Volley(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }

    @Override
    public String getName() {
        return "Volley";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Left click with a bow to Prepare",
                "Your next shot is instant, and shoots",
                "a volley of arrows in the direction you are facing",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level)
        };
    }

    @Override
    public Role getClassType() {
        return Role.RANGER;
    }


    @Override
    public SkillType getType() {
        return SkillType.BOW;
    }

    @Override
    public void processEntityShootBowEvent(EntityShootBowEvent event, Player player, int level, Arrow arrow) {
        event.setCancelled(true);

        Location location = player.getLocation();
        Vector vector;

        for (int i = 0; i < 10; i += 2) {
            Arrow n = player.launchProjectile(Arrow.class);
            n.setShooter(player);
            location.setYaw(location.getYaw() + i);
            vector = location.getDirection();
            n.setVelocity(vector.multiply(2));
            arrows.add(n);
        }
        location = player.getLocation();
        for (int i = 0; i < 10; i += 2) {
            Arrow n = player.launchProjectile(Arrow.class);
            n.setShooter(player);
            location.setYaw(location.getYaw() - i);
            vector = location.getDirection();
            n.setVelocity(vector.multiply(2));
            arrows.add(n);
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 3F, 1F);
    }

    @Override
    public void onHit(Player damager, LivingEntity target, int level) {

    }

    @Override
    public void displayTrail(Location location) {
        Particle.REDSTONE.builder().location(location).color(255, 255, 0).count(3).extra(0).receivers(60, true).spawn();
    }

    @EventHandler
    public void onHit(CustomDamageEvent event) {
        if(!(event.getDamager() instanceof Player)) return;
        if(!(event.getProjectile() instanceof Arrow arrow)) return;
        if(!arrows.contains(arrow)) return;


        event.setDamage(8);
        event.setReason(getName());

    }


    @Override
    public double getCooldown(int level) {
        return cooldown - ((level - 1) * 2);
    }

    @Override
    public void activate(Player player, int level) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 2.5F, 2.0F);
        active.add(player.getUniqueId());
    }

    @Override
    public Action[] getActions() {
        return SkillActions.LEFT_CLICK;
    }
}
