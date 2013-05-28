package org.coldswap.asm;

import org.coldswap.util.AutoBoxing;
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
 * Date: 5/28/13
 * Time: 5:29 PM
 */

/**
 * Replace any reference to a method who's return type is not void, and
 * it's paramaters is only an Object[].
 */
public class NVInvokeVirtualMethod extends MethodBox {

    /**
     * Constructs an object that will contain basic information about a
     * method whose call will be replaced.
     *
     * @param classContainer the name of the class containig the method
     *                       whose invoke should be replaced.
     * @param methodName     the name of the method whose invoke should be replaced.
     * @param retType        return {@link org.objectweb.asm.Type} of the method
     * @param paramType      parameters {@link org.objectweb.asm.Type}.
     */
    public NVInvokeVirtualMethod(String classContainer, String methodName, Type retType, Type[] paramType) {
        super(classContainer, methodName, retType, paramType);
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
                    // if the return type is primitive, unbox
                    if (AutoBoxing.isPrimitive(retType.getDescriptor())) {
                        instructions.insert(code, AutoBoxing.unbox(retType));
                    }
                    // replace call with a custom call
                    String newMethodName = TransformerNameGenerator.getObjectMethodName(classContainer);
                    AbstractInsnNode newInvoke = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classContainer, newMethodName, "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
                    instructions.set(code, newInvoke);

                    // insert the first param to be the name of the method who will be replaced
                    int index = findFirstLabelBeforeInvoke(instructions, code);
                    // check if next is a  aload 1
                    AbstractInsnNode tmp = instructions.get(index + 1);
                    final boolean[] hasBeenFound = new boolean[]{false};
                    if (tmp.getOpcode() == Opcodes.ALOAD) {
                        tmp.accept(new MethodVisitor(Opcodes.ASM4) {
                            @Override
                            public void visitVarInsn(int i, int i2) {
                                if (i2 == 1) {
                                    hasBeenFound[0] = true;
                                }
                                super.visitVarInsn(i, i2);
                            }
                        });
                    }
                    if (hasBeenFound[0]) {
                        instructions.insert(tmp, new LdcInsnNode(methodName));
                    }

                }
            }
        }
        return methodNode;
    }

    private int findFirstLabelBeforeInvoke(InsnList insnList, AbstractInsnNode code) {
        int index = insnList.indexOf(code);
        AbstractInsnNode tmp;
        while ((tmp = code.getPrevious()) != null) {
            if (tmp.getOpcode() == -1) {
                index = insnList.indexOf(tmp);
            }
            code = tmp;
        }
        return index;
    }
}
