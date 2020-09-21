package com.gamesense.client.module.modules.misc;

import com.gamesense.api.settings.Setting;
import com.gamesense.client.module.Module;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Refill extends Module {
    private Setting.Integer threshold = registerInteger("Threshold", "Threshold", 10, 1, 63);

    public Refill() {
        super("Refill", Category.Misc);
    }

    @Override
    protected void onEnable() {
        if (mc.player.getHeldItemMainhand().isStackable() && mc.player.getHeldItemMainhand().stackSize < threshold.getValue()) {
            int spot = getRefillItemSpot(mc.player.getHeldItemMainhand().getItem());
            if (spot == -1) return;

            mc.playerController.windowClick(0, spot, 0, ClickType.QUICK_MOVE, mc.player);
        }
    }

    @Override
    public void onUpdate() {
        if (mc.player.getHeldItemMainhand().isStackable() && mc.player.getHeldItemMainhand().stackSize < threshold.getValue()) {
            int refillSpot = getRefillItemSpot(mc.player.getHeldItemMainhand().getItem());
            int itemSpot = getHotbarItemSpots(mc.player.getHeldItemMainhand());
            if (refillSpot == -1) return;
            if (itemSpot == -1) return;

            mc.playerController.windowClick(0, refillSpot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, itemSpot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(0, refillSpot, 0, ClickType.PICKUP, mc.player);
        }
    }

    private int getRefillItemSpot(Item item) {
        int slot = -1;

        for (int i = 9; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i) == ItemStack.EMPTY) return -1;
            Item currentCheckItem = mc.player.inventory.getStackInSlot(i).getItem();
            if (currentCheckItem == item) {
                slot = i;
                break;
            }
        }

        return slot;
    }

    private int getHotbarItemSpots(ItemStack itemStack) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i) == itemStack) return i;
        }

        return -1; //should never happen.
    }
}
