package org.coldswap.asm.method;

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
 * 8:08 PM       5/14/13
 */

import org.coldswap.asm.MemberReplacer;
import org.coldswap.asm.MethodBox;
import org.coldswap.asm.NVInvokeVirtualMethod;
import org.coldswap.transformer.ReferenceReplacerManager;
import org.coldswap.util.AutoBoxing;
import org.coldswap.util.ByteCodeGenerator;
import org.coldswap.util.MethodUtil;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Searches for any new method that does not return
 * a void type and has parameter of type Object[], copy the code from the method to a helper method,
 * make some modification on the code and finally remove the new methods.
 */
public class PublicNotVoidMethodReplacer implements MemberReplacer {
    private static final Logger logger = Logger.getLogger(PublicNotVoidMethodReplacer.class.getName());
    private ReferenceReplacerManager refManager = ReferenceReplacerManager.getInstance();
    private Class<?> aClass;
    private byte[] bytes;

    static {
        logger.setLevel(Level.ALL);
    }

    public PublicNotVoidMethodReplacer(Class<?> aClass, byte[] bytes) {
        this.aClass = aClass;
        this.bytes = bytes;
    }

    @Override
    public byte[] replace() {
        final ClassNode cn = new ClassNode(Opcodes.ASM4);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(cn, 0);
        List<MethodBox> factory = new ArrayList<MethodBox>();
        // find first the helper method
        MethodNode helperMethod = MethodUtil.createHelperMethod(cn.name);
        cn.methods.add(helperMethod);
        // get a list of public/protected/package/private methods but exclude
        // inherited methods.
        Method[] methods = aClass.getDeclaredMethods();
        List asmMethods = cn.methods;
        Iterator iterator = asmMethods.iterator();
        while (iterator.hasNext()) {
            final MethodNode mNode = (MethodNode) iterator.next();
            // exlude <init>() and <cliniti>()
            if (!"<clinit>".equals(mNode.name) && !"<init>".equals(mNode.name)) {
                boolean foundIt = false;
                int publicAccessor = Opcodes.ACC_PUBLIC;
                // search only for public methods
                if ((mNode.access == publicAccessor)) {
                    // check if this method exist in the old loaded class
                    // and if not proceed with the trick.
                    Type mRetType = MethodUtil.getReturnType(mNode);
                    Type[] mParamType = MethodUtil.getParamsType(mNode);
                    for (Method method : methods) {
                        if (mNode.name.equals(method.getName())) {
                            Type fRetType = Type.getType(method).getReturnType();
                            if (mRetType.equals(fRetType)) {
                                Type[] fParamType = MethodUtil.classToType(method.getParameterTypes());
                                boolean tmp = true;
                                if (mParamType.length == fParamType.length) {
                                    for (int i = 0; i < mParamType.length; i++) {
                                        if (!mParamType[i].equals(fParamType[i])) {
                                            tmp = false;
                                        }
                                    }
                                }
                                foundIt = tmp;
                            }
                        }
                    }
                    if (!foundIt) {
                        // ignore any method that has return type of void
                        if (!mRetType.equals(Type.VOID_TYPE)) {
                            // work only with methods that have as a parameter
                            // Object[]. If the conditions are not asserted, remove the code so that
                            // redefinition will be successful.
                            if (mParamType.length == 1 && mParamType[0].equals(Type.getType("[Ljava/lang/Object;"))) {
                                refManager.registerFieldReferenceReplacer(new PublicMethodReferenceReplacer(cn.name,
                                        mNode.name, mRetType, mParamType));
                                // autobox return type.
                                if (AutoBoxing.isPrimitive(mRetType.getDescriptor())) {
                                    // replace any return reference with a boxing call and a areturn call
                                    mNode.instructions = replaceReturn(mNode.instructions, mRetType);
                                }
                                // reload params from stack correctly, and make the unboxing
                                mNode.instructions = replaceLoadFromStack(mNode.instructions, mParamType);
                                mNode.instructions = replaceStoreToStack(mNode.instructions, mParamType);
                                // modify the helper method to mirror the new changes.
                                helperMethod.instructions = ByteCodeGenerator.insertNewNotVoidMethod(helperMethod,
                                        mNode.instructions, mNode.name, mParamType.length);
                                // replace method call
                                factory.add(new NVInvokeVirtualMethod(cn.name, mNode.name, mRetType, mParamType));
                            }
                            // remove method
                            iterator.remove();

                        }
                    }
                }
            }
        }
        // start replacing references
        Iterator secondIterator = asmMethods.iterator();
        while (secondIterator.hasNext()) {
            MethodNode container = (MethodNode) secondIterator.next();
            for (MethodBox methodReplacer : factory) {
                methodReplacer.replaceInvoke(container);
            }

        }
        cn.accept(cw);
        return cw.toByteArray();
    }

    private InsnList replaceLoadFromStack(InsnList instructions, Type[] mParamType) {
        Iterator iter = instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
            int opcode = absIns.getOpcode();
            if ((opcode == Opcodes.ALOAD) || (opcode == Opcodes.ILOAD) || (opcode == Opcodes.LLOAD)
                    || (opcode == Opcodes.FLOAD) || (opcode == Opcodes.DLOAD)) {
                // find stack order number
                final int[] stackIndex = {-1};
                absIns.accept(new MethodVisitor(Opcodes.ASM4) {
                    @Override
                    public void visitVarInsn(int i, int i2) {
                        stackIndex[0] = i2;
                        super.visitVarInsn(i, i2);
                    }
                });
                // if the stack order number is less than mParamType.length + 1 and greater than 0
                // than replace with aload stackIndex + 1
                // bipush param_nr
                // aaload
                // AutoBoxing.unbox
                // else if (stackIndex >= mParamType.length + 1 )just put opcode stackIndex + 1

                InsnList iList = new InsnList();
                if ((stackIndex[0] < mParamType.length + 1) && (stackIndex[0] > 0)) {
                    iList.add(new VarInsnNode(Opcodes.ALOAD, stackIndex[0] + 1));
                    // no unboxing because the method params type is already Object[]
                    //iList.add(new IntInsnNode(Opcodes.BIPUSH, stackIndex[0] - 1));
                    //iList.add(new InsnNode(Opcodes.AALOAD));
                    //iList.add(AutoBoxing.unbox(mParamType[stackIndex[0] - 1]));
                } else if (stackIndex[0] >= mParamType.length + 1) {
                    iList.add(new VarInsnNode(opcode, stackIndex[0] + 1));
                }
                instructions.insertBefore(absIns, iList);
                // remove the old load
                iter.remove();
            }
        }
        return instructions;
    }

    private InsnList replaceStoreToStack(InsnList instructions, Type[] mParamType) {
        Iterator iter = instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
            int opcode = absIns.getOpcode();
            if ((opcode == Opcodes.ASTORE) || (opcode == Opcodes.ISTORE) || (opcode == Opcodes.LSTORE)
                    || (opcode == Opcodes.FSTORE) || (opcode == Opcodes.DSTORE)) {
                // find stack order number
                final int[] stackIndex = {-1};
                absIns.accept(new MethodVisitor(Opcodes.ASM4) {
                    @Override
                    public void visitVarInsn(int i, int i2) {
                        stackIndex[0] = i2;
                        super.visitVarInsn(i, i2);
                    }
                });
                // if the stack order number is less than mParamType.length + 1 and greater than 0
                // than replace with aload stackIndex + 1
                // bipush param_nr
                // code before opcode until a label is found
                // AutoBoxing.unbox
                // aastore
                // else if (stackIndex >= mParamType.length + 1 )just put opcode stackIndex + 1

                InsnList iList = new InsnList();
                if ((stackIndex[0] < mParamType.length + 1) && (stackIndex[0] > 0)) {
                    iList.add(new VarInsnNode(Opcodes.ALOAD, stackIndex[0] + 1));
//                    iList.add(new IntInsnNode(Opcodes.BIPUSH, stackIndex[0] - 1));
//                    iList.add(getCodeFromLabelToStoreOpcode(instructions, absIns));
//                    iList.add(AutoBoxing.unbox(mParamType[stackIndex[0] - 1]));
//                    iList.add(new InsnNode(Opcodes.AASTORE));
                } else if (stackIndex[0] >= mParamType.length + 1) {
                    iList.add(new VarInsnNode(opcode, stackIndex[0] + 1));
                }
                instructions.insert(absIns, iList);
                // remove the old load
                iter.remove();
            }
        }
        return instructions;
    }

    /**
     * Copies and removes a chunck of code from a give store opcode like istore/astore etc.. up to a label.
     * The store opcode is not removed.
     *
     * @param insnList
     * @param storeOpcode
     * @return
     */
    private InsnList getCodeFromLabelToStoreOpcode(InsnList insnList, AbstractInsnNode storeOpcode) {
        InsnList toRet = new InsnList();
        boolean isLabel = false;
        int labelIndex = -1;
        int storeIndex = insnList.indexOf(storeOpcode);
        // find label index
        while (!isLabel) {
            if (storeOpcode.getOpcode() == -1) {
                labelIndex = insnList.indexOf(storeOpcode);
                isLabel = true;
            }
            storeOpcode = storeOpcode.getPrevious();
        }
        if (labelIndex > -1) {
            // start cloning from labelIndex + 1 until storeIndex - 1
            for (int i = labelIndex + 1; i < storeIndex; i++) {
                toRet.add(insnList.get(i).clone(null));
            }
            // remove what has been cloned
            for (int i = labelIndex; i < storeIndex; i++) {
                insnList.remove(insnList.get(i));
            }
        }
        return toRet;

    }

    private InsnList replaceReturn(InsnList insnList, Type retType) {
        int retOpcode = getRetOpcodeToReplace(retType);
        Iterator iter = insnList.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode absIns = (AbstractInsnNode) iter.next();
            int opcode = absIns.getOpcode();
            if (opcode == retOpcode) {
                // replace this instruction with a boxing and a areturn
                InsnList iList = new InsnList();
                iList.add(AutoBoxing.box(retType));
                iList.add(new InsnNode(Opcodes.ARETURN));
                insnList.insertBefore(absIns, iList);
                // remove the old return
                iter.remove();
            }
        }
        return insnList;
    }

    private int getRetOpcodeToReplace(Type retType) {
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
