package com.gamesense.client.module.modules.movement;

import com.gamesense.client.module.Module;
import com.gamesense.client.module.ModuleManager;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

//import net.minecraft.network.Packet;
//import net.minecraft.world.IBlockAccess;

public class FastFall extends Module {
    private final double[] oneblockPositions = new double[]{0.42, 0.75};
    private int packets;
    private boolean jumped;
    public FastFall() {
        super("FastFall", Category.Movement);
    }

    public void onUpdate() {
        if (FastFall.mc.world == null || FastFall.mc.player == null || ModuleManager.isModuleEnabled("Speed")) {
            return;
        }
        if (!FastFall.mc.player.onGround) {
            if (FastFall.mc.gameSettings.keyBindJump.isKeyDown()) {
                this.jumped = true;
            }
        } else {
            this.jumped = false;
        }
        if (!this.jumped && FastFall.mc.player.fallDistance < 0.5 && this.isInHole() && FastFall.mc.player.posY - this.getNearestBlockBelow() <= 1.125 && FastFall.mc.player.posY - this.getNearestBlockBelow() <= 0.95 && !this.isOnLiquid() && !this.isInLiquid()) {
            if (!FastFall.mc.player.onGround) {
                this.packets++;
            }
            if (!FastFall.mc.player.onGround && !FastFall.mc.player.isInsideOfMaterial(Material.WATER) && !FastFall.mc.player.isInsideOfMaterial(Material.LAVA) && !FastFall.mc.gameSettings.keyBindJump.isKeyDown() && !FastFall.mc.player.isOnLadder() && this.packets > 0) {
                final BlockPos blockPos = new BlockPos(FastFall.mc.player.posX, FastFall.mc.player.posY, FastFall.mc.player.posZ);
                for (final double position : this.oneblockPositions) {
                    FastFall.mc.player.connection.sendPacket(new CPacketPlayer.Position(blockPos.getX() + 0.5f, FastFall.mc.player.posY - position, blockPos.getZ() + 0.5f, true));
                }
                FastFall.mc.player.setPosition(blockPos.getX() + 0.5f, this.getNearestBlockBelow() + 0.1, blockPos.getZ() + 0.5f);
                this.packets = 0;
            }
        }
    }

    private boolean isInHole() {
        final BlockPos blockPos = new BlockPos(FastFall.mc.player.posX, FastFall.mc.player.posY, FastFall.mc.player.posZ);
        final IBlockState blockState = FastFall.mc.world.getBlockState(blockPos);
        return this.isBlockValid(blockState, blockPos);
    }

    private double getNearestBlockBelow() {
        for (double y = FastFall.mc.player.posY; y > 0.0; y -= 0.001) {
            if (!(FastFall.mc.world.getBlockState(new BlockPos(FastFall.mc.player.posX, y, FastFall.mc.player.posZ)).getBlock() instanceof BlockSlab) && FastFall.mc.world.getBlockState(new BlockPos(FastFall.mc.player.posX, y, FastFall.mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(FastFall.mc.world, new BlockPos(0, 0, 0)) != null) {
                return y;
            }
        }
        return -1.0;
    }

    private boolean isBlockValid(final IBlockState blockState, final BlockPos blockPos) {
        return blockState.getBlock() == Blocks.AIR && FastFall.mc.player.getDistanceSq(blockPos) >= 1.0 && FastFall.mc.world.getBlockState(blockPos.up()).getBlock() == Blocks.AIR && FastFall.mc.world.getBlockState(blockPos.up(2)).getBlock() == Blocks.AIR && (this.isBedrockHole(blockPos) || this.isObbyHole(blockPos) || this.isBothHole(blockPos) || this.isElseHole(blockPos));
    }

    private boolean isObbyHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (final BlockPos touching : array) {
            final IBlockState touchingState = FastFall.mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
        }
        return true;
    }

    private boolean isBedrockHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (final BlockPos touching : array) {
            final IBlockState touchingState = FastFall.mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || touchingState.getBlock() != Blocks.BEDROCK) {
                return false;
            }
        }
        return true;
    }

    private boolean isBothHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (final BlockPos touching : array) {
            final IBlockState touchingState = FastFall.mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || (touchingState.getBlock() != Blocks.BEDROCK && touchingState.getBlock() != Blocks.OBSIDIAN)) {
                return false;
            }
        }
        return true;
    }

    private boolean isElseHole(final BlockPos blockPos) {
        final BlockPos[] array;
        final BlockPos[] touchingBlocks = array = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
        for (final BlockPos touching : array) {
            final IBlockState touchingState = FastFall.mc.world.getBlockState(touching);
            if (touchingState.getBlock() == Blocks.AIR || !touchingState.isFullBlock()) {
                return false;
            }
        }
        return true;
    }

    private boolean isOnLiquid() {
        final double y = FastFall.mc.player.posY - 0.03;
        for (int x = MathHelper.floor(FastFall.mc.player.posX); x < MathHelper.ceil(FastFall.mc.player.posX); x++) {
            for (int z = MathHelper.floor(FastFall.mc.player.posZ); z < MathHelper.ceil(FastFall.mc.player.posZ); z++) {
                final BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);
                if (FastFall.mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInLiquid() {
        final double y = FastFall.mc.player.posY + 0.01;
        for (int x = MathHelper.floor(FastFall.mc.player.posX); x < MathHelper.ceil(FastFall.mc.player.posX); x++) {
            for (int z = MathHelper.floor(FastFall.mc.player.posZ); z < MathHelper.ceil(FastFall.mc.player.posZ); z++) {
                final BlockPos pos = new BlockPos(x, (int) y, z);
                if (FastFall.mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
                    return true;
                }
            }
        }
        return false;
    }
}
