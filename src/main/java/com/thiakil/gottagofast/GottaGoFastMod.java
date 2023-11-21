package com.thiakil.gottagofast;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(GottaGoFastMod.MOD_ID)
public class GottaGoFastMod {

    public static final String MOD_ID = "gottagofast";

    //values are placeholders, actual defaults in Config
    public static float MAX_PLAYER_SPEED = Float.MAX_VALUE;
    public static float MAX_PLAYER_ELYTRA_SPEED = Float.MAX_VALUE;
    public static double MAX_PLAYER_VEHICLE_SPEED = 100.0F;

    public static Logger logger = LogUtils.getLogger();

    public GottaGoFastMod(){
        //IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register our mod's ConfigSpec so that Neo can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

}
