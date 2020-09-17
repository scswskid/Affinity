package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.settings.Setting;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class Velocity extends Module {
    Setting.Double xMultiplier = registerDouble("X Multiplier", "xMultiplier", 0.0, 0.0, 1.0);
    Setting.Double yMultiplier = registerDouble("Y Multiplier", "yMultiplier", 0.0, 0.0, 1.0);
    Setting.Double zMultiplier = registerDouble("Z Multiplier", "zMultiplier", 0.0, 0.0, 1.0);

    public Velocity() {
        super("Velocity", Category.Movement);
    }

    @EventHandler
    Listener<PacketEvent.Receive> listener = new Listener<>(event -> {
        if (!ModuleManager.isModuleEnabled(this))
            return;

        if (!(event.getPacket() instanceof SPacketEntityVelocity))
            return;

        SPacketEntityVelocity packet = (SPacketEntityVelocity) event.getPacket();
        if (packet.entityID != mc.player.entityId) return;

        packet.motionX *= xMultiplier.getValue();
        packet.motionY *= yMultiplier.getValue();
        packet.motionZ *= zMultiplier.getValue();
    });
}
