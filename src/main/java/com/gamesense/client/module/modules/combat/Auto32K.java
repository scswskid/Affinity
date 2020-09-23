package com.gamesense.client.module.modules.combat;

import com.gamesense.api.settings.Setting;
import com.gamesense.api.util.world.BlockUtils;
import com.gamesense.client.AffinityPlus;
import com.gamesense.client.command.Command;
import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Auto32K extends Module {
    private static final DecimalFormat df = new DecimalFormat("#.#");
    private final Setting.Boolean rotate = registerBoolean("Rotate", "Rotate", false);
    private final Setting.Boolean grabItem = registerBoolean("Grab Item", "Grab Item", false);
    private final Setting.Boolean autoEnableHitAura = registerBoolean("Auto enable Hit Aura", "Auto enable Hit Aura", false);
    private final Setting.Boolean debugMessages = registerBoolean("Debug Messages", "Debug Messages", false);
    private int stage;
    private BlockPos placeTarget;
    private int obiSlot;
    private int dispenserSlot;
    private int shulkerSlot;
    private int redstoneSlot;
    private int hopperSlot;
    private boolean isSneaking;

    public Auto32K() {
        super("Auto32K", Category.Combat);
    }

    @Override
    protected void onEnable() {
        if (Auto32K.mc.player == null || ModuleManager.isModuleEnabled("Freecam")) {
            this.disable();
            return;
        }
        df.setRoundingMode(RoundingMode.CEILING);
        this.stage = 0;
        this.placeTarget = null;
        this.obiSlot = -1;
        this.dispenserSlot = -1;
        this.shulkerSlot = -1;
        this.redstoneSlot = -1;
        this.hopperSlot = -1;
        this.isSneaking = false;
        for (int i = 0; i < 9 && (this.obiSlot == -1 || this.dispenserSlot == -1 || this.shulkerSlot == -1 || this.redstoneSlot == -1 || this.hopperSlot == -1); ++i) {
            ItemStack stack = Auto32K.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !(stack.getItem() instanceof ItemBlock)) continue;
            Block block = ((ItemBlock) stack.getItem()).getBlock();
            if (block == Blocks.HOPPER) {
                this.hopperSlot = i;
                continue;
            }
            if (BlockUtils.shulkerList.contains(block)) {
                this.shulkerSlot = i;
                continue;
            }
            if (block == Blocks.OBSIDIAN) {
                this.obiSlot = i;
                continue;
            }
            if (block == Blocks.DISPENSER) {
                this.dispenserSlot = i;
                continue;
            }
            if (block != Blocks.REDSTONE_BLOCK) continue;
            this.redstoneSlot = i;
        }
        if (this.obiSlot == -1 || this.dispenserSlot == -1 || this.shulkerSlot == -1 || this.redstoneSlot == -1 || this.hopperSlot == -1) {
            if (this.debugMessages.getValue()) {
                Command.sendClientMessage("Auto32k: Items missing, disabling.");
            }
            this.disable();
            return;
        }
        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null || mc.objectMouseOver.getBlockPos().up() == null) {
            if (this.debugMessages.getValue()) {
                Command.sendClientMessage("Auto32k: Not a valid place target, disabling.");
            }
            this.disable();
            return;
        }
        this.placeTarget = Auto32K.mc.objectMouseOver.getBlockPos().up();
        if (!this.debugMessages.getValue()) return;
        Command.sendClientMessage("Auto32k: Place Target is " + this.placeTarget.x + " " + this.placeTarget.y + " " + this.placeTarget.z + " Distance: " + df.format(Auto32K.mc.player.getPositionVector().distanceTo(new Vec3d(this.placeTarget))));
    }

    @Override
    public void onUpdate() {
        if (Auto32K.mc.player == null) return;
        if (AffinityPlus.getInstance().moduleManager.isModuleEnabled("Freecam")) {
            return;
        }
        if (this.stage == 0) {
            mc.player.inventory.currentItem = this.obiSlot;
            this.placeBlock(new BlockPos(this.placeTarget), EnumFacing.DOWN);
            mc.player.inventory.currentItem = this.dispenserSlot;
            this.placeBlock(new BlockPos(this.placeTarget.add(0, 1, 0)), EnumFacing.DOWN);
            mc.player.connection.sendPacket(new CPacketEntityAction(Auto32K.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            this.isSneaking = false;
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.placeTarget.add(0, 1, 0), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            this.stage = 1;
            return;
        }
        if (this.stage == 1) {
            if (!(mc.currentScreen instanceof GuiContainer)) {
                return;
            }
            mc.playerController.windowClick(mc.player.openContainer.windowId, 1, this.shulkerSlot, ClickType.SWAP, mc.player);
            mc.player.closeScreen();
            mc.player.inventory.currentItem = this.redstoneSlot;
            this.placeBlock(new BlockPos(this.placeTarget.add(0, 2, 0)), EnumFacing.DOWN);
            this.stage = 2;
            return;
        }
        if (this.stage == 2) {
            Block block = mc.world.getBlockState(this.placeTarget.offset(Auto32K.mc.player.getHorizontalFacing().getOpposite()).up()).getBlock();
            if (block instanceof BlockAir) return;
            if (block instanceof BlockLiquid) {
                return;
            }
            mc.player.inventory.currentItem = this.hopperSlot;
            this.placeBlock(new BlockPos(this.placeTarget.offset(mc.player.getHorizontalFacing().getOpposite())), mc.player.getHorizontalFacing());
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            this.isSneaking = false;
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.placeTarget.offset(mc.player.getHorizontalFacing().getOpposite()), EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            mc.player.inventory.currentItem = this.shulkerSlot;
            if (!this.grabItem.getValue()) {
                this.disable();
                return;
            }
            this.stage = 3;
            return;
        }
        if (this.stage != 3) return;
        if (!(mc.currentScreen instanceof GuiContainer)) {
            return;
        }

        if (((GuiContainer) mc.currentScreen).inventorySlots.getSlot(0).getStack().isEmpty) return;

//        mc.playerController.windowClick(mc.player.openContainer.windowId, 0, 0, ClickType.PICKUP, mc.player);
//        mc.playerController.windowClick(mc.player.openContainer.windowId, shulkerSlot, 0, ClickType.PICKUP, mc.player);
//        hello
        mc.playerController.windowClick(mc.player.openContainer.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
        if (this.autoEnableHitAura.getValue()) {
            ModuleManager.getModuleByName("Bruce Aura").enable();
            ModuleManager.getModuleByName("YakgodAura").enable();
        }
        this.disable();
    }

    private void placeBlock(BlockPos pos, EnumFacing side) {
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        if (!this.isSneaking) {
            Auto32K.mc.player.connection.sendPacket(new CPacketEntityAction(Auto32K.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            this.isSneaking = true;
        }
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        if (this.rotate.getValue()) {
            BlockUtils.faceVectorPacketInstant(hitVec);
        }
        Auto32K.mc.playerController.processRightClickBlock(Auto32K.mc.player, Auto32K.mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        Auto32K.mc.player.swingArm(EnumHand.MAIN_HAND);
    }
}