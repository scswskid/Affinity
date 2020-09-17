package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.network.play.client.CPacketPlayer;

import java.util.Objects;

public class FootXp extends Module {
    public FootXp() {
        super("FootXp", Category.Combat);
    }

    @EventHandler
    final Listener<PacketEvent.Send> packetListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketPlayer && FootXp.mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle) {
        final CPacketPlayer packet = (CPacketPlayer) event.getPacket();
        event.cancel();
        packet.pitch = 90.0f;
        packet.moving = false;
        packet.onGround = true;
        Objects.requireNonNull(mc.getConnection()).sendPacket(packet);
    }});
}