package com.gamesense.client.module.modules.combat;

import com.gamesense.api.settings.Setting;
import com.gamesense.api.util.BlockInteractionHelper;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBed;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class BedAura extends Module {
    private static final DecimalFormat df = new DecimalFormat("#.#");
    private final Setting.Boolean rotate = this.registerBoolean("Rotate", "Rotate", false);
    private final Setting.Boolean debugMessages = this.registerBoolean("Debug Messages", "DebugMessages", false);
    private final Setting.Boolean chainBomb = this.registerBoolean("Chain Bomb", "ChainBomb", true);
    private final Setting.Boolean fastMode = this.registerBoolean("Fast Mode", "FastMode", true);
    private final Setting.Integer tickDelay = this.registerInteger("Tick Delay", "TickDelay", 5, 0, 40);
    private BlockPos placeTarget;
    private int bedSlot;
    private int ticksWaited = 0;
    private int normalTickDelay;
    private boolean isSneaking;
    private boolean firstRun;

    public BedAura() {
        super("Bed Aura", Category.Combat);
    }

    @Override
    protected void onEnable() {
        if (mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            this.disable();
            return;
        }

        normalTickDelay = mc.rightClickDelayTimer;

        df.setRoundingMode(RoundingMode.CEILING);

        this.placeTarget = null;
        this.bedSlot = -1;

        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.BED) this.bedSlot = i;
        }
        this.isSneaking = false;

        if (this.bedSlot == -1) {
            if (this.debugMessages.getValue()) {
                Command.sendClientMessage("BedAura: Bed(s) missing, disabling.");
            }
            this.disable();
            return;
        }

        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null || mc.objectMouseOver.getBlockPos().up() == null) {
            if (this.debugMessages.getValue()) {
                Command.sendClientMessage("BedAura: Not a valid place target, disabling.");
            }
            this.disable();
            return;
        }

        this.placeTarget = mc.objectMouseOver.getBlockPos().up();
        firstRun = true;
    }

    @Override public void onUpdate() {
        if (mc.player == null || mc.world == null) return;
        if (ModuleManager.isModuleEnabled("Freecam")) return;
        if (!firstRun && tickDelay.getValue() > ticksWaited) {
            ticksWaited++;
            return;
        }

        if (firstRun) firstRun = false;

        ticksWaited = 0;

        mc.player.inventory.currentItem = this.bedSlot;

        if (fastMode.getValue()) {
            if (mc.player.getHeldItemMainhand().getItem() == Items.BED) {
                mc.rightClickDelayTimer = 0;
            }
        } else {
            mc.rightClickDelayTimer = normalTickDelay;
        }

        this.placeBlock(new BlockPos(this.placeTarget), EnumFacing.DOWN);

        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        this.isSneaking = false;
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.placeTarget.add(0, 0, 0), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
        if (!chainBomb.getValue()) {
            this.disable();
            return;
        }
        int nextBed = findBedSlot();
        if (nextBed == -1) {
            Command.sendClientMessage("BedAura: Bed(s) missing, disabling.");
            disable();
        } else {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, nextBed, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, bedSlot < 9 ? bedSlot + 36 : bedSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, nextBed, 0, ClickType.PICKUP, mc.player);

            mc.playerController.updateController();
        }
    }

    private void placeBlock(BlockPos pos, EnumFacing side) {
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        if (!this.isSneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            this.isSneaking = true;
        }
        Vec3d hitVec = new Vec3d(neighbour.add(0.5, 0.5, 0.5).add(opposite.getDirectionVec())); //add .scale(0.5)
        if (this.rotate.getValue()) {
            BlockInteractionHelper.faceVectorPacketInstant(hitVec);
        }
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    private int findBedSlot() {
        for (int i = 9; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBed) return i;
        }

        return -1;
    }
}