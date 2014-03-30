package org.coldswap.asm.field;

import org.coldswap.asm.MemberReplacer;
import org.coldswap.transformer.ReferenceReplacerManager;
import org.coldswap.util.ByteCodeClassWriter;
import org.coldswap.util.ByteCodeGenerator;
import org.coldswap.util.ClassUtil;
import org.coldswap.util.TransformerNameGenerator;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
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
 * 12:33 PM       4/13/13
 */

public class PublicStaticFieldReplacer implements MemberReplacer {
    private final static Logger logger = Logger.getLogger(PublicStaticFieldReplacer.class.getName());
    private ReferenceReplacerManager replacerManager = ReferenceReplacerManager.getInstance();
    private Class<?> aClass;
    private byte[] bytes;
    private String classPackage = "";

    static {
        logger.setLevel(ClassUtil.logLevel);
    }

    public PublicStaticFieldReplacer(Class<?> clazz, byte[] bytes) {
        this.aClass = clazz;
        this.bytes = bytes;
        String sPackage = "";
        Package p = clazz.getPackage();
        if (p != null) {
            sPackage = p.getName();
        }
        classPackage = sPackage.replace(".", ClassUtil.fileSeparator) + ClassUtil.fileSeparator;
    }

    public byte[] replace() {
        final ClassNode cn = new ClassNode(Opcodes.ASM5);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        cr.accept(cn, 0);
        // get a list of public/protected/package/private fields but exclude
        // inherited fields.
        Field[] refFields = aClass.getDeclaredFields();
        List asmFields = cn.fields;
        Iterator iterator = asmFields.iterator();
        while (iterator.hasNext()) {
            final FieldNode fNode = (FieldNode) iterator.next();
            int publicStatic = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC;
            //int publicStaticFinal = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
            // search only for public static fields
            if ((fNode.access == publicStatic)) {
                // check if this field exist in the old loaded class
                // and if not proceed with the trick.
                boolean foundIt = false;
                for (Field refField : refFields) {
                    if (fNode.name.equals(refField.getName())) {
                        if (fNode.desc.equals(Type.getType(refField.getType()).getDescriptor())) {
                            foundIt = true;
                            break;
                        }
                    }
                }
                if (!foundIt) {
                    // register a public static reference replacer
                    String contClass = cn.name.substring(cn.name.lastIndexOf("/") + 1);
                    String className = TransformerNameGenerator.getPublicStaticFieldClassName(contClass, fNode.name);
                    replacerManager.registerFieldReferenceReplacer(new PublicStaticFieldReferenceReplacer(contClass,
                            fNode, classPackage + className));
                    // remove the static reference from <clinit>
                    InsnList insnList = cleanClInit(cn, fNode);
                    // create a new class that contains the field
                    byte[] newBClass = ByteCodeGenerator.newFieldClass(cn, fNode, insnList, classPackage + className);
                    try {
                        String cp = ClassUtil.getClassPath(aClass);
                        ByteCodeClassWriter.setClassPath(cp);
                        ByteCodeClassWriter.writeClass(className, newBClass);
                    } catch (IOException e) {
                        logger.warning(e.toString());
                    }

                    iterator.remove();
                    // replace every reference
                    replaceReferences(cn, fNode);
                }
            }
        }
        cn.accept(cw);
        return cw.toByteArray();
    }

