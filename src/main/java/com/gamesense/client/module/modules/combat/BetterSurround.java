package com.gamesense.client.module.modules.combat;

import com.gamesense.api.settings.Setting;
import com.gamesense.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BetterSurround extends Module {
    private Setting.Boolean sneak = registerBoolean("Only When Sneaking", "Sneak", false);
    private Setting.Boolean teleport = registerBoolean("Auto Center", "Teleport", true);
    private Setting.Boolean endChest = registerBoolean("Use EChests", "EndChest", false);
    private Setting.Boolean jumpDisable = registerBoolean("Disable On Jump", "JumpDisable", true);
    private Setting.Boolean autoToggle = registerBoolean("Auto Toggle", "AutoToggle", false);
    private Setting.Boolean chainPopToggle = registerBoolean("Anti Chain Pop", "ChainPopToggle", false);

    public BetterSurround() {
        super("BetterSurround", Category.Combat);
    }

    public static boolean hasNeighbour(BlockPos blockPos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if (!mc.world.getBlockState(neighbour).getMaterial().isReplaceable())
                return true;
        }
        return false;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        if (sneak.getValue() && !mc.gameSettings.keyBindSneak.isKeyDown()) return;
        if (!mc.player.onGround && jumpDisable.getValue()) {
            toggle();
            return;
        }
        final Vec3d vec3d = getInterpolatedPos(mc.player, 0);
        BlockPos northBlockPos = new BlockPos(vec3d).north();
        BlockPos southBlockPos = new BlockPos(vec3d).south();
        BlockPos eastBlockPos = new BlockPos(vec3d).east();
        BlockPos westBlockPos = new BlockPos(vec3d).west();
        final int newSlot = findBlockInHotbar();
        if (newSlot == -1)
            return;
        final BlockPos centerPos = mc.player.getPosition();
        double y = centerPos.getY();
        double x = centerPos.getX();
        double z = centerPos.getZ();
        final Vec3d plusPlus = new Vec3d(x + 0.5, y, z + 0.5);
        final Vec3d plusMinus = new Vec3d(x + 0.5, y, z - 0.5);
        final Vec3d minusMinus = new Vec3d(x - 0.5, y, z - 0.5);
        final Vec3d minusPlus = new Vec3d(x - 0.5, y, z + 0.5);
        final int oldSlot = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = newSlot;
        if (!hasNeighbour(northBlockPos)) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = northBlockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    northBlockPos = neighbour;
                    break;
                }
            }
        }

        if (!hasNeighbour(southBlockPos)) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = southBlockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    southBlockPos = neighbour;
                    break;
                }
            }
        }

        if (!hasNeighbour(eastBlockPos)) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = eastBlockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    eastBlockPos = neighbour;
                    break;
                }
            }
        }

        if (!hasNeighbour(westBlockPos)) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = westBlockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    westBlockPos = neighbour;
                    break;
                }
            }
        }

        if (mc.world.getBlockState(northBlockPos).getMaterial().isReplaceable() && isEntitiesEmpty(northBlockPos)) {
            if (mc.player.onGround && teleport.getValue()) {
                if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
            }
            placeBlockScaffold(northBlockPos, true);
        }

        if (mc.world.getBlockState(southBlockPos).getMaterial().isReplaceable() && isEntitiesEmpty(southBlockPos)) {
            if (mc.player.onGround && teleport.getValue()) {
                if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
            }
            placeBlockScaffold(southBlockPos, true);
        }

        if (mc.world.getBlockState(eastBlockPos).getMaterial().isReplaceable() && isEntitiesEmpty(eastBlockPos)) {
            if (mc.player.onGround && teleport.getValue()) {
                if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
            }
            placeBlockScaffold(eastBlockPos, true);
        }

        if (mc.world.getBlockState(westBlockPos).getMaterial().isReplaceable() && isEntitiesEmpty(westBlockPos)) {
            if (mc.player.onGround && teleport.getValue()) {
                if (getDst(plusPlus) < getDst(plusMinus) && getDst(plusPlus) < getDst(minusMinus) && getDst(plusPlus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(plusMinus) < getDst(plusPlus) && getDst(plusMinus) < getDst(minusMinus) && getDst(plusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() + 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusMinus) < getDst(plusPlus) && getDst(minusMinus) < getDst(plusMinus) && getDst(minusMinus) < getDst(minusPlus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() - 0.5;
                    centerPlayer(x, y, z);
                }
                if (getDst(minusPlus) < getDst(plusPlus) && getDst(minusPlus) < getDst(plusMinus) && getDst(minusPlus) < getDst(minusMinus)) {
                    x = centerPos.getX() - 0.5;
                    z = centerPos.getZ() + 0.5;
                    centerPlayer(x, y, z);
                }
            }
            placeBlockScaffold(westBlockPos, true);
        }
        mc.player.inventory.currentItem = oldSlot;
        if ((autoToggle.getValue() || chainPopToggle.getValue()) && (mc.world.getBlockState(new BlockPos(vec3d).north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(new BlockPos(vec3d).north()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(new BlockPos(vec3d).south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(new BlockPos(vec3d).south()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(new BlockPos(vec3d).west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(new BlockPos(vec3d).west()).getBlock() == Blocks.BEDROCK) && (mc.world.getBlockState(new BlockPos(vec3d).east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(new BlockPos(vec3d).east()).getBlock() == Blocks.BEDROCK)) {
            chainPopToggle.setValue(false);
            toggle();
        }
    }

    private int findBlockInHotbar() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();
                if (block instanceof BlockObsidian) {
                    return i;
                }
            }
        }
        if (endChest.getValue()) {
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                    final Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (block instanceof BlockEnderChest) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private void centerPlayer(double x, double y, double z) {
        mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, true));
        mc.player.setPosition(x, y, z);
    }

    private double getDst(Vec3d vec) {
        return mc.player.getDistance(vec.x, vec.y, vec.z);
    }

    private boolean isEntitiesEmpty(BlockPos pos) {
        return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).stream().filter(e -> !(e instanceof EntityItem) && !(e instanceof EntityXPOrb)).count() == 0;
    }

    public static void placeBlockScaffold(BlockPos pos, boolean rotate) {
        for (EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = pos.offset(side);
            final EnumFacing side2 = side.getOpposite();
            if (!canBeClicked(neighbor))
                continue;
            final Vec3d hitVec = new Vec3d(neighbor).add(new Vec3d(0.5, 0.5, 0.5)).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
            if (rotate) faceVectorPacketInstant(hitVec);
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            processRightClickBlock(neighbor, side2, hitVec);
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.rightClickDelayTimer = 0;
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            return;
        }
    }

    private static PlayerControllerMP getPlayerController() {
        return mc.playerController;
    }

    public static void processRightClickBlock(BlockPos pos, EnumFacing side,
                                              Vec3d hitVec) {
        getPlayerController().processRightClickBlock(mc.player, mc.world, pos, side, hitVec, EnumHand.MAIN_HAND);
    }


    public static boolean canBeClicked(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock().canCollideCheck(mc.world.getBlockState(pos), false);
    }

    public static void faceVectorPacketInstant(Vec3d vec) {
        final float[] rotations = getNeededRotations2(vec);
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], rotations[1], mc.player.onGround));
    }

    private static float[] getNeededRotations2(Vec3d vec) {
        final Vec3d eyesPos = getEyesPos();
        final double diffX = vec.x - eyesPos.x;
        final double diffY = vec.y - eyesPos.y;
        final double diffZ = vec.z - eyesPos.z;
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        final float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return new float[]{ mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)};
    }

    public static Vec3d getEyesPos() {
        return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
    }

    public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks));
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z
        );
    }
}