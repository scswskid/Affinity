package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PlayerJumpEvent;
import com.gamesense.api.settings.Setting;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.client.CPacketPlayer;

public class LongJump extends Module {
    private Setting.Integer xzMultiplier = registerInteger("Speed Multiplier", "SpeedMultiplier", 5, 1, 20);
    private Setting.Double fallSpeed = registerDouble("Fall Speed", "FallSpeed", 1.0, 0.1, 4.0);

    public LongJump() {
        super("LongJump", Category.Movement);
    }

    @EventHandler
    public Listener<PlayerJumpEvent> jumpEventListener = new Listener<>(e -> {
        if (mc.world == null || mc.getConnection() == null || mc.player == null) return;
        sendMovePacket();
    });

    private void sendMovePacket() {
        if (mc.getConnection() == null) return;

        CPacketPlayer packet = new CPacketPlayer(false);

        packet.x = mc.player.motionX * xzMultiplier.getValue();
        packet.y = fallSpeed.getValue();
        packet.z = mc.player.motionZ * xzMultiplier.getValue();

        mc.getConnection().sendPacket(packet);
    }
}
