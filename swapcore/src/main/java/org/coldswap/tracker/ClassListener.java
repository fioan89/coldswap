package org.coldswap.tracker;


import net.contentobjects.jnotify.JNotifyListener;
import org.coldswap.instrumentation.ClassInstrumenter;
import org.coldswap.transformer.ClassRedefiner;
import org.coldswap.util.BytecodeClassLoader;

import java.io.File;
import java.io.FileFilter;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Map;
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
 * 5:53 PM       3/17/13
 */

/**
 * Listen for any class file modifications and instrument
 * JVM class loader to reload any modification that has been
 * made to the byte code.
 */
public class ClassListener implements JNotifyListener {
    private final static String sep = System.getProperty("file.separator");
    private final static Logger logger = Logger.getLogger(ClassListener.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    private final FileFilter filter = new ClassFileFilter();

    @Override
    public void fileCreated(int i, String s, String s2) {
//        if (filter.accept(new File(s2))) {
//            System.out.println("New File Created:");
//            System.out.println("watchID:" + String.valueOf(i));
//            System.out.println("RootPath:" + s);
//            System.out.println("Name:" + s2);
//        }
    }

    @Override
    public void fileDeleted(int i, String s, String s2) {
//        if (filter.accept(new File(s2))) {
//            System.out.println("File Deleted:");
//            System.out.println("watchID:" + String.valueOf(i));
//            System.out.println("RootPath:" + s);
//            System.out.println("Name:" + s2);
//        }
    }

    @Override
    public void fileModified(int i, String root, String className) {
        System.out.println("File modified");
        if ((!"".equals(className)) && (null != className)) {
            if (filter.accept(new File(className))) {
                Map<String, Class<?>> loaded = ClassInstrumenter.getInstance().getLoadedClasses();
                String cls = className.replace(".class", "").replace(System.getProperty("file.separator"), ".");
                // maybe this className is located in one of  root subtrees, therefore whee need to remove the
                // subtree from the className
                int dotPosition = cls.lastIndexOf(".");
                String clsName = cls;
                if (dotPosition > -1) {
                    clsName = cls.substring(dotPosition + 1);
                }
                if ((!"".equals(clsName)) && (null != clsName)) {
                    Class<?> clazz = loaded.get(clsName);
                    System.out.println("Cls:" + cls);
                    System.out.println("Class Name:" + clsName);
                    if (clazz == null) {
                        logger.info(className + " is new class file!");
                        try {
                            Class<?> cla = Class.forName(clsName);
                            ClassDefinition def = new ClassDefinition(cla, BytecodeClassLoader.loadClassBytes(root + this.sep + className));
                            Instrumentation inst = ClassInstrumenter.getInstance().getInstrumenter();
                            inst.redefineClasses(new ClassDefinition[]{def});
                        } catch (ClassNotFoundException e) {
                            logger.warning(e.toString());
                        } catch (UnmodifiableClassException e) {
                            logger.warning(toString());
                        }

                        return;
                    }
                    System.out.println("Clazz:" + clazz.getName());
                    System.out.println("logger:" + logger.getName());
                    logger.info("From memory:" + clazz.getName());
                    if (clazz != null) {
                        ClassRedefiner redefiner = new ClassRedefiner(clsName, root + this.sep + className);
                        try {
                            redefiner.redefineClass(clazz);
                        } catch (UnmodifiableClassException e) {
                            logger.severe(e.toString());
                        } catch (ClassNotFoundException e) {
                            logger.severe(e.toString());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fileRenamed(int i, String s, String s2, String s3) {
        if (filter.accept(new File(s3))) {
            System.out.println("File Renamed:");
            System.out.println("watchID:" + String.valueOf(i));
            System.out.println("RootPath:" + s);
            System.out.println("Old Name:" + s2);
            System.out.println("New Name:" + s2);
        }
    }
}
