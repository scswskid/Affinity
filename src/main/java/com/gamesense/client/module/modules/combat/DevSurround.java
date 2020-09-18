package com.gamesense.client.module.modules.combat;

import com.gamesense.api.settings.Setting;
import com.gamesense.api.util.world.BlockUtils;
import com.gamesense.client.module.Module;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.List;

public class DevSurround extends Module {
    public final Setting.Integer BPT = registerInteger("Blocks Per Tick", "BPT", 4, 1, 10);
    public final Setting.Boolean cancelOnAir = registerBoolean("Cancel On Air", "CancelOnAir", true);
    public final Setting.Boolean autoCenter = registerBoolean("Auto Center", "AutoCenter", true);

    public DevSurround() {
        super("Dev Surround", Category.Combat);
    }

    int blocksPerTickCount = 0;

    @Override
    public void onUpdate() {
        blocksPerTickCount = 0;
        BlockPos current = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
        List<BlockPos> posList = Arrays.asList(
                current.west(),
                current.west().north(),
                current.north(),
                current.north().east(),
                current.east(),
                current.east().south(),
                current.south(),
                current.south().west()
        );
        List<EnumFacing> wantedFacing = Arrays.asList(
                EnumFacing.UP,
                EnumFacing.DOWN,
                EnumFacing.WEST,
                EnumFacing.EAST,
                EnumFacing.NORTH,
                EnumFacing.SOUTH
        );

        for (BlockPos b : posList) {
            for (EnumFacing f : wantedFacing) {
                placeBlock(b, f);
                if (cancelOnAir.getValue()) if (mc.player.isAirBorne) return;
                if (BPT.getValue() >= blocksPerTickCount) return;
            }
            if (cancelOnAir.getValue()) if (mc.player.isAirBorne) return;
            if (BPT.getValue() >= blocksPerTickCount) return;
        }
    }

    private void placeBlock(BlockPos b, EnumFacing facing) {
        if (BlockUtils.getBlock(b) != Blocks.AIR) {
            blocksPerTickCount++;
            return;
        }
        mc.playerController.clickBlock(b, facing);
        mc.playerController.updateController();
    }

    private void equipObsidian() {
        int obiPos = -1;
    }
}