    /**
     * Removes any initializing reference of the field.
     *
     * @param classNode containing the old class.
     * @param fieldNode containing the old field.
     * @return the initializing list of instructions.
     */
    @SuppressWarnings("unchecked")
    private InsnList cleanClInit(ClassNode classNode, FieldNode fieldNode) {
        List<MethodNode> methodNodes = classNode.methods;
        AbstractInsnNode firstInst = null;
        int counter = 0;
        for (MethodNode methodNode : methodNodes) {
            if (methodNode.name.equals("<clinit>")) {
                // search for PUTSTATIC
                InsnList insnList = methodNode.instructions;
                Iterator iterator1 = insnList.iterator();
                while (iterator1.hasNext()) {
                    AbstractInsnNode ins2 = (AbstractInsnNode) iterator1.next();
                    // if a initializing has been found, then copy everything from
                    // the corresponding label to the PUTSTATIC
                    if (ins2.getOpcode() == Opcodes.PUTSTATIC) {
                        final Boolean[] fieldFound = {false};
                        final FieldNode fNode = fieldNode;
                        ins2.accept(new MethodVisitor(Opcodes.ASM5) {
                            @Override
                            public void visitFieldInsn(int i, String s, String s2, String s3) {
                                if (s2.equals(fNode.name)) {
                                    fieldFound[0] = true;
                                }
                                super.visitFieldInsn(i, s, s2, s3);
                            }
                        });
                        if (fieldFound[0]) {
                            // find the first PUTSTATIC before this one.
                            boolean staticFound = false;
                            while (!staticFound) {
                                AbstractInsnNode tmpInst = ins2.getPrevious();
                                if (tmpInst != null) {
                                    if (tmpInst.getOpcode() != Opcodes.F_NEW) {
                                        if (tmpInst.getOpcode() == Opcodes.PUTSTATIC) {
                                            staticFound = true;
                                        } else {
                                            firstInst = tmpInst;
                                            counter++;
                                        }
                                    }
                                } else {
                                    staticFound = true;
                                }
                                ins2 = tmpInst;
                            }

                            break;
                        }
                    }
                }

                if (firstInst != null) {
                    InsnList iList = new InsnList();
                    iList.add(firstInst.clone(null));
                    counter--;
                    while (counter > 0) {
                        AbstractInsnNode ain = firstInst.getNext();
                        iList.add(ain.clone(null));
                        counter--;
                        insnList.remove(firstInst);
                        firstInst = ain;
                    }
                    // remove last instruction and the putstatic instruction
                    AbstractInsnNode putStatic = firstInst.getNext();
                    insnList.remove(firstInst);
                    insnList.remove(putStatic);
                    return iList;
                }
            }
        }
        return null;
    }

    /**
     * Replaces any GETSTATIC/PUTSTATIC call of the field in the old class with the field
     * introduced in the new class.
     *
     * @param classNode containing the old class.
     * @param fieldNode containing the old field.
     */
    private void replaceReferences(ClassNode classNode, FieldNode fieldNode) {
        List<MethodNode> methodNodes = classNode.methods;
        String contClass = classNode.name.substring(classNode.name.lastIndexOf("/") + 1);
        final String className = classPackage + TransformerNameGenerator.getPublicStaticFieldClassName(contClass, fieldNode.name);
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
                        final ClassNode cNode = classNode;
                        final FieldNode fNode = fieldNode;
                        absIns.accept(new MethodVisitor(Opcodes.ASM5) {
                            @Override
                            public void visitFieldInsn(int i, String s, String s2, String s3) {
                                if (cNode.name.equals(s) && fNode.name.equals(s2)) {
                                    foundField[0] = true;
                                }
                                super.visitFieldInsn(i, s, s2, s3);
                            }
                        });
                        if (foundField[0]) {
                            inst.set(absIns, new FieldInsnNode(Opcodes.GETSTATIC, className, fieldNode.name, fieldNode.desc));
                        }
                    }
                } else if (opcode == Opcodes.PUTSTATIC) {
                    if (absIns.getType() == AbstractInsnNode.FIELD_INSN) {
                        final Boolean[] foundField = {false};
                        final ClassNode cNode = classNode;
                        final FieldNode fNode = fieldNode;
                        absIns.accept(new MethodVisitor(Opcodes.ASM5) {
                            @Override
                            public void visitFieldInsn(int i, String s, String s2, String s3) {
                                if (cNode.name.equals(s) && fNode.name.equals(s2)) {
                                    foundField[0] = true;
                                }
                                super.visitFieldInsn(i, s, s2, s3);
                            }
                        });
                        if (foundField[0]) {
                            inst.set(absIns, new FieldInsnNode(Opcodes.PUTSTATIC, className, fieldNode.name, fieldNode.desc));
                        }
                    }
                }
            }
        }
    }

}
