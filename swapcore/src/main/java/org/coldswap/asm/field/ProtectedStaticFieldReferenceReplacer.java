package org.coldswap.asm.field;

import org.coldswap.asm.FieldBox;
import org.coldswap.asm.ReferenceReplacer;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.List;

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
 * 5:05 PM       5/5/13
 */

public class ProtectedStaticFieldReferenceReplacer extends FieldBox implements ReferenceReplacer {
    private String supperClass;

    /**
     * Constructs a container for a field reference that shTetsould be searched and replaced.
     *
     * @param oldClass       the old class reference.
     * @param fieldToReplace the field that should be instrumented.
     * @param newClass       the new class reference for the field.
     * @param supperClass    replace only on class that have this supper class.
     */
    public ProtectedStaticFieldReferenceReplacer(String oldClass, FieldNode fieldToReplace, String newClass, String supperClass) {
        super(oldClass, fieldToReplace, newClass);
        this.supperClass = supperClass;
    }

    @SuppressWarnings("uncheked")
    @Override
    public int findAndReplace(ClassNode classNode) {
        int counter = 0;
        if (classNode.superName.equals(supperClass)) {
            List<MethodNode> methodNodes = classNode.methods;
            for (MethodNode method : methodNodes) {
                InsnList inst = method.instructions;
                Iterator iter = inst.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
                    int opcode = absIns.getOpcode();
                    // check if instruction is GETSTATIC or PUTSTATIC
                    if (opcode == Opcodes.GETSTATIC) {
                        // get type
                        if (absIns.getType() == AbstractInsnNode.FIELD_INSN) {
                            final Boolean[] foundField = {false};
                            absIns.accept(new MethodVisitor(Opcodes.ASM4) {
                                @Override
                                public void visitFieldInsn(int i, String s, String s2, String s3) {
                                    if (oldClass.equals(s) && fieldToReplace.name.equals(s2)) {
                                        foundField[0] = true;
                                    }
                                    super.visitFieldInsn(i, s, s2, s3);
                                }
                            });
                            if (foundField[0]) {
                                inst.set(absIns, new FieldInsnNode(Opcodes.GETSTATIC, newClass, fieldToReplace.name, fieldToReplace.desc));
                                counter++;
                            }
                        }
                    } else if (opcode == Opcodes.PUTSTATIC) {
                        if (absIns.getType() == AbstractInsnNode.FIELD_INSN) {
                            final Boolean[] foundField = {false};
                            absIns.accept(new MethodVisitor(Opcodes.ASM4) {
                                @Override
                                public void visitFieldInsn(int i, String s, String s2, String s3) {
                                    if (oldClass.equals(s) && fieldToReplace.name.equals(s2)) {
                                        foundField[0] = true;
                                    }
                                    super.visitFieldInsn(i, s, s2, s3);
                                }
                            });
                            if (foundField[0]) {
                                inst.set(absIns, new FieldInsnNode(Opcodes.PUTSTATIC, newClass, fieldToReplace.name, fieldToReplace.desc));
                                counter++;
                            }
                        }
                    }
                }
            }
        }
        return counter;
    }
}
