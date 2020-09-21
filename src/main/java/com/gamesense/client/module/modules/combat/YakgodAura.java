package com.gamesense.client.module.modules.combat;

import com.gamesense.api.players.friends.Friends;
import com.gamesense.api.settings.Setting;
import com.gamesense.api.util.world.EntityUtil;
import com.gamesense.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;

public class YakgodAura extends Module {
    public YakgodAura() {
        super("YakgodAura", Category.Combat);
    }

    private Setting.Boolean attackPlayers = registerBoolean("Players", "Players", true);
    private Setting.Boolean attackMobs = registerBoolean("Mobs", "Mobs", false);
    private Setting.Boolean attackAnimals = registerBoolean("Animals", "Animals", false);
    private Setting.Boolean ignoreWalls = registerBoolean("Ignore Walls", "IgnoreWalls", true);
    private Setting.Boolean switchTo32k = registerBoolean("32k Switch", "32kSwitch", true);
    private Setting.Boolean onlyUse32k = registerBoolean("32k Only", "32kOnly", true);
    private Setting.Double hitRange = registerDouble("Hit Range", "HitRange", 5.5d, 0.0d, 25.0d);
    private Setting.Integer waitTick = registerInteger("Tick Delay", "TickDelay", 3, 0, 20);

    private int waitCounter = 0;

    @Override
    public void onUpdate() {
        if (mc.player.isDead) {
            return;
        }

        if (waitCounter < waitTick.getValue()) {
            waitCounter++;
            return;
        } else {
            waitCounter = 0;
        }

        Iterator<Entity> entityIterator = mc.world.loadedEntityList.iterator();
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

        NBTTagList enchants = (NBTTagList) stack.getTagCompound().getTag("ench");

        // IntelliJ marks (enchants == null) as always false but this is not correct.
        // In case of a Hotbar Slot without any Enchantment on the Stack it contains,
        // this will throw a NullPointerException if not accounted for!
        //noinspection ConstantConditions
        if (enchants == null) {
            return false;
        }

        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (enchant.getInteger("id") == 16) {
                int lvl = enchant.getInteger("lvl");
                if (lvl >= 42) { // dia sword against full prot 5 armor is deadly somehere >= 34 sharpness iirc
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
            holding32k = true;
        }

        if (switchTo32k.getValue() && !holding32k) {

            int newSlot = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack == ItemStack.EMPTY) {
                    continue;
                }
                if (checkSharpness(stack)) {
                    newSlot = i;
                    break;
                }
            }

            if (newSlot != -1) {
                mc.player.inventory.currentItem = newSlot;
                holding32k = true;
            }

        }

        if (onlyUse32k.getValue() && !holding32k) {
            return;
        }

        mc.playerController.attackEntity(mc.player, e);
        mc.player.swingArm(EnumHand.MAIN_HAND);

    }
    private boolean canEntityFeetBeSeen(Entity entityIn) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ), false, true, false) == null;
    }
}
