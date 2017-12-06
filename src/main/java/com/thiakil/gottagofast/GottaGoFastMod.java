/*
 * This file is part of ConcreteFactories. Copyright 2017 Thiakil
 *
 * ConcreteFactories is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ConcreteFactories is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ConcreteFactories.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.thiakil.gottagofast;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/*@Mod(
        modid = GottaGoFastMod.MOD_ID,
        name = GottaGoFastMod.MOD_NAME,
        version = GottaGoFastMod.VERSION
)*/
public class GottaGoFastMod extends DummyModContainer {

    public static final String MOD_ID = "gottagofast";
    public static final String MOD_NAME = "Gotta Go Fast";
    public static final String VERSION = "1.0";

    //values are placeholders, actual defaults in `get` calls
    public static float MAX_PLAYER_SPEED = Float.MAX_VALUE;
    public static float MAX_PLAYER_ELYTRA_SPEED = Float.MAX_VALUE;
    public static double MAX_PLAYER_VEHICLE_SPEED = 100.0F;

    public static Logger logger = LogManager.getLogger("Gotta Go Fast");

    public static File myModFile;

    //private static NetHandlerPlayServer npsh = new NetHandlerPlayServer(null, null, null);

    public GottaGoFastMod(){
        super(loadMcmodInfo());
    }

    //public static GottaGoFastMod INSTANCE = new GottaGoFastMod();

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void handleModStateEvent(FMLEvent event){
        if (event instanceof FMLConstructionEvent){
            List<String> certs = CertificateHelper.getFingerprints(getClass().getProtectionDomain().getCodeSource().getCertificates());
            boolean foundCert = false;
            String myFinger = "@FINGERPRINT@".toLowerCase().replaceAll(":", "");
            for (String c : certs){
                if (myFinger.equals(c)){
                    foundCert = true;
                    break;
                }
            }
            if (!foundCert){
                logger.error("Warning: did not find the signature I was expecting!");
                logger.info("Available fingerprints: ");
                certs.forEach(c->logger.info(c));
            }
        }
        if (event instanceof FMLPreInitializationEvent){
            Configuration config = new Configuration(new File(Loader.instance().getConfigDir(), "gottagofast.cfg"));
            MAX_PLAYER_SPEED = config.getFloat("max_player_speed", "general", Float.MAX_VALUE, 1F, Float.MAX_VALUE, "Maximum player movement speed (x^2 + y^2 + z^2) before triggering the moved too quickly messages. Vanilla is 100.0");
            MAX_PLAYER_ELYTRA_SPEED = config.getFloat("max_player_elytra_speed", "general", Float.MAX_VALUE, 1F, Float.MAX_VALUE, "Maximum player movement speed (x^2 + y^2 + z^2) before triggering the moved too quickly messages when Elytra flying. Vanilla is 300.0");
            MAX_PLAYER_VEHICLE_SPEED = config.getFloat("max_player_vehicle_speed", "general", 100.0F, 1F, Float.MAX_VALUE, "Maximum player vehicle (ridden entity) movement speed (x^2 + y^2 + z^2) before triggering the moved too quickly messages. Vanilla is 100.0");
            if (config.hasChanged()){
                config.save();
            }
        }
    }

    private static ModMetadata loadMcmodInfo(){
        InputStream is = null;
        if (myModFile != null) {
            try {
                if (myModFile.isDirectory()){
                    //this will probably never be reached, oh well
                    is = new FileInputStream(new File(myModFile, "mcmod.info"));
                } else {
                    JarFile myjar = new JarFile(myModFile);
                    is = myjar.getInputStream(myjar.getJarEntry("mcmod.info"));
                }
            } catch (Exception e) {
                GottaGoFastMod.logger.error("Could not load mcmod.info", e);
            }
        }
        Map<String, Object> dummyMeta = ImmutableMap.<String, Object>builder().put("name", MOD_NAME).put("version", VERSION).build();
        return MetadataCollection.from(is, MOD_ID).getMetadataForId(MOD_ID, dummyMeta);
    }

    @Override
    public File getSource() {
        return myModFile;
    }

    @Override
    public Class<?> getCustomResourcePackClass()
    {
        if (myModFile == null){
            return null;
        }
        try
        {
            return getSource().isDirectory() ? Class.forName("net.minecraftforge.fml.client.FMLFolderResourcePack", true, getClass().getClassLoader()) : Class.forName("net.minecraftforge.fml.client.FMLFileResourcePack", true, getClass().getClassLoader());
        }
        catch (ClassNotFoundException e)
        {
            return null;
        }
    }

}
