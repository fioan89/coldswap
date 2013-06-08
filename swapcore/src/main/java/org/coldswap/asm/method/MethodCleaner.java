package org.coldswap.asm.method;

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
 * Date: 6/8/13
 * Time: 7:30 PM
 */

import org.coldswap.asm.MemberReplacer;
import org.coldswap.util.MethodUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The scope of this class is to remove any method that was not inserted in the helper
 * method so that redefinition will be successful. Please note that code might break during runtime
 * because calls to the removed method will not be fixed.
 */
public class MethodCleaner implements MemberReplacer {
    private static final Logger logger = Logger.getLogger(MethodCleaner.class.getName());
    private int counter = 0;
    private Class<?> aClass;
    private byte[] bytes;

    static {
        logger.setLevel(Level.ALL);
    }

    public MethodCleaner(Class<?> aClass, byte[] bytes) {
        this.aClass = aClass;
        this.bytes = bytes;
    }

    @Override
    public byte[] replace() {
        final ClassNode cn = new ClassNode(Opcodes.ASM4);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(cn, 0);
        // get a list of public/protected/package/private methods but exclude
        // inherited methods.
        Method[] methods = aClass.getDeclaredMethods();
        List asmMethods = cn.methods;
        Iterator iterator = asmMethods.iterator();
        while (iterator.hasNext()) {
            final MethodNode mNode = (MethodNode) iterator.next();
            // exclude <init>() and <clinit>()
            if (!"<clinit>".equals(mNode.name) && !"<init>".equals(mNode.name)) {
                boolean foundIt = false;
                int publicAccessor = Opcodes.ACC_PUBLIC;
                // search only for public methods
                if ((mNode.access == publicAccessor)) {
                    // check if this method exist in the old loaded class
                    // and if not proceed with the trick.
                    Type mRetType = MethodUtil.getReturnType(mNode);
                    Type[] mParamType = MethodUtil.getParamsType(mNode);
                    for (Method method : methods) {
                        if (mNode.name.equals(method.getName())) {
                            Type fRetType = Type.getType(method).getReturnType();
                            if (mRetType.equals(fRetType)) {
                                Type[] fParamType = MethodUtil.classToType(method.getParameterTypes());
                                boolean tmp = true;
                                if (mParamType.length == fParamType.length) {
                                    for (int i = 0; i < mParamType.length; i++) {
                                        if (!mParamType[i].equals(fParamType[i])) {
                                            tmp = false;
                                        }
                                    }
                                }
                                foundIt = tmp;
                            }
                        }
                    }
                    if (!foundIt) {
                        // remove
                        counter++;
                        iterator.remove();
                    }
                }
            }
        }
        // note user about removed methods.
        if (counter > 0) {
            logger.severe(counter + " new methods found and removed. Please consider stopping the application because" +
                    " there might be calls to this methods that would break application state.");
        }
        cn.accept(cw);
        return cw.toByteArray();
    }
}
