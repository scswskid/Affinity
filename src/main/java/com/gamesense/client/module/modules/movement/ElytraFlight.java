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
    private final Setting.Double speed = registerDouble("Speed", "Speed", 50, 1, 150);
    private final Setting.Mode mode = registerMode("Mode", "Mode", Arrays.asList(
            "Control",
            "Highway",
            "Superman"
    ), "Control");
    private final Setting.Boolean takeoffTimer = registerBoolean("Takeoff Timer", "TakeoffTimer", true);
    private final Setting.Boolean autoTakeoff = registerBoolean("Auto Takeoff", "AutoTakeoff", true);
    private final Setting.Mode yawMode = registerMode("Yaw Mode", "YawMode", Arrays.asList(
            "East",
            "North",
            "West",
            "South",
            "PlayerYaw"
    ), "PlayerYaw");

    private boolean wasAllowedFlying = false;
    private boolean wasFlying = false;
    private float wantedPitch = 90f;

    public ElytraFlight() {
        super("ElytraFlight", Category.Movement);
    }

    @Override public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
        if (mc.getConnection() == null  ||
            mc.world == null            ||
            mc.player == null) return;
        wasAllowedFlying = mc.player.capabilities.allowFlying;
        mc.player.capabilities.allowFlying = true;
        wasFlying = mc.player.capabilities.isFlying;
        mc.player.capabilities.isFlying = true;
    }
    @Override public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
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

        if (mc.player.onGround) {

        } else {
            CPacketPlayer packet = new CPacketPlayer(false);
            setWantedPitch(Keyboard.isKeyDown(Keyboard.KEY_SPACE));
            packet.pitch = wantedPitch;
            packet.yaw = getWantedYaw();
            packet.moving = true;
            packet.x = speed.getValue();
            packet.z = speed.getValue();
            mc.getConnection().sendPacket(packet);
        }
    }

    private void setWantedPitch(boolean isSpacePressed) {
        if (isSpacePressed) {
            switch (mode.getValue()) {
                case "Control":
                    wantedPitch = 180f;
                    break;
                case "Highway":
                    wantedPitch = 145f;
                    break;
                case "Superman":
                    wantedPitch = mc.player.rotationPitch + 40f;
                    break;
            }
        } else switch (mode.getValue()) {
            case "Control":
                wantedPitch = 90f;
                break;
            case "Highway":
                wantedPitch = 80f;
                break;
            case "Superman":
                wantedPitch = mc.player.rotationPitch;
                break;
        }
    }
    private float getWantedYaw() {
        switch (yawMode.getValue()) {
            //TODO fix values here
            case "East":
                return 0;
            case "North":
                return 0;
            case "West":
                return 0;
            case "South":
                return 0;
            default:
                return mc.player.rotationYaw;
        }
    }
}
