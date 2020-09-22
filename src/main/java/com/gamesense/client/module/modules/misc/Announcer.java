package com.gamesense.client.module.modules.misc;

import com.gamesense.api.event.events.DestroyBlockEvent;
import com.gamesense.api.event.events.PacketEvent;
import com.gamesense.api.event.events.PlayerJumpEvent;
import com.gamesense.api.settings.Setting;
import com.gamesense.client.AffinityPlus;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Announcer extends Module {
    public static int blockBrokeDelay = 0;
    //TODO: Multilingual announcer :D
    public static String walkMessage = "I just walked{blocks} meters thanks to Affinity+!";
    public static String placeMessage = "I just inserted{amount}{name} into the muliverse thanks to Affinity+";
    public static String jumpMessage = "I just hovered in the air thanks to Affinity+!";
    public static String breakMessage = "I just snapped{amount}{name} out of existance thanks to Affinity+";
    public static String attackMessage = "I just disembowed{name} with a{item} thanks to Affinity+!";
    public static String eatMessage = "I just gobbled up{amount}{name} thanks to Affinity+!";
    public static String guiMessage = "I just opened my advanced hacking console thanks to Affinity+!";

    public static String[] walkMessages = {"I just walked{blocks} meters thanks to Affinity+!"};
    public static String[] placeMessages = {"I just inserted{amount}{name} into the muliverse thanks to Affinity+!"};
    public static String[] jumpMessages = {"I just hovered in the air thanks to Affinity+"};
    public static String[] breakMessages = {"I just snapped{amount}{name} out of existance thanks to Affinity+"};
    public static String[] eatMessages = {"I just ate{amount}{name} thanks to Affinity+"};
    static int blockPlacedDelay = 0;
    static int jumpDelay = 0;
    static int attackDelay = 0;
    static int eattingDelay = 0;
    static long lastPositionUpdate;
    static double lastPositionX;
    static double lastPositionY;
    static double lastPositionZ;
    private static double speed;
    public Setting.Boolean clientSide;
    public Setting.Boolean clickGui;
    String heldItem = "";
    int blocksPlaced = 0;
    int blocksBroken = 0;
    int eaten = 0;
    Setting.Boolean walk;
    Setting.Boolean place;
    Setting.Boolean jump;
    Setting.Boolean breaking;
    Setting.Boolean attack;
    Setting.Boolean eat;
    Setting.Integer delay;
    @EventHandler
    private final Listener<LivingEntityUseItemEvent.Finish> eatListener = new Listener<>(event -> {
        int randomNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
        if (event.getEntity() == mc.player) {
            if (event.getItem().getItem() instanceof ItemFood || event.getItem().getItem() instanceof ItemAppleGold) {
                eaten++;
                if (eattingDelay >= 300 * delay.getValue()) {
                    if (eat.getValue() && eaten > randomNum) {
                        Random random = new Random();
                        if (clientSide.getValue()) {
                            Command.sendClientMessage
                                    (eatMessages[random.nextInt(eatMessages.length)].replace("{amount}", eaten + "").replace("{name}", mc.player.getHeldItemMainhand().getDisplayName()));
                        } else {
                            mc.player.sendChatMessage
                                    (eatMessages[random.nextInt(eatMessages.length)].replace("{amount}", eaten + "").replace("{name}", mc.player.getHeldItemMainhand().getDisplayName()));
                        }
                        eaten = 0;
                        eattingDelay = 0;
                    }
                }
            }
        }
    });
    @EventHandler
    private final Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock && mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemBlock) {
            blocksPlaced++;
            int randomNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
            if (blockPlacedDelay >= 150 * delay.getValue()) {
                if (place.getValue() && blocksPlaced > randomNum) {
                    Random random = new Random();
                    String msg = placeMessages[random.nextInt(placeMessages.length)].replace("{amount}", blocksPlaced + "").replace("{name}", mc.player.getHeldItemMainhand().getDisplayName());
                    if (clientSide.getValue()) {
                        Command.sendClientMessage(msg);
                    } else {
                        mc.player.sendChatMessage(msg);
                    }
                    blocksPlaced = 0;
                    blockPlacedDelay = 0;
                }
            }
        }
    });
    @EventHandler
    private final Listener<DestroyBlockEvent> destroyListener = new Listener<>(event -> {
        blocksBroken++;
        int randomNum = ThreadLocalRandom.current().nextInt(1, 10 + 1);
        if (blockBrokeDelay >= 300 * delay.getValue()) {
            if (breaking.getValue() && blocksBroken > randomNum) {
                Random random = new Random();
                String msg = breakMessages[random.nextInt(breakMessages.length)]
                        .replace("{amount}", blocksBroken + "")
                        .replace("{name}", mc.world.getBlockState(event.getBlockPos()).getBlock().getLocalizedName());
                if (clientSide.getValue()) {
                    Command.sendClientMessage(msg);
                } else {
                    mc.player.sendChatMessage(msg);
                }
                blocksBroken = 0;
                blockBrokeDelay = 0;
            }
        }
    });
    @EventHandler
    private final Listener<AttackEntityEvent> attackListener = new Listener<>(event -> {
        if (attack.getValue() && !(event.getTarget() instanceof EntityEnderCrystal)) {
            if (attackDelay >= 300 * delay.getValue()) {
                String msg = attackMessage.replace("{name}", event.getTarget().getName()).replace("{item}", mc.player.getHeldItemMainhand().getDisplayName());
                if (clientSide.getValue()) {
                    Command.sendClientMessage(msg);
                } else {
                    mc.player.sendChatMessage(msg);
                }
                attackDelay = 0;
            }
        }
    });
    @EventHandler
    private final Listener<PlayerJumpEvent> jumpListener = new Listener<>(event -> {
        if (jump.getValue()) {
            if (jumpDelay >= 300 * delay.getValue()) {
                if (clientSide.getValue()) {
                    Random random = new Random();
                    Command.sendClientMessage(jumpMessages[random.nextInt(jumpMessages.length)]);
                } else {
                    Random random = new Random();
                    mc.player.sendChatMessage(jumpMessages[random.nextInt(jumpMessages.length)]);
                }
                jumpDelay = 0;
            }
        }
    });

    public Announcer() {
        super("Announcer", Category.Misc);
    }

    public void setup() {

        clientSide = registerBoolean("Client Side", "ClientSide", false);
        walk = registerBoolean("Walk", "Walk", true);
        place = registerBoolean("Place", "Place", true);
        jump = registerBoolean("Jump", "Jump", true);
        breaking = registerBoolean("Breaking", "Breaking", true);
        attack = registerBoolean("Attack", "Attack", true);
        eat = registerBoolean("Eat", "Eat", true);
        clickGui = registerBoolean("DevGUI", "DevGUI", true);
        delay = registerInteger("Delay", "Delay", 1, 1, 20);
    }

    public void onUpdate() {
        blockBrokeDelay++;
        blockPlacedDelay++;
        jumpDelay++;
        attackDelay++;
        eattingDelay++;
        heldItem = mc.player.getHeldItemMainhand().getDisplayName();

        if (walk.getValue()) {
            if (lastPositionUpdate + (5000L * delay.getValue()) < System.currentTimeMillis()) {

                double d0 = lastPositionX - mc.player.lastTickPosX;
                double d2 = lastPositionY - mc.player.lastTickPosY;
                double d3 = lastPositionZ - mc.player.lastTickPosZ;


                speed = Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);

                if (!(speed <= 1) && !(speed > 5000)) {
                    String walkAmount = new DecimalFormat("0.00").format(speed);

                    Random random = new Random();
                    if (clientSide.getValue()) {
                        Command.sendClientMessage(walkMessage.replace("{blocks}", walkAmount));
                    } else {
                        mc.player.sendChatMessage(walkMessages[random.nextInt(walkMessages.length)].replace("{blocks}", walkAmount));
                    }
                    lastPositionUpdate = System.currentTimeMillis();
                    lastPositionX = mc.player.lastTickPosX;
                    lastPositionY = mc.player.lastTickPosY;
                    lastPositionZ = mc.player.lastTickPosZ;
                }
            }
        }

    }

    public void onEnable() {
        AffinityPlus.EVENT_BUS.subscribe(this);
        blocksPlaced = 0;
        blocksBroken = 0;
        eaten = 0;
        speed = 0;
        blockBrokeDelay = 0;
        blockPlacedDelay = 0;
        jumpDelay = 0;
        attackDelay = 0;
        eattingDelay = 0;
    }

    public void onDisable() {
        AffinityPlus.EVENT_BUS.unsubscribe(this);
    }

}