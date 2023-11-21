package com.thiakil.gottagofast;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = GottaGoFastMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public static final ModConfigSpec.DoubleValue MAX_PLAYER_SPEED;
    public static final ModConfigSpec.DoubleValue MAX_PLAYER_ELYTRA_SPEED;
    public static final ModConfigSpec.DoubleValue MAX_PLAYER_VEHICLE_SPEED;

    static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        
        MAX_PLAYER_SPEED = BUILDER
                .comment("Maximum player movement speed (x^2 + y^2 + z^2) before triggering the moved too quickly messages. Vanilla is 100.0")
                .defineInRange("max_player_speed", 1000, 1F, Float.MAX_VALUE);

        MAX_PLAYER_ELYTRA_SPEED = BUILDER
                .comment("Maximum player movement speed (x^2 + y^2 + z^2) before triggering the moved too quickly messages when Elytra flying. Vanilla is 300.0")
                .defineInRange("max_player_elytra_speed", 3000, 1F, Float.MAX_VALUE);

        MAX_PLAYER_VEHICLE_SPEED = BUILDER
                .comment("Maximum player vehicle (ridden entity) movement speed (x^2 + y^2 + z^2) before triggering the moved too quickly messages. Vanilla is 100.0")
                .defineInRange("max_player_vehicle_speed", 100.0F, 1F, Float.MAX_VALUE);

        SPEC = BUILDER.build();
    }


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        GottaGoFastMod.MAX_PLAYER_SPEED = MAX_PLAYER_SPEED.get().floatValue();
        GottaGoFastMod.MAX_PLAYER_ELYTRA_SPEED = MAX_PLAYER_ELYTRA_SPEED.get().floatValue();
        GottaGoFastMod.MAX_PLAYER_VEHICLE_SPEED = MAX_PLAYER_VEHICLE_SPEED.get();
    }
}