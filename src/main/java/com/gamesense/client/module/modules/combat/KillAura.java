package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.players.friends.Friends;
import com.gamesense.api.settings.Setting;
import com.gamesense.client.AffinityPlus;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillAura extends Module {
    Setting.Boolean players;
    Setting.Boolean hostileMobs;
    Setting.Boolean passiveMobs;
    Setting.Boolean swordOnly;
    Setting.Boolean caCheck;
    Setting.Boolean criticals;
    Setting.Double range;
    private boolean isAttacking = false;
    @EventHandler
    private final Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketUseEntity) {
            if (criticals.getValue() && ((CPacketUseEntity) event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK && mc.player.onGround && isAttacking) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f, mc.player.posZ, false));
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }
        }
    });

    public KillAura() {
        super("KillAura", Category.Combat);
    }

    public void setup() {
        players = registerBoolean("Players", "Players", true);
        passiveMobs = registerBoolean("Animals", "Animals", false);
        hostileMobs = registerBoolean("Monsters", "Monsters", false);
        range = registerDouble("Range", "Range", 5, 0, 10);
        swordOnly = registerBoolean("Sword Only", "SwordOnly", true);
        criticals = registerBoolean("Criticals", "Criticals", true);
        caCheck = registerBoolean("AC Check", "ACCheck", false);
    }

    public void onUpdate() {
        if (mc.player == null || mc.player.isDead) return;
        List<Entity> targets = mc.world.loadedEntityList.stream()
                .filter(entity -> entity != mc.player)
                .filter(entity -> mc.player.getDistance(entity) <= range.getValue())
                .filter(entity -> !entity.isDead)
                .filter(entity -> attackCheck(entity))
                .sorted(Comparator.comparing(e -> mc.player.getDistance(e)))
                .collect(Collectors.toList());

        targets.forEach(target -> {
            if (swordOnly.getValue() && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)) {
                return;
            }
            if (caCheck.getValue() && ((AutoCrystal) ModuleManager.getModuleByName("AutoCrystalGS")).isActive) {
                return;
            }
            attack(target);
        });
    }

    public void onEnable() {
        AffinityPlus.EVENT_BUS.subscribe(this);
    }

    public void onDisable() {
        AffinityPlus.EVENT_BUS.unsubscribe(this);
    }

    public void attack(Entity e) {
        if (mc.player.getCooledAttackStrength(0) >= 1) {
            isAttacking = true;
            mc.playerController.attackEntity(mc.player, e);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            isAttacking = false;
        }
    }

    private boolean attackCheck(Entity entity) {

        if (players.getValue() && entity instanceof EntityPlayer && !Friends.isFriend(entity.getName())) {
            if (((EntityPlayer) entity).getHealth() > 0) {
                return true;
            }
        }

        if (passiveMobs.getValue() && entity instanceof EntityAnimal) {
            return !(entity instanceof EntityTameable);
        }

        return hostileMobs.getValue() && entity instanceof EntityMob;
    }
}