package com.thiakil.gottagofast;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

import java.io.File;

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

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

}
