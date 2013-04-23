package org.coldswap.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
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
 * 4:07 PM       4/1/13
 */

/**
 * Insert a new final static field and initialize it.
 */
public class NewFSFieldTransformer implements ASMClassLoadTransformer {
    private String owner;
    private String name;
    private int fAcc;
    private String desc;
    private String signature;
    private Object value;
    private final static Logger logger = Logger.getLogger(ASMClassLoadTransformer.class.getName());

    static {
        logger.setLevel(Level.ALL);
    }

    /**
     * Constructs an adapter for a new field insertion.
     *
     * @param owner       Class owner of the new field.
     * @param fieldAccess a numeric value representing the access property of a field.
     *                    It can be a combination of ACC_PRIVATE, ACC_FINAL, ACC_STATIC,
     *                    ACC_PUBLIC, ACC_PROTECTED.
     * @param name        The name of the field.
     * @param desc        Field description.
     */
    public NewFSFieldTransformer(String owner, int fieldAccess, String name, String desc, String signature, Object value) {
        this.owner = owner;
        this.name = name;
        this.fAcc = fieldAccess;
        this.desc = desc;
        this.signature = signature;
        this.value = value;
    }


    @Override
    public int transformClass(ClassNode classNode) {
        boolean clInitFound = false;
        for (FieldNode fn : (List<FieldNode>) classNode.fields) {
            if (name.equals(fn.name)) {
                logger.severe("Duplicate field name <" + name + ">!");
                return -1;
            }
        }

        classNode.fields.add(new FieldNode(fAcc, name, desc, signature, null));
        // initialize this field
        for (MethodNode mn : (List<MethodNode>) classNode.methods) {
            if ("<clinit>".equals(mn.name)) {
                clInitFound = true;
                InsnList inst = mn.instructions;
                Iterator iter = inst.iterator();
                while (iter.hasNext()) {
                    AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
                    int opcode = absIns.getOpcode();
                    if (opcode == Opcodes.RETURN) {
                        InsnList l = new InsnList();
                        LabelNode l0 = new LabelNode();
                        l.add(new JumpInsnNode(Opcodes.IFLT, l0));
                        l.insert(new TypeInsnNode(Opcodes.NEW, "java/lang/HashMap"));
                        l.insert(new InsnNode(Opcodes.DUP));
                        l.insert(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V"));
                        l.insert(new FieldInsnNode(Opcodes.PUTSTATIC, owner, name, "Ljava/util/HashMap;"));
                        inst.insert(absIns.getPrevious(), l);
                    }
                }
            }
        }

        if (!clInitFound) {
            // create a new method called clinit and insert the new initializer
            MethodNode mn = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            InsnList l = mn.instructions;
            LabelNode l0 = new LabelNode();
            l.add(new JumpInsnNode(Opcodes.IFLT, l0));
            l.insert(new TypeInsnNode(Opcodes.NEW, "java/lang/HashMap"));
            l.insert(new InsnNode(Opcodes.DUP));
            l.insert(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V"));
            l.insert(new FieldInsnNode(Opcodes.PUTSTATIC, owner, name, "Ljava/util/HashMap;"));
            l.insert(new InsnNode(Opcodes.RETURN));
            classNode.methods.add(mn);
        }
        return 1;
    }
}
