package org.coldswap.transformer;

import org.coldswap.util.ClassUtil;
import org.coldswap.util.MethodUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
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
 * 7:38 PM       5/14/13
 */

/**
 * Insert a new method that returns an {@link Object}. In this method
 * will be appended any new method that does not return void and that appears
 * when a class is redefined.
 */
public class NewObjMethodTransformer implements ClassFileTransformer {
    private static final Logger logger = Logger.getLogger(NewObjMethodTransformer.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className != null && !"".equals(className)) {
            for (String pack : ClassUtil.skipTransforming) {
                if (className.startsWith(pack)) {
                    return classfileBuffer;
                }
            }

            ClassNode cn = new ClassNode(Opcodes.ASM4);
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            // create adapter for method insertion.
            cr.accept(cn, 0);
            // insert <clinit>V if it is not inserted
            List methods = cn.methods;
            MethodNode mn = MethodUtil.createHelperMethod(className);
            cn.methods.add(mn);
            cn.accept(cw);
            byte[] toRet = cw.toByteArray();
            if (toRet != null) {
                logger.info("Successful transformation!");
                return toRet;
            } else {
                logger.severe("Could not transform class");
                return classfileBuffer;
            }
        }
        return classfileBuffer;
    }
}
