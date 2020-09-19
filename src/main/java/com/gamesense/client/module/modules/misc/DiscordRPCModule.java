package com.gamesense.client.module.modules.misc;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.gamesense.api.settings.Setting;
import com.gamesense.client.AffinityPlus;
import com.gamesense.client.module.Module;

/*TODO keeps crashing*/

public class DiscordRPCModule extends Module {
    public final Setting.Integer updateDelay = registerInteger("Update Delay", "UpdateDelay", 30, 1, 1000);

    private boolean connected = false;
    private final DiscordRPC rpc = DiscordRPC.INSTANCE;
    private final DiscordRichPresence presence = new DiscordRichPresence();

    public DiscordRPCModule() {
        super("DiscordRPC", Category.Misc);
    }

    @Override
    protected void onEnable() {
        if (connected) return;
        connected = true;

        rpc.Discord_Initialize(AffinityPlus.DISCORDAPPID, new DiscordEventHandlers(), true, null);
        presence.startTimestamp = System.currentTimeMillis() / 1000L;

        new Thread(() -> rpcUpdate(), "DiscordRPCHandler");
    }

    @Override
    protected void onDisable() {
        rpc.Discord_Shutdown();
    }

    private void rpcUpdate() {
        while (connected) {
            try {
                presence.details = "Vibing on " + mc.getConnection() != null ? mc.currentServerData.serverIP : "the Main Menu";
                presence.state = getPresenceState() + " | Affinity+ On Top!";
                rpc.Discord_UpdatePresence(presence);

                Thread.sleep(updateDelay.getValue() * 1000L);
            } catch (Exception ignored) {}
        }
    }

    private String getPresenceState() {
        return mc.player == null ? "Player not Found." : mc.player.getName();
    }
}