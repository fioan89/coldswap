package org.coldswap.util;

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
 * 3:47 PM       5/5/13
 */

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Generates byte code for new classes.
 */
public class ByteCodeGenerator {

    /**
     * Creates a new class containing the new static field.
     *
     * @param classNode        containing the old class.
     * @param fieldNode        containing the old field.
     * @param initInstructions a list of instructions that goes into <clinit>.
     * @param className        the name of the new class to be generated.
     * @return an array of bytes which builds the new class.
     */
    @SuppressWarnings("unchecked")
    public static byte[] newFieldClass(ClassNode classNode, FieldNode fieldNode, InsnList initInstructions, String className) {
        ClassNode newClass = new ClassNode();
        newClass.version = classNode.version;
        newClass.access = Opcodes.ACC_PUBLIC;
        newClass.signature = "L" + className + ";";
        newClass.name = className;
        newClass.superName = "java/lang/Object";
        newClass.fields.add(new FieldNode(fieldNode.access, fieldNode.name, fieldNode.desc, fieldNode.desc, fieldNode.value));
        if (initInstructions != null) {
            if (initInstructions.size() > 0) {
                MethodNode mn = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                InsnList il = mn.instructions;
                il.add(new LabelNode());
                il.add(initInstructions);
                il.add(new FieldInsnNode(Opcodes.PUTSTATIC, className, fieldNode.name, fieldNode.desc));
                il.add(new InsnNode(Opcodes.RETURN));
                newClass.methods.add(mn);
            }
        }

        ClassWriter newCWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        newClass.accept(newCWriter);
        return newCWriter.toByteArray();
    }

    /**
     * Inserts in the helper method a list of instructions
     *
     * @param mNode      helper method
     * @param toInsert   code to be inserted.
     * @param methodName name of the method that contains the to be inserted code.
     * @param nrOfParams the number of params that method to be inserted has.
     * @return a list of instructions.
     */
    public static InsnList insertNewNotVoidMethod(MethodNode mNode, InsnList toInsert, String methodName, int nrOfParams) {
        InsnList into = mNode.instructions;
        AbstractInsnNode firstInst = into.getFirst();
        InsnList tmp = new InsnList();
        tmp.add(new LabelNode());
        tmp.add(new LdcInsnNode(methodName));
        tmp.add(new VarInsnNode(Opcodes.AALOAD, 1));
        tmp.add(new MethodInsnNode(Opcodes.H_INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z"));
        LabelNode l1 = new LabelNode();
        tmp.add(l1);
        tmp.add(new JumpInsnNode(Opcodes.IFEQ, l1));
        tmp.add(new VarInsnNode(Opcodes.AALOAD, 2));
        tmp.add(new InsnNode(Opcodes.ARRAYLENGTH));
        if (nrOfParams <= 5) {
            switch (nrOfParams) {
                case 0: {
                    tmp.add(new InsnNode(Opcodes.ICONST_0));
                    break;
                }

                case 1: {
                    tmp.add(new InsnNode(Opcodes.ICONST_1));
                    break;
                }

                case 2: {
                    tmp.add(new InsnNode(Opcodes.ICONST_2));
                    break;
                }

                case 3: {
                    tmp.add(new InsnNode(Opcodes.ICONST_3));
                    break;
                }

                case 4: {
                    tmp.add(new InsnNode(Opcodes.ICONST_4));
                    break;
                }

                case 5: {
                    tmp.add(new InsnNode(Opcodes.ICONST_5));
                    break;
                }
                default:
                    break;
            }

        } else if (nrOfParams <= Constants.BIPUSH_MAX) {
            tmp.add(new VarInsnNode(Opcodes.BIPUSH, nrOfParams));
        } else if (nrOfParams <= Constants.SIPUSH_MAX) {
            tmp.add(new VarInsnNode(Opcodes.SIPUSH, nrOfParams));
        }
        tmp.add(new JumpInsnNode(Opcodes.IF_ICMPNE, l1));
        tmp.add(toInsert);
        into.insertBefore(firstInst, tmp);
        return into;
    }
}
