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
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates byte code for new classes.
 */
public class ByteCodeGenerator {
    private static final Map<LabelNode, LabelNode> labelNodes = new HashMap<LabelNode, LabelNode>();
    private static final Map<Label, Label> labels = new HashMap<Label, Label>();
    private static final Map<Integer, LabelNode> offset = new HashMap<Integer, LabelNode>();
    private static final Map<Label, LabelNode> offsetL = new HashMap<Label, LabelNode>();
    private static LabelNode firstLabel = null;
    private static LabelNode lastLabel;

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
     * @param mNode       helper method
     * @param foundMethod method to be inserted.
     * @return the helper method with it's new code
     */
    public static MethodNode insertNewNotVoidMethod(MethodNode mNode, MethodNode foundMethod) {
        mNode.instructions = foundMethod.instructions;
        mNode.localVariables = foundMethod.localVariables;
        mNode.maxLocals = foundMethod.maxLocals;
        mNode.maxStack = foundMethod.maxStack;
        mNode.instructions.resetLabels();

        return mNode;
    }
}
