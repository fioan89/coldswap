package org.fioan89.coldswap.dummy.tests;

import org.coldswap.util.ByteCodeClassLoader;
import org.coldswap.util.ByteCodeClassWriter;
import org.coldswap.util.ClassUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
 * Date: 6/27/13
 * Time: 4:14 PM
 */

public class TestNewPrivateStaticField {
    private final static int acc = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
    private static final double DELTA = 1e-15;
    private static int object = 5;

    public static final int METHODNUMBER = 2;

    @BeforeClass
    public static void setUp() {

        byte[] newBClass;
        try {
            String sep = System.getProperty("file.separator");
            String cp = ClassUtil.getClassPath(TestNewPrivateStaticField.class);
            ByteCodeClassWriter.setClassPath(cp);
            newBClass = ByteCodeClassLoader.loadClassBytes(cp + sep + TestNewPrivateStaticField.class.getSimpleName() + ".class");
            final ClassNode cn = new ClassNode(Opcodes.ASM4);
            ClassReader cr = new ClassReader(newBClass);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
            cr.accept(cn, 0);
            // create fields and insert them
            cn.fields.add(new FieldNode(acc, "intValue", "I", null, null));
            cn.fields.add(new FieldNode(acc, "stringValue", "Ljava/lang/String;", null, null));
            cn.fields.add(new FieldNode(acc, "longValue", "J", null, null));
            cn.fields.add(new FieldNode(acc, "doubleValue", "D", null, null));
            // find <clinit> and testFieldInsertion methods
            MethodNode clInit = null;
            MethodNode prv = null;
            List<MethodNode> methods = cn.methods;
            for (MethodNode method : methods) {
                if ("<clinit>".equals(method.name)) {
                    clInit = method;
                } else if ("testFieldInsertion".equals(method.name)) {
                    prv = method;
                }
            }
            // create init instructions
            InsnList inst = new InsnList();
            inst.add(new LabelNode());
            inst.add(new LdcInsnNode("file.separator"));
            inst.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;"));
            inst.add(new LabelNode());
            inst.add(new InsnNode(Opcodes.ICONST_5));
            inst.add(new FieldInsnNode(Opcodes.PUTSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "object", "I"));
            inst.add(new LabelNode());
            inst.add(new IntInsnNode(Opcodes.BIPUSH, 10));
            inst.add(new FieldInsnNode(Opcodes.PUTSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "intValue", "I"));
            inst.add(new LabelNode());
            inst.add(new LdcInsnNode("stringValue"));
            inst.add(new FieldInsnNode(Opcodes.PUTSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "stringValue", "Ljava/lang/String;"));
            inst.add(new LabelNode());
            inst.add(new LdcInsnNode(new Long(10L)));
            inst.add(new FieldInsnNode(Opcodes.PUTSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "longValue", "J"));
            inst.add(new LabelNode());
            inst.add(new LdcInsnNode(new Double("10.0")));
            inst.add(new FieldInsnNode(Opcodes.PUTSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "doubleValue", "D"));
            inst.add(new InsnNode(Opcodes.RETURN));
            clInit.instructions = inst;
            clInit.maxStack = 2;

            // create test instructions
            InsnList inst1 = new InsnList();
            inst1.add(new LabelNode());
            inst1.add(new LdcInsnNode(new Long(10L)));
            inst1.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "intValue", "I"));
            inst1.add(new InsnNode(Opcodes.I2L));
            inst1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/junit/Assert", "assertEquals", "(JJ)V"));

            inst1.add(new LabelNode());
            inst1.add(new LdcInsnNode("stringValue"));
            inst1.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "stringValue", "Ljava/lang/String;"));
            inst1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/junit/Assert", "assertEquals", "(Ljava/lang/Object;Ljava/lang/Object;)V"));
            inst1.add(new LabelNode());
            inst1.add(new LdcInsnNode(new Long(10L)));
            inst1.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "longValue", "J"));
            inst1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/junit/Assert", "assertEquals", "(JJ)V"));
            inst1.add(new LabelNode());
            inst1.add(new LdcInsnNode(new Double("10.0")));
            inst1.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/fioan89/coldswap/dummy/tests/TestNewPrivateStaticField", "doubleValue", "D"));
            inst1.add(new LdcInsnNode(new Double("1.0E-15")));
            inst1.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/junit/Assert", "assertEquals", "(DDD)V"));
            inst1.add(new LabelNode());
            inst1.add(new InsnNode(Opcodes.RETURN));
            prv.instructions = inst1;
            prv.maxLocals = 1;
            prv.maxStack = 6;
            cn.accept(cw);
            newBClass = cw.toByteArray();
            ByteCodeClassWriter.writeClass(TestNewPrivateStaticField.class.getSimpleName(), newBClass, true);
            Thread.sleep(2000);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDummy() {
        assertEquals(5, object);
    }

    @Test
    public void testFieldInsertion() {
        assertEquals("TestNewPrivateStaticField", "TestNewPrivateStaticField");
    }
}
