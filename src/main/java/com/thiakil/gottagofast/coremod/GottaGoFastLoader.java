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

package com.thiakil.gottagofast.coremod;

import com.thiakil.gottagofast.GottaGoFastMod;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
public class GottaGoFastLoader implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"com.thiakil.gottagofast.coremod.GottaGoFastASMTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return "com.thiakil.gottagofast.GottaGoFastMod";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        GottaGoFastMod.myModFile = (File)data.get("coremodLocation");
        if (GottaGoFastMod.myModFile == null){//find the classes root by the main class
            URL modclassLoc = getClass().getResource("/"+GottaGoFastMod.class.getName().replaceAll("\\.", "/")+".class");
            String uri = modclassLoc.toString();
            if (uri.startsWith("file:")){
                String pkg = GottaGoFastMod.class.getPackage().getName();
                uri = uri.substring(0, uri.indexOf(pkg.replaceAll("\\.", "/")));try {
                    String fileName = URLDecoder.decode(uri.substring("file:".length()), Charset.defaultCharset().name());
                    GottaGoFastMod.myModFile = new File(fileName);
                } catch (UnsupportedEncodingException e) {
                    throw new InternalError("default charset doesn't exist. Your VM is borked.");
                }
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
