package com.gamesense.client.module.modules.combat;

import com.gamesense.api.settings.Setting;
import com.gamesense.client.module.Module;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Arrays;

/**
 * Author Seth 4/30/2019 @ 3:37 AM.
 * Updates by SalHack 2-1-20 (Ionar)
 */
public final class AutoOffhand extends Module {
    public final Setting.Double health = registerDouble("Health", "HP", 16.0f, 0.0f, 36.0f);
    public final Setting.Mode Mode = registerMode("Mode", "Mode", Arrays.asList(
            "Totem",
            "Gap",
            "Crystal",
            "Pearl",
            "Chorus",
            "Strength"
    ),"Totem");
    public final Setting.Mode FallbackMode = registerMode("Fallback", "FallbackMode", Arrays.asList(
            "Totem",
            "Gap",
            "Crystal",
            "Pearl",
            "Chorus",
            "Strength"
    ),"Crystal");
    public final Setting.Double FallDistance = registerDouble("FallDistance", "Fall", 15.0f, 0.0f, 100.0f);
    public final Setting.Boolean TotemOnElytra = registerBoolean("TotemOnElytra", "Elytra", true);
    public final Setting.Boolean OffhandGapOnSword = registerBoolean("SwordGap", "SwordGap", false);
    public final Setting.Boolean OffhandStrNoStrSword = registerBoolean("StrGap", "Strength", false);
    public final Setting.Boolean HotbarFirst = registerBoolean("HotbarFirst", "Recursive", false);
    public final Setting.Boolean stopInGUI = registerBoolean("StopInContainers", "stopInGUI", false);

    public AutoOffhand() {
        super("AutoOffhand", Category.Combat);
    }

    private void SwitchOffHandIfNeed(String p_Val)
    {
        Item l_Item = GetItemFromModeVal(p_Val);

        if (mc.player.getHeldItemOffhand().getItem() != l_Item)
        {
            int l_Slot = HotbarFirst.getValue() ? GetRecursiveItemSlot(l_Item) : GetItemSlot(l_Item);

            Item l_Fallback = GetItemFromModeVal(FallbackMode.getValue());

            String l_Display = GetItemNameFromModeVal(p_Val);

            if (l_Slot == -1 && l_Item != l_Fallback && mc.player.getHeldItemOffhand().getItem() != l_Fallback)
            {
                l_Slot = GetRecursiveItemSlot(l_Fallback);
                l_Display = GetItemNameFromModeVal(FallbackMode.getValue());

                /// still -1...
                if (l_Slot == -1 && l_Fallback != Items.TOTEM_OF_UNDYING)
                {
                    l_Fallback = Items.TOTEM_OF_UNDYING;

                    if (l_Item != l_Fallback && mc.player.getHeldItemOffhand().getItem() != l_Fallback)
                    {
                        l_Slot = GetRecursiveItemSlot(l_Fallback);
                        l_Display = "Emergency Totem";
                    }
                }
            }

            if (l_Slot != -1)
            {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                        ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP,
                        mc.player);

                /// @todo: this might cause desyncs, we need a callback for windowclicks for transaction complete packet.
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_Slot, 0,
                        ClickType.PICKUP, mc.player);
                mc.playerController.updateController();
            }
        }
    }

    @Override
    public void onUpdate() {
        if (stopInGUI.getValue()) {
            if (mc.currentScreen != null && (!(mc.currentScreen instanceof GuiInventory)))
            return;
        }

        if (!mc.player.getHeldItemMainhand().isEmpty())
        {
            if (health.getValue() <= GetHealthWithAbsorption() && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && OffhandStrNoStrSword.getValue() && !mc.player.isPotionActive(MobEffects.STRENGTH))
            {
                SwitchOffHandIfNeed("Strength");
                return;
            }

            /// Sword override
            if (health.getValue() <= GetHealthWithAbsorption() && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && OffhandGapOnSword.getValue())
            {
                SwitchOffHandIfNeed("Gap");
                return;
            }
        }

        /// First check health, most important as we don't want to die for no reason.
        if (health.getValue() > GetHealthWithAbsorption() || Mode.getValue() == "Totem" || (TotemOnElytra.getValue() && mc.player.isElytraFlying()) || (mc.player.fallDistance >= FallDistance.getValue() && !mc.player.isElytraFlying()))
        {
            SwitchOffHandIfNeed("Totem");
            return;
        }

        /// If we meet the required health
        SwitchOffHandIfNeed(Mode.getValue());
    }

    public static float GetHealthWithAbsorption() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }
    public static int GetItemSlot(Item input) {
        if (mc.player == null)
            return 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == input)
            {
                return i;
            }
        }
        return -1;
    }
    public static int GetRecursiveItemSlot(Item input) {
        if (mc.player == null)
            return 0;

        for (int i = mc.player.inventoryContainer.getInventory().size() - 1; i > 0; --i)
        {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == input)
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable()
    {
        super.onEnable();
    }

    public Item GetItemFromModeVal(String p_Val)
    {
        switch (p_Val)
        {
            case "Crystal":
                return Items.END_CRYSTAL;
            case "Gap":
                return Items.GOLDEN_APPLE;
            case "Pearl":
                return Items.ENDER_PEARL;
            case "Chorus":
                return Items.CHORUS_FRUIT;
            case "Strength":
                return Items.POTIONITEM;
            default:
                break;
        }

        return Items.TOTEM_OF_UNDYING;
    }

    private String GetItemNameFromModeVal(String p_Val)
    {
        switch (p_Val)
        {
            case "Crystal":
                return "End Crystal";
            case "Gap":
                return "Gap";
            case "Pearl":
                return "Pearl";
            case "Chorus":
                return "Chorus";
            case "Strength":
                return "Strength";
            default:
                break;
        }

        return "Totem";
    }
}