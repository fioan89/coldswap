package org.coldswap.asm;

import org.coldswap.util.ByteCodeClassWriter;
import org.coldswap.util.TransformerNameGenerator;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
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

public class PublicStaticFieldReplacer {
    private final static Logger logger = Logger.getLogger(PublicStaticFieldReplacer.class.getName());
    private Class<?> aClass;
    private byte[] bytes;

    public PublicStaticFieldReplacer(Class<?> clazz, byte[] bytes) {
        this.aClass = clazz;
        this.bytes = bytes;
    }

    public byte[] replace() {
        final ClassNode cn = new ClassNode(Opcodes.ASM4);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
        cr.accept(cn, 0);
        // get a list of public/protected/package/private fields but exclude
        // inherited fields.
        Field[] refFields = aClass.getDeclaredFields();
        List<FieldNode> asmFields = cn.fields;
        Iterator<FieldNode> iterator = asmFields.iterator();
        while (iterator.hasNext()) {
            final FieldNode fNode = iterator.next();
            int publicStatic = Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC;
            //int publicStaticFinal = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
            // search only for public static and public static final fields
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
                    // create a new class that contains the field
                    // remove the field from the loaded bytes
                    // load the new class with the static field
                    // and replace every reference.
                    String contClass = cn.name.substring(cn.name.lastIndexOf("/") + 1);
                    final String className = TransformerNameGenerator.getPublicStaticFieldClassName(contClass, fNode.name);
                    ClassNode newClass = new ClassNode();
                    newClass.version = cn.version;
                    newClass.access = Opcodes.ACC_PUBLIC;
                    newClass.signature = "L" + className + ";";
                    newClass.name = className;
                    newClass.superName = "java/lang/Object";
                    newClass.fields.add(new FieldNode(fNode.access, fNode.name, fNode.desc, fNode.desc, fNode.value));
                    ClassWriter newCWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                    newClass.accept(newCWriter);
                    byte[] newBClass = newCWriter.toByteArray();
                    try {
                        String cp = getClassPath();
                        ByteCodeClassWriter.setClassPath(cp);
                        ByteCodeClassWriter.writeClass(className, newBClass);
                    } catch (IOException e) {
                        logger.warning(e.toString());
                    }
                    iterator.remove();
                    List<MethodNode> methodNodes = cn.methods;
                    // remove the static reference from <clinit>
                    for (MethodNode methodNode : methodNodes)
                        if (methodNode.name.equals("<clinit>")) {
                            // search for PUTSTATIC
                            InsnList insnList = methodNode.instructions;
                            Iterator iterator1 = insnList.iterator();
                            while (iterator1.hasNext()) {
                                AbstractInsnNode abstractInsnNode = (AbstractInsnNode) iterator1.next();
                                int opcode = abstractInsnNode.getOpcode();
                                if (opcode == Opcodes.LDC) {
                                    AbstractInsnNode ins2 = (AbstractInsnNode) iterator1.next();
                                    if (ins2.getOpcode() == Opcodes.PUTSTATIC) {
                                        final Boolean[] fieldFound = {false};
                                        ins2.accept(new MethodVisitor(Opcodes.ASM4) {
                                            @Override
                                            public void visitFieldInsn(int i, String s, String s2, String s3) {
                                                if (s2.equals(fNode.name)) {
                                                    fieldFound[0] = true;
                                                }
                                                super.visitFieldInsn(i, s, s2, s3);    //To change body of overridden methods use File | Settings | File Templates.
                                            }
                                        });
                                        if (fieldFound[0]) {
                                            insnList.remove(abstractInsnNode);
                                            insnList.remove(ins2);
                                        }
                                    }

                                }
                            }
                        }

                    // replace every reference
                    for (MethodNode method : methodNodes) {
                        InsnList inst = method.instructions;
                        Iterator iter = inst.iterator();
                        while (iter.hasNext()) {
                            AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
                            int opcode = absIns.getOpcode();
                            if (opcode == Opcodes.GETSTATIC) {
                                // get type
                                if (absIns.getType() == AbstractInsnNode.FIELD_INSN) {
                                    final Boolean[] foundField = {false};
                                    absIns.accept(new MethodVisitor(Opcodes.ASM4) {
                                        @Override
                                        public void visitFieldInsn(int i, String s, String s2, String s3) {
                                            String owner = s;
                                            String name = s2;
                                            String desc = s3;
                                            if (cn.name.equals(owner) && fNode.name.equals(name)) {
                                                foundField[0] = true;
                                            }
                                            super.visitFieldInsn(i, owner, name, desc);
                                        }
                                    });
                                    if (foundField[0]) {
                                        inst.set(absIns, new FieldInsnNode(Opcodes.GETSTATIC, className, fNode.name, fNode.desc));
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        cn.accept(cw);
        return cw.toByteArray();
    }

    /**
     * Finds where is stored the byte code for {@link PublicStaticFieldReplacer#aClass }
     *
     * @return returns the parent directory of class
     */
    private String getClassPath() {
        String path = aClass.getClassLoader().getResource(
                aClass.getName().replace('.', '/') + ".class").toString();
        String parent = null;
        try {
            parent = new File(new URI(path)).getParent();
        } catch (URISyntaxException e) {
            logger.warning(e.toString());
        }
        return parent;
    }
}
