package org.coldswap.transformer;

import org.coldswap.util.ClassUtil;
import org.coldswap.util.MethodUtil;
import org.coldswap.util.TransformerNameGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
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
 * Created with IntelliJ IDEA.
 * User: faur
 * Date: 6/11/13
 * Time: 6:35 PM
 */

/**
 * Inserts helper methods for new methods that have this definition:
 * public <any return type> <any method name>(String <any arg name>)
 */
public class StringMethodTransformer implements ClassFileTransformer {
    private final int maxNumberOfMethods;
    private static final Logger logger = Logger.getLogger(StringMethodTransformer.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    public StringMethodTransformer(int maxNumberOfMethods) {
        this.maxNumberOfMethods = maxNumberOfMethods;
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
            // create a list of method name's so that we resolve method collision
            List<MethodNode> methodNodes = cn.methods;
            Iterator it = methodNodes.iterator();
            List<String> methodNames = new ArrayList<String>(methodNodes.size());
            while (it.hasNext()) {
                MethodNode methodNode = (MethodNode) it.next();
                methodNames.add(methodNode.name);
            }
            // insert helper methods if they do not exist.
            for (int i = 0; i < maxNumberOfMethods; i++) {
                String name = TransformerNameGenerator.getStringMethodNameWithCounter(className, i);
                if (!methodNames.contains(name)) {
                    MethodNode mn = MethodUtil.createStringHelperMethod(className, i);
                    cn.methods.add(mn);
                }
            }
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
