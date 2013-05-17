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
 * Contributors:
 * faur
 * <p/>
 * Created at:
 * 8:08 PM       5/14/13
 */

import org.coldswap.asm.MemberReplacer;
import org.coldswap.util.AutoBoxing;
import org.coldswap.util.ByteCodeGenerator;
import org.coldswap.util.MethodUtil;
import org.coldswap.util.TransformerNameGenerator;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Searches for any new method that does not return
 * a void type, copy the code from the method to a helper method,
 * make some modification on the code and finally remove the new methods.
 */
public class PublicNotVoidMethodReplacer implements MemberReplacer {
    private static final Logger logger = Logger.getLogger(PublicNotVoidMethodReplacer.class.getName());
    private Class<?> aClass;
    private byte[] bytes;

    static {
        logger.setLevel(Level.ALL);
    }

    public PublicNotVoidMethodReplacer(Class<?> aClass, byte[] bytes) {
        this.aClass = aClass;
        this.bytes = bytes;
    }

    @Override
    public byte[] replace() {
        final ClassNode cn = new ClassNode(Opcodes.ASM4);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        cr.accept(cn, 0);
        // find first the helper method
        MethodNode helperMethod = findHelperMethodRef(cn.name, cn.methods);
        // get a list of public/protected/package/private methods but exclude
        // inherited methods.
        Method[] methods = aClass.getDeclaredMethods();
        List asmMethods = cn.methods;
        Iterator iterator = asmMethods.iterator();
        while (iterator.hasNext()) {
            final MethodNode mNode = (MethodNode) iterator.next();
            int publicAccessor = Opcodes.ACC_PUBLIC;
            // search only for public methods
            if ((mNode.access == publicAccessor)) {
                // check if this method exist in the old loaded class
                // and if not proceed with the trick.
                boolean foundIt = false;
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
                                    if (!mParamType.equals(fParamType)) {
                                        tmp = false;
                                    }
                                }
                            }
                            foundIt = tmp;
                        }
                    }
                }
                if (!foundIt) {
                    // autobox return type.
                    if (AutoBoxing.isPrimitive(mRetType.getDescriptor())) {
                        // replace any return reference with a boxing call and a areturn call
                        mNode.instructions = replaceReturn(mNode.instructions, mRetType);
                        // reload params from stack correctly, and make the unboxing
                        mNode.instructions = replaceLoadFromStack(mNode.instructions, mParamType);
                        // modify the helper method to mirror the new changes.
                        helperMethod.instructions = ByteCodeGenerator.insertNewNotVoidMethod(helperMethod,
                                mNode.instructions, mNode.name, mParamType.length);
                        // remove method
                        iterator.remove();
                    }
                }
            }
        }
        cn.accept(cw);
        return cw.toByteArray();
    }

    private InsnList replaceLoadFromStack(InsnList instructions, Type[] mParamType) {
        Iterator iter = instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
            int opcode = absIns.getOpcode();
            if ((opcode == Opcodes.AALOAD) || (opcode == Opcodes.ILOAD) || (opcode == Opcodes.LLOAD)
                    || (opcode == Opcodes.FLOAD) || (opcode == Opcodes.DLOAD)) {
                // find stack order number and increase by one.
                final int[] stackIndex = new int[1];
                absIns.accept(new MethodVisitor(Opcodes.ASM4) {
                    @Override
                    public void visitVarInsn(int i, int i2) {
                        stackIndex[0] = i2;
                        super.visitVarInsn(i, i2);
                    }
                });
                // replace this instruction with an unboxing and a aload

                InsnList iList = new InsnList();
                iList.add(AutoBoxing.unbox(mParamType[stackIndex[0] - 1]));
                iList.add(new VarInsnNode(Opcodes.AALOAD, stackIndex[0] + 1));
                instructions.insert(absIns, iList);
                // remove the old load
                iter.remove();
            }
        }
        return instructions;
    }

    private InsnList replaceReturn(InsnList insnList, Type retType) {
        int retOpcode = getRetOpcodeToReplace(retType);
        Iterator iter = insnList.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
            int opcode = absIns.getOpcode();
            if (opcode == retOpcode) {
                // replace this instruction with a boxing and a areturn
                InsnList iList = new InsnList();
                iList.add(AutoBoxing.box(retType));
                iList.add(new InsnNode(Opcodes.ARETURN));
                insnList.insert(absIns, iList);
                // remove the old return
                iter.remove();
            }
        }
        return insnList;
    }

    private int getRetOpcodeToReplace(Type retType) {
        String retDesc = retType.getDescriptor();
        int opcode = Opcodes.IRETURN;
        if ("Z".equals(retDesc)) {
            opcode = Opcodes.IRETURN;
        } else if ("B".equals(retDesc)) {
            opcode = Opcodes.IRETURN;
        } else if ("C".equals(retDesc)) {
            opcode = Opcodes.IRETURN;
        } else if ("S".equals(retDesc)) {
            opcode = Opcodes.IRETURN;
        } else if ("I".equals(retDesc)) {
            opcode = Opcodes.IRETURN;
        } else if ("J".equals(retDesc)) {
            opcode = Opcodes.LRETURN;
        } else if ("F".equals(retDesc)) {
            opcode = Opcodes.FRETURN;
        } else if ("D".equals(retDesc)) {
            opcode = Opcodes.DRETURN;
        }
        return opcode;
    }

    private MethodNode findHelperMethodRef(String className, List<MethodNode> methodNodes) {
        for (MethodNode methodNode : methodNodes) {
            if (methodNode.equals(TransformerNameGenerator.getObjectMethodName(className))) {
                return methodNode;
            }
        }
        return null;
    }
}
