package com.gamesense.client.module.modules.movement;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.network.play.server.SPacketEntityVelocity;

public class Velocity extends Module {
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

        packet.motionX = 0;
        packet.motionY = 0;
        packet.motionZ = 0;
    });
}
