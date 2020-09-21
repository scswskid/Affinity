package com.gamesense.client.module.modules.combat;

import com.gamesense.api.settings.Setting;
import com.gamesense.api.util.BlockInteractionHelper;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemStack;
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
    private int stage;
    private BlockPos placeTarget;
    private int bedSlot;
    private boolean isSneaking;

    public BedAura() {
        super("Bed Aura", Category.Combat);
    }

    @Override
    protected void onEnable() {
        if (mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            this.disable();
            return;
        }
        df.setRoundingMode(RoundingMode.CEILING);
        this.stage = 0;
        this.placeTarget = null;
        this.bedSlot = -1;
        this.isSneaking = false;
        for (int i = 0; i < 9 && (this.bedSlot == -1); ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if(stack.getItem() instanceof ItemBed) {
                this.bedSlot = 1;
                break;
            }
        }
        if (this.bedSlot == -1) {
            if (this.debugMessages.getValue()) {
                Command.sendClientMessage("[AutoBedBomb] Bed(s) missing, disabling.");
            }
            this.disable();
            return;
        }
        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null || mc.objectMouseOver.getBlockPos().up() == null) {
            if (this.debugMessages.getValue()) {
                Command.sendClientMessage("[AutoBedBomb] Not a valid place target, disabling.");
            }
            this.disable();
            return;
        }
        this.placeTarget = mc.objectMouseOver.getBlockPos().up();

        if (this.debugMessages.getValue() == false) return;

    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        if (ModuleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (this.stage == 0) {
            mc.player.inventory.currentItem = this.bedSlot;
            this.placeBlock(new BlockPos(this.placeTarget), EnumFacing.DOWN);

            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            this.isSneaking = false;
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.placeTarget.add(0, 0, 0), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            this.stage = 1;
            return;
        }
        if (!chainBomb.getValue()) {
            this.disable();
            return;
        }

        int newBed = findBedSlot();

        if (newBed == -1) {
            Command.sendClientMessage("[AutoBedBomb] Bed(s) missing, disabling.");
            return;
        }

        mc.playerController.windowClick(0, findBedSlot(), 0, ClickType.PICKUP, mc.player);
        mc.playerController.updateController();
        mc.playerController.windowClick(0, bedSlot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.updateController();
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