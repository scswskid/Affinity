package com.gamesense.client.module.modules.movement;

import com.gamesense.api.settings.Setting;
import com.gamesense.client.module.Module;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

@Mod.EventBusSubscriber
public class ElytraFlight extends Module {
    private final Setting.Double speed = registerDouble("Speed", "Speed", 35, 1, 100);
    private final Setting.Mode mode = registerMode("Mode", "Mode", Arrays.asList(
            "Control",
            "Highway"
    ), "Control");
    private final Setting.Boolean takeoffTimer = registerBoolean("Takeoff Timer", "TakeoffTimer", true);
    private final Setting.Boolean autoTakeoff = registerBoolean("Auto Takeoff", "AutoTakeoff", true);

    private boolean wasAllowedFlying = false;
    private boolean wasFlying = false;
    private float wantedPitch = 90f;

    public ElytraFlight() {
        super("ElytraFlight", Category.Movement);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override public void onEnable() {
        if (mc.getConnection() == null  ||
            mc.world == null            ||
            mc.player == null) return;
        wasAllowedFlying = mc.player.capabilities.allowFlying;
        mc.player.capabilities.allowFlying = true;
        wasFlying = mc.player.capabilities.isFlying;
        mc.player.capabilities.isFlying = true;
    }
    @Override public void onDisable() {
        if (mc.getConnection() == null  ||
            mc.world == null            ||
            mc.player == null) return;

        mc.player.capabilities.allowFlying = wasAllowedFlying;
        mc.player.capabilities.isFlying = wasFlying;
    }

    @Override
    public void onUpdate() {
        if (mc.getConnection() == null  ||
            mc.world == null            ||
            mc.player == null) return;

        mc.player.speedInAir = (float) speed.getValue();
        CPacketPlayer packet = new CPacketPlayer(false);
        packet.pitch = wantedPitch;
        mc.getConnection().sendPacket(packet);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
                setWantedPitch();
            }
        }
    }

    private void setWantedPitch() {
        switch (mode.getValue()){
            case "Control":
                wantedPitch = 180f;
                break;
            case "Highway":
                wantedPitch = 145f;
                break;
        }
    }
}
