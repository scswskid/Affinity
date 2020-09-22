package com.gamesense.client.module.modules.combat;

import com.gamesense.api.players.friends.Friends;
import com.gamesense.api.settings.Setting;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Iterator;

public class BruceAura extends Module {
    private Setting.Boolean attackPlayers = registerBoolean("Players", "Players", true);
    private Setting.Boolean attackMobs = registerBoolean("Mobs", "Mobs", false);
    private Setting.Boolean attackAnimals = registerBoolean("Animals", "Animals", false);
    private Setting.Boolean ignoreWalls = registerBoolean("Ignore Walls", "IgnoreWalls", true);
    private Setting.Boolean autoSwapReverted = registerBoolean("Auto Throw Reverted", "AutoThrowReverted", true);
    private Setting.Double hitRange = registerDouble("Hit Range", "HitRange", 5.5d, 0.0d, 25.0d);
    private Setting.Double waitTick = registerDouble("Tick Delay", "TickDelay", 2.0d, 0.0d, 20.0d);
    private Setting.Mode switchMode = registerMode("Auto Switch", "Autoswitch", Arrays.asList(
            "NONE",
            "ALL",
            "Only32k"
    ), "Only32k");
    private Setting.Mode hitMode = registerMode("Hit Mode", "Tool", Arrays.asList(
            "SWORD",
            "AXE",
            "Only32k"
    ), "SWORD");
    private int waitCounter;
    private int cached32kSlot = -1;

    public BruceAura() {
        super("Bruce Aura", Category.Combat);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            return;
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player.isDead || mc.player == null) {
            return;
        }

        boolean shield = mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) && mc.player.getActiveHand() == EnumHand.OFF_HAND;
        if (mc.player.isHandActive() && !shield) {
            return;
        }

        if (waitTick.getValue() > 0) {
            if (waitCounter < waitTick.getValue()) {
                waitCounter++;
                return;
            } else {
                waitCounter = 0;
            }
        }

        if (autoSwapReverted.getValue() && switchMode.getValue().equals("Only32k")) {
            if (cached32kSlot == -1) return;

            if (!is32kSword(mc.player.inventory.getStackInSlot(cached32kSlot)))
                mc.playerController.windowClick(0, cached32kSlot, 0, ClickType.THROW, mc.player);

            if (!(mc.currentScreen instanceof GuiContainer)) return;
            if (((GuiContainer) mc.currentScreen).inventorySlots.getSlot(0).getStack().isEmpty) return;
            if (!checkSharpness(((GuiContainer) mc.currentScreen).inventorySlots.getSlot(0).getStack())) return;
            mc.playerController.windowClick(mc.player.openContainer.windowId, 0, mc.player.inventory.currentItem, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, cached32kSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        }

        Iterator<Entity> entityIterator = Minecraft.getMinecraft().world.loadedEntityList.iterator();
        while (entityIterator.hasNext()) {
            Entity target = entityIterator.next();
            if (!EntityUtil.isLiving(target)) {
                continue;
            }
            if (target == mc.player) {
                continue;
            }
            if (mc.player.getDistance(target) > hitRange.getValue()) {
                continue;
            }
            if (((EntityLivingBase) target).getHealth() <= 0) {
                continue;
            }
            if (!ignoreWalls.getValue() && (!mc.player.canEntityBeSeen(target) && !canEntityFeetBeSeen(target))) {
                continue; // If walls is on & you can't see the feet or head of the target, skip. 2 raytraces needed
            }
            if (attackPlayers.getValue() && target instanceof EntityPlayer && !Friends.isFriend(target.getName())) {
                attack(target);
                return;
            } else {
                if (EntityUtil.isPassive(target) ? attackAnimals.getValue() : (EntityUtil.isMobAggressive(target) && attackMobs.getValue())) {
                    // We want to skip this if switchTo32k.getValue() is true,
                    // because it only accounts for tools and weapons.
                    // Maybe someone could refactor this later? :3
                    attack(target);
                    return;
                }
            }
        }

    }

    private boolean checkSharpness(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return false;
        }

        if (!stack.getItem().equals(Items.DIAMOND_SWORD) && hitMode.getValue().equals("SWORD")) {
            return false;
        }

        if (!stack.getItem().equals(Items.DIAMOND_AXE) && hitMode.getValue().equals("AXE")) {
            return false;
        }

        NBTTagList enchants = (NBTTagList) stack.getTagCompound().getTag("ench");

        if (enchants == null) {
            return false;
        }

        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
                int lvl = enchant.getInteger("lvl");
                if (switchMode.getValue().equals("Only32k")) {
                    if (lvl >= 42) {
                        return true;
                    }
                } else if (switchMode.getValue().equals("ALL")) {
                    if (lvl >= 4) {
                        return true;
                    }
                } else if (switchMode.getValue().equals("NONE")) {
                    return true;
                }
                break;
            }
        }

        return false;

    }

    private void attack(Entity e) {
        boolean holding32k = false;

        if (checkSharpness(mc.player.getHeldItemMainhand())) {
            if (cached32kSlot == -1) {
                for (int i = 0; i < 9; i++) {
                    if (mc.player.inventory.getStackInSlot(i) == mc.player.getHeldItemMainhand()) cached32kSlot = i + 36;
                    return;
                }
            }
            holding32k = true;
        }

        if ((switchMode.getValue().equals("Only32k") || switchMode.getValue().equals("ALL")) && !holding32k) {
            int newSlot = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack == ItemStack.EMPTY) {
                    continue;
                }
                if (checkSharpness(stack)) {
                    newSlot = i;
                    if (cached32kSlot == -1) cached32kSlot = i;
                    break;
                }
            }

            if (newSlot != -1) {
                mc.player.inventory.currentItem = newSlot;
                holding32k = true;
            }

        }

        if (switchMode.getValue().equals("Only32k") && !holding32k) {
            return;
        }

        mc.playerController.attackEntity(mc.player, e);
        mc.player.swingArm(EnumHand.MAIN_HAND);

    }

    private boolean canEntityFeetBeSeen(Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }

    private boolean is32kSword(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return false;
        }

        NBTTagList enchants = (NBTTagList) stack.getTagCompound().getTag("ench");

        if (enchants == null) {
            return false;
        }

        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
                int lvl = enchant.getInteger("lvl");
                if (lvl >= 42) {
                    return true;
                }
                break;
            }
        }

        return false;
    }
}