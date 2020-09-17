package com.gamesense.client.module.modules.misc;

import com.gamesense.client.module.Module;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

@Mod.EventBusSubscriber
public class FakePlayer extends Module {
    public FakePlayer() {
        super("FakePlayer", Category.Misc);
        MinecraftForge.EVENT_BUS.register(this);
    }

    EntityOtherPlayerMP fakePlayer;

    @Override
    protected void onEnable() {
        fakePlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("e4ea5edb-e317-433f-ac90-ef304841d8c8"), "skidmaster"));
        mc.world.addEntityToWorld(fakePlayer.entityId, fakePlayer);
        fakePlayer.attemptTeleport(mc.player.posX, mc.player.posY, mc.player.posZ);
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        if (event.getMessage().equalsIgnoreCase("fakeplayer jump")) {
            event.setCanceled(true);
            fakePlayer.jump();
        }
    }

    @Override
    protected void onDisable() {
        try {
            mc.world.removeEntity(fakePlayer);
        } catch (Exception ignored) {}
    }
}
