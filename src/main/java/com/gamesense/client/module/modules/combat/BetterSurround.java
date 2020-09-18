package com.gamesense.client.module.modules.combat;

import com.gamesense.api.event.events.PlayerMoveEvent;
import com.gamesense.api.settings.Setting;
import com.gamesense.api.util.BlockInteractionHelper;
import com.gamesense.client.module.Module;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

public class BetterSurround extends Module {
    public final Setting.Boolean disable = registerBoolean("Toggles", "Toggles", false);
    public final Setting.Boolean ToggleOffGround = registerBoolean("ToggleOffGround", "TogglesOffGround", true);
    public final Setting.Mode CenterMode = registerMode("Center", "Center", Arrays.asList(
            "Teleport",
            "NCP",
            "None"
    ), "NCP");

    public final Setting.Boolean rotate = registerBoolean("Rotate", "Rotate", true);
    public final Setting.Integer BlocksPerTick = registerInteger("BlocksPerTick", "BlocksPerTick", 1, 1, 10);
    public final Setting.Boolean ActivateOnlyOnShift = registerBoolean("ActivateOnlyOnShift", "ActivateOnlyOnShift", false);

    public enum CenterModes
    {
        Teleport,
        NCP,
        None,
    }

    public BetterSurround()
    {
        super("Surround", Category.Combat);
    }

    private Vec3d Center = Vec3d.ZERO;

    @Override
    public void onEnable()
    {
        super.onEnable();

        if (mc.player == null)
        {
            toggle();
            return;
        }

        if (ActivateOnlyOnShift.getValue())
            return;

        Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);

        if (!CenterMode.getValue().equals("None"))
        {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        if (CenterMode.getValue().equals("Teleport"))
        {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(Center.x, Center.y, Center.z, true));
            mc.player.setPosition(Center.x, Center.y, Center.z);
        }
    }

    @EventHandler
    private Listener<PlayerMoveEvent> OnPlayerUpdate = new Listener<>(p_Event ->
    {
        if (ActivateOnlyOnShift.getValue())
        {
            if (!mc.gameSettings.keyBindSneak.isKeyDown())
            {
                Center = Vec3d.ZERO;
                return;
            }

            if (Center == Vec3d.ZERO)
            {
                Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);

                if (!CenterMode.getValue().equals("None"))
                {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }

                if (CenterMode.getValue().equals("Teleport"))
                {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(Center.x, Center.y, Center.z, true));
                    mc.player.setPosition(Center.x, Center.y, Center.z);
                }
            }
        }

        /// NCP Centering
        if (Center != Vec3d.ZERO && CenterMode.getValue().equals("NCP"))
        {
            double l_XDiff = Math.abs(Center.x - mc.player.posX);
            double l_ZDiff = Math.abs(Center.z - mc.player.posZ);

            if (l_XDiff <= 0.1 && l_ZDiff <= 0.1)
            {
                Center = Vec3d.ZERO;
            }
            else
            {
                double l_MotionX = Center.x-mc.player.posX;
                double l_MotionZ = Center.z-mc.player.posZ;

                mc.player.motionX = l_MotionX/2;
                mc.player.motionZ = l_MotionZ/2;
            }
        }

        if (!mc.player.onGround && !ActivateOnlyOnShift.getValue())
        {
            if (ToggleOffGround.getValue())
            {
                toggle();
                return;
            }
        }

        final Vec3d pos = interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

        final BlockPos north = interpPos.north();
        final BlockPos south = interpPos.south();
        final BlockPos east = interpPos.east();
        final BlockPos west = interpPos.west();

        BlockPos[] l_Array = {north, south, east, west};

        /// We don't need to do anything if we are not surrounded
        if (IsSurrounded(mc.player))
            return;

        int lastSlot;
        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        if (hasStack(Blocks.OBSIDIAN) || slot != -1)
        {
            if ((mc.player.onGround))
            {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();

                int l_BlocksPerTick = BlocksPerTick.getValue();

                for (BlockPos l_Pos : l_Array)
                {
                    BlockInteractionHelper.ValidResult l_Result = BlockInteractionHelper.valid(l_Pos);

                    if (l_Result == BlockInteractionHelper.ValidResult.AlreadyBlockThere && !mc.world.getBlockState(l_Pos).getMaterial().isReplaceable())
                        continue;

                    if (l_Result == BlockInteractionHelper.ValidResult.NoNeighbors)
                    {
                        final BlockPos[] l_Test = {  l_Pos.down(), l_Pos.north(), l_Pos.south(), l_Pos.east(), l_Pos.west(), l_Pos.up(), };

                        for (BlockPos l_Pos2 : l_Test)
                        {
                            BlockInteractionHelper.ValidResult l_Result2 = BlockInteractionHelper.valid(l_Pos2);

                            if (l_Result2 == BlockInteractionHelper.ValidResult.NoNeighbors || l_Result2 == BlockInteractionHelper.ValidResult.NoEntityCollision)
                                continue;

                            BlockInteractionHelper.place (l_Pos2, 5.0f, false, false);
                            p_Event.cancel();
                            float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(l_Pos2.getX(), l_Pos2.getY(), l_Pos2.getZ()));
                            break;
                        }

                        continue;
                    }

                    BlockInteractionHelper.place (l_Pos, 5.0f, false, false);

                    p_Event.cancel();

                    float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(l_Pos.getX(), l_Pos.getY(), l_Pos.getZ()));
                    if (--l_BlocksPerTick <= 0)
                        break;
                }

                if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN))
                {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();

                if (this.disable.getValue())
                {
                    this.toggle();
                }
            }
        }
    });

    public boolean IsSurrounded(EntityPlayer p_Who)
    {
        final Vec3d l_PlayerPos = interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos l_InterpPos = new BlockPos(l_PlayerPos.x, l_PlayerPos.y, l_PlayerPos.z);

        final BlockPos l_North = l_InterpPos.north();
        final BlockPos l_South = l_InterpPos.south();
        final BlockPos l_East = l_InterpPos.east();
        final BlockPos l_West = l_InterpPos.west();

        BlockPos[] l_Array = {l_North, l_South, l_East, l_West};

        for (BlockPos l_Pos : l_Array)
        {
            if (BlockInteractionHelper.valid(l_Pos) != BlockInteractionHelper.ValidResult.AlreadyBlockThere)
            {
                return false;
            }
        }

        return true;
    }

    public boolean hasStack(Block type)
    {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    private boolean slotEqualsBlock(int slot, Block type)
    {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock)
        {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private int findStackHotbar(Block type)
    {
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock)
            {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type)
                {
                    return i;
                }
            }
        }
        return -1;
    }

    public Vec3d GetCenter(double posX, double posY, double posZ)
    {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D ;

        return new Vec3d(x, y, z);
    }

    public boolean HasObsidian()
    {
        return findStackHotbar(Blocks.OBSIDIAN) != -1;
    }

    public static Vec3d interpolateEntity(Entity entity, float time)
    {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time,
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time);
    }

}