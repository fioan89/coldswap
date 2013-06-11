package org.coldswap.asm;


import org.coldswap.util.AutoBoxing;
import org.coldswap.util.Constants;
import org.coldswap.util.TransformerNameGenerator;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

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
 * Date: 6/6/13
 * Time: 9:35 PM
 */
public class VirtualMethodReplacer extends MethodBox {
    private final int methodNumber;
    private final String methodType;

    /**
     * Constructs an object that will contain basic information about a
     * method whose call will be replaced.
     *
     * @param classContainer the name of the class containig the method
     *                       whose invoke should be replaced.
     * @param methodName     the name of the method whose invoke should be replaced.
     * @param retType        return {@link org.objectweb.asm.Type} of the method
     * @param paramType      parameters {@link org.objectweb.asm.Type}.
     * @param methodType     what kind of method should be replaced:"Object[]","int"
     * @param counter        a counter for method name generation.
     */
    public VirtualMethodReplacer(String classContainer, String methodName, Type retType, Type[] paramType, String methodType, int counter) {
        super(classContainer, methodName, retType, paramType);
        this.methodNumber = counter;
        this.methodType = methodType;
    }

    @Override
    public MethodNode replaceInvoke(MethodNode methodNode) {
        InsnList instructions = methodNode.instructions;
        Iterator it = instructions.iterator();
        while (it.hasNext()) {
            AbstractInsnNode code = (AbstractInsnNode) it.next();
            if (code.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                // check if methodToReplace is called
                final boolean[] callFounded = new boolean[]{false};
                code.accept(new MethodVisitor(Opcodes.ASM4) {
                    @Override
                    public void visitMethodInsn(int i, String s, String s2, String s3) {
                        if (s.equals(classContainer) && s2.equals(methodName)) {
                            callFounded[0] = true;
                        }
                        super.visitMethodInsn(i, s, s2, s3);
                    }
                });

                if (callFounded[0]) {
                    // if the return type is primitive and the value is not discarded, unbox
                    if (AutoBoxing.isPrimitive(retType.getDescriptor())) {
                        AbstractInsnNode codeNext = code.getNext();
                        boolean discarded = false;
                        // if returning primitive double or long and it is discarded with a pop2 than discard with
                        // simple pop, becuase we use an Object as return value.
                        if (codeNext.getOpcode() == Opcodes.POP2 && (retType.getDescriptor().equals("D")
                                || retType.getDescriptor().equals("J"))) {
                            instructions.set(codeNext, new InsnNode(Opcodes.POP));

                        }
                        if (codeNext.getOpcode() == Opcodes.POP || codeNext.getOpcode() == Opcodes.POP2) {
                            discarded = true;
                        }
                        if (!discarded) {
                            instructions.insert(code, AutoBoxing.unbox(retType));
                        }
                    }

                    // replace call with a custom call
                    String newMethodName;
                    AbstractInsnNode newInvoke = null;
                    if (Constants.VAROBJECT.equals(methodType)) {
                        newMethodName = TransformerNameGenerator.getObjectMethodNameWithCounter(classContainer, methodNumber);
                        newInvoke = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classContainer, newMethodName, "([Ljava/lang/Object;)Ljava/lang/Object;");
                    } else if (Constants.INT.equals(methodType)) {
                        newMethodName = TransformerNameGenerator.getIntMethodNameWithCounter(classContainer, methodNumber);
                        newInvoke = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classContainer, newMethodName, "(I)Ljava/lang/Object;");
                    } else if (Constants.FLOAT.equals(methodType)) {
                        newMethodName = TransformerNameGenerator.getFloatMethodNameWithCounter(classContainer, methodNumber);
                        newInvoke = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classContainer, newMethodName, "(F)Ljava/lang/Object;");
                    } else if (Constants.STRING.equals(methodType)) {
                        newMethodName = TransformerNameGenerator.getStringMethodNameWithCounter(classContainer, methodNumber);
                        newInvoke = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classContainer, newMethodName, "(Ljava/lang/String;)Ljava/lang/Object;");
                    }
                    if (newInvoke != null) {
                        instructions.set(code, newInvoke);
                    }
                }
            }
        }
        return methodNode;
    }
}
