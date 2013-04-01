package org.coldswap.transformer;

import org.coldswap.asm.ASMClassLoadTransformer;
import org.coldswap.asm.NewFSFieldTransformer;
import org.coldswap.util.TransformerNameGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
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
 * 7:43 PM       3/19/13
 */

public class ColdSwapTransformer implements ClassFileTransformer {
    private final static Logger logger = Logger.getLogger(ColdSwapTransformer.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    private final String[] skipTransforming = {
            "java/applet",
            "java/awt",
            "java/beans",
            "java/io",
            "java/lang",
            "java/lang",
            "java/math",
            "java/net",
            "java/nio",
            "java/rmi",
            "java/security",
            "java/sql",
            "java/text",
            "java/util",
            "javax/accessibility",
            "javax/activation",
            "javax/activity",
            "javax/annotation",
            "javax/crypto",
            "javax/imageio",
            "javax/jws",
            "javax/lang",
            "javax/management",
            "javax/naming",
            "javax/net",
            "javax/print",
            "javax/rmi",
            "javax/script",
            "javax/security",
            "javax/sound",
            "javax/sql",
            "javax/swing",
            "javax/tools",
            "javax/transaction",
            "javax/xml",
            "net/contentobjects",
            "org/ietf/jgss",
            "org/omg",
            "org/w3c",
            "org/xml/sax",
            "org/coldswap",
            "org/objectweb",
            "sun/util",
            "sun/misc",
            "sun/net",
            "sun/nio",
            "sun/text",
            "sun/reflect",
            "sun/security",
            "com/intellij"

    };

    @Override
    public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
        if (s != null && !"".equals(s)) {
            for (String pack : skipTransforming) {
                if (s.startsWith(pack)) {
                    return bytes;
                }
            }

            ClassNode cn = new ClassNode(Opcodes.ASM4);
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            // create adapter for field insertion.
            cr.accept(cn, 0);

            int acc = Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC;
            String name = TransformerNameGenerator.getPublicMethodName(s);
            ASMClassLoadTransformer tr = new NewFSFieldTransformer(s, acc, name, "Ljava/util/HashMap;", "Ljava/util/HashMap<Ljava/lang/String;Ljava/lang/Object;>;", null);
            int opcode = tr.transformClass(cn);
            cn.accept(cw);
            byte[] toRet = cw.toByteArray();

            if (toRet != null && opcode >= 0) {
                logger.info("Class <" + s + "> has been transformed!");
            } else {
                logger.severe("Field <" + name + "> could not been inserted!");
            }
            return toRet;
        }
        return bytes;
    }
}
