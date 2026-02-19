package ru.agniaendie.mlwborderlinker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MlwBorderLinker.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue X_LIMIT = BUILDER
            .comment("X coord limit of world")
            .defineInRange("x_limit", 8128, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue Z_LIMIT = BUILDER
            .comment("Z coord limit of world")
            .defineInRange("z_limit", 8128, 0, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int xCoord;
    public static int zCoord;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        xCoord = X_LIMIT.get();
        zCoord = Z_LIMIT.get();
    }
}
