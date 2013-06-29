package org.coldswap.transformer;

import org.coldswap.util.ClassUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
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

/**
 * Searches the class before is loaded to find the
 * static initializer method(<clinit>). If it is not founded
 * insert a default one.
 */
public class ClInitTransformer implements ClassFileTransformer {
    private final static Logger logger = Logger.getLogger(ClInitTransformer.class.getName());

    static {
        logger.setLevel(ClassUtil.logLevel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
        if (s != null && !"".equals(s)) {
            for (String pack : ClassUtil.skipTransforming) {
                if (s.startsWith(pack)) {
                    return bytes;
                }
            }

            ClassNode cn = new ClassNode(Opcodes.ASM4);
            ClassReader cr = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            // create adapter for method insertion.
            cr.accept(cn, 0);
            // insert <clinit>V if it is not inserted
            List methods = cn.methods;
            boolean clInitFound = false;
            for (MethodNode methodNode : (List<MethodNode>) methods) {
                if ("<clinit>".equals(methodNode.name)) {
                    clInitFound = true;
                }
            }

            if (!clInitFound) {
                MethodNode mn = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                InsnList insnList = mn.instructions;
                insnList.add(new LabelNode());
                insnList.add(new InsnNode(Opcodes.RETURN));
                cn.methods.add(mn);
            }

            cn.accept(cw);
            byte[] toRet = cw.toByteArray();
            if (toRet != null) {
                logger.info("Successful transformation!");
                return toRet;
            } else {
                logger.severe("Could not transform class");
                return bytes;
            }
        }
        return bytes;
    }
}
