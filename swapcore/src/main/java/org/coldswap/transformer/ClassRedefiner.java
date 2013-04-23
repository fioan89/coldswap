package org.coldswap.transformer;

import org.coldswap.asm.PublicStaticFieldReplacer;
import org.coldswap.instrumentation.ClassInstrumenter;
import org.coldswap.util.BytecodeClassLoader;

import java.io.*;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * (C) Copyright 2013 Faur Ioan-Aurel.
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * Contributors:
 * faur
 * <p/>
 * Created at:
 * 9:05 PM       3/20/13
 */

/**
 * Redefine methods who's body has changed during runtime.
 */
public class ClassRedefiner {
    private String classFile;
    private String clsName;
    private String path;
    private String sep = System.getProperty("file.separator");
    private static final Logger logger = Logger.getLogger(ClassRedefiner.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Constructs a Class method redefiner.
     *
     * @param className Name.class of a modified file.
     * @param classPath Class residing directory.
     */
    public ClassRedefiner(String className, String classPath) {
        this.clsName = className.replace(".class", "").replace(System.getProperty("file.separator"), ".");
        this.path = classPath;
    }

    /**
     * Reloads any modified method body.
     *
     * @param clazz the already existing body of a class.
     * @throws UnmodifiableClassException
     * @throws ClassNotFoundException
     */
    public void redefineClass(Class<?> clazz) throws UnmodifiableClassException, ClassNotFoundException {
        byte[] classBytes = BytecodeClassLoader.loadClassBytes(this.path);
        PublicStaticFieldReplacer rep = new PublicStaticFieldReplacer(clazz, classBytes);
        classBytes = rep.replace();
        ClassDefinition cls = new ClassDefinition(clazz, classBytes);
        Instrumentation inst = ClassInstrumenter.getInstance().getInstrumenter();
        inst.redefineClasses(new ClassDefinition[]{cls});
        logger.info("Class " + this.clsName + " was  redefined!");
    }

    /**
     * Loads the byte file of a class file. This class name is
     * specified in the constructor.
     *
     * @return
     */
    private byte[] loadClassBytes() {
        InputStream in = null;
        ByteArrayOutputStream bao = null;
        byte[] ret = null;
        try {
            logger.info("Reading bytes from:" + this.path);
            in = new FileInputStream(new File(this.path));
            bao = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int counter = 0;
            while ((counter = in.read(buffer)) != -1) {
                bao.write(buffer, 0, counter);
            }

        } catch (FileNotFoundException e) {
            logger.severe(e.toString());
        } catch (IOException e) {
            logger.severe(e.toString());
        } finally {
            if (bao != null) {
                ret = bao.toByteArray();
            }
            try {
                in.close();
                bao.close();
            } catch (IOException e) {
                // ignore
            }

        }
        logger.info("Going to return:" + String.valueOf(ret.length));
        return ret;
    }
}
