package com.thiakil.gottagofast.coremod;

import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * Created by Thiakil on 16/11/2017.
 */
public class MCClassWriter extends ClassWriter {
    public MCClassWriter(int flags) {
        super(flags);
    }

    public MCClassWriter(ClassReader classReader, int flags) {
        super(classReader, flags);
    }

    protected String getCommonSuperClass(final String type1, final String type2) {
        Class<?> c, d;
        ClassLoader classLoader = Launch.classLoader;
        try {
            c = Class.forName(type1.replace('/', '.'), false, classLoader);
            d = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }
}
