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
 * 8:39 PM       5/14/13
 */

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/**
 * Helper class for extracting method return type, method params type, etc...
 */
public class MethodUtil {

    public static Type getReturnType(MethodNode methodNode) {
        String methodDesc = methodNode.desc;
        // get 2 strings one containing the var params and
        // the other the return type
        String[] descs = methodDesc.split("\\)");
        return Type.getReturnType(descs[1]);
    }

    public static Type[] getParamsType(MethodNode methodNode) {
        String methodDesc = methodNode.desc;
        // get 2 strings one containing the var params and
        // the other the return type
        String[] descs = methodDesc.split("\\)");
        // get a list of params that this method has
        String[] paramType = null;
        if (!descs[0].equals("(")) {
            paramType = descs[0].substring(1).split(";");
            // append ";" if param is not primitive type
            for (int i = 0; i < paramType.length; i++) {
                if (paramType[i].length() > 1) {
                    paramType[i] = paramType[i] + ";";
                }
            }
        }
        Type[] toRet = new Type[0];
        if (paramType != null) {
            toRet = new Type[paramType.length];
            for (int i = 0; i < toRet.length; i++) {
                toRet[i] = Type.getType(paramType[i]);
            }
        }
        return toRet;
    }

    public static Type classToType(Class<?> aClass) {
        return Type.getType(aClass);
    }

    public static Type[] classToType(Class<?>[] classes) {
        Type[] toRet = new Type[classes.length];
        for (int i = 0; i < toRet.length; i++) {
            toRet[i] = classToType(classes[i]);
        }
        return toRet;
    }

    public static MethodNode createObjectHelperMethod(String className, int counter) {
        int acc = Opcodes.ACC_PUBLIC;
        String methodName = TransformerNameGenerator.getObjectMethodNameWithCounter(className, counter);
        MethodNode mn = new MethodNode(acc, methodName, "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        InsnList insnList = mn.instructions;
        LabelNode l0 = new LabelNode();
        insnList.add(l0);
        insnList.add(new InsnNode(Opcodes.ACONST_NULL));
        insnList.add(new InsnNode(Opcodes.ARETURN));
        LabelNode l1 = new LabelNode();
        insnList.add(l1);
        String classLiteral = "L" + className + ";";
        mn.localVariables.add(new LocalVariableNode("this", classLiteral, null, l0, l1, 0));
        mn.localVariables.add(new LocalVariableNode("args", "[Ljava/lang/Object;", null, l0, l1, 1));
        mn.maxStack = 1;
        mn.maxLocals = 2;
        return mn;

    }

    public static MethodNode createIntHelperMethod(String className, int counter) {
        int acc = Opcodes.ACC_PUBLIC;
        String methodName = TransformerNameGenerator.getIntMethodNameWithCounter(className, counter);
        MethodNode mn = new MethodNode(acc, methodName, "(I)Ljava/lang/Object;", null, null);
        InsnList insnList = mn.instructions;
        LabelNode l0 = new LabelNode();
        insnList.add(l0);
        insnList.add(new InsnNode(Opcodes.ACONST_NULL));
        insnList.add(new InsnNode(Opcodes.ARETURN));
        LabelNode l1 = new LabelNode();
        insnList.add(l1);
        String classLiteral = "L" + className + ";";
        mn.localVariables.add(new LocalVariableNode("this", classLiteral, null, l0, l1, 0));
        mn.localVariables.add(new LocalVariableNode("arg", "I", null, l0, l1, 1));
        mn.maxStack = 1;
        mn.maxLocals = 2;
        return mn;
    }

    public static MethodNode createFloatHelperMethod(String className, int counter) {
        int acc = Opcodes.ACC_PUBLIC;
        String methodName = TransformerNameGenerator.getFloatMethodNameWithCounter(className, counter);
        MethodNode mn = new MethodNode(acc, methodName, "(F)Ljava/lang/Object;", null, null);
        InsnList insnList = mn.instructions;
        LabelNode l0 = new LabelNode();
        insnList.add(l0);
        insnList.add(new InsnNode(Opcodes.ACONST_NULL));
        insnList.add(new InsnNode(Opcodes.ARETURN));
        LabelNode l1 = new LabelNode();
        insnList.add(l1);
        String classLiteral = "L" + className + ";";
        mn.localVariables.add(new LocalVariableNode("this", classLiteral, null, l0, l1, 0));
        mn.localVariables.add(new LocalVariableNode("arg", "F", null, l0, l1, 1));
        mn.maxStack = 1;
        mn.maxLocals = 2;
        return mn;
    }

    public static MethodNode createStringHelperMethod(String className, int counter) {
        int acc = Opcodes.ACC_PUBLIC;
        String methodName = TransformerNameGenerator.getStringMethodNameWithCounter(className, counter);
        MethodNode mn = new MethodNode(acc, methodName, "(Ljava/lang/String;)Ljava/lang/Object;", null, null);
        InsnList insnList = mn.instructions;
        LabelNode l0 = new LabelNode();
        insnList.add(l0);
        insnList.add(new InsnNode(Opcodes.ACONST_NULL));
        insnList.add(new InsnNode(Opcodes.ARETURN));
        LabelNode l1 = new LabelNode();
        insnList.add(l1);
        String classLiteral = "L" + className + ";";
        mn.localVariables.add(new LocalVariableNode("this", classLiteral, null, l0, l1, 0));
        mn.localVariables.add(new LocalVariableNode("arg", "Ljava/lang/String;", null, l0, l1, 1));
        mn.maxStack = 1;
        mn.maxLocals = 2;
        return mn;
    }

    public static int getRetOpcodeToReplace(Type retType) {
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
}
