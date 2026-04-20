package XiGyoku.furyborn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import java.util.HashMap;
import java.util.Map;

public class FuryBornRarities {
    public static final Rarity STARRY = Rarity.create("STARRY", ChatFormatting.WHITE);
    public static final Rarity RED_STARRY = Rarity.create("RED_STARRY", ChatFormatting.RED);

    private static final Map<Rarity, Integer> CUSTOM_RARITY_COLORS = new HashMap<>();

    static {
        CUSTOM_RARITY_COLORS.put(STARRY, 0x5AFF19);
        CUSTOM_RARITY_COLORS.put(RED_STARRY, 0xFF0000);
    }

    public static int getColor(Rarity rarity) {
        if (CUSTOM_RARITY_COLORS.containsKey(rarity)) {
            return CUSTOM_RARITY_COLORS.get(rarity);
        }
        if (rarity.color != null && rarity.color.getColor() != null) {
            return rarity.color.getColor();
        }
        return 0xFFFFFF;
    }

    public static boolean hasCustomColor(Rarity rarity) {
        return CUSTOM_RARITY_COLORS.containsKey(rarity);
    }
}