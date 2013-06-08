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
 * Created with IntelliJ IDEA.
 * User: faur
 * Date: 6/6/13
 * Time: 8:16 PM
 */


import org.coldswap.asm.MemberReplacer;
import org.coldswap.asm.MethodBox;
import org.coldswap.asm.VirtualMethodReplacer;
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
public class PublicObjectMethodReplacer implements MemberReplacer {
    private static final Logger logger = Logger.getLogger(PublicObjectMethodReplacer.class.getName());
    private final int maxNumberOfMethods;
    private ReferenceReplacerManager refManager = ReferenceReplacerManager.getInstance();
    private Class<?> aClass;
    private byte[] bytes;

    static {
        logger.setLevel(Level.ALL);
    }

    public PublicObjectMethodReplacer(Class<?> aClass, byte[] bytes, int maxMethods) {
        this.aClass = aClass;
        this.bytes = bytes;
        this.maxNumberOfMethods = maxMethods;
    }

    @Override
    public byte[] replace() {
        int counter = -1;
        final ClassNode cn = new ClassNode(Opcodes.ASM4);
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(cn, 0);
        List<MethodBox> factory = new ArrayList<MethodBox>();
        // find first the helper method
        MethodNode[] helperMethod = new MethodNode[maxNumberOfMethods];
        for (int i = 0; i < maxNumberOfMethods; i++) {
            helperMethod[i] = MethodUtil.createObjectHelperMethod(cn.name, i);
            cn.methods.add(helperMethod[i]);
        }
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
                                if (counter < maxNumberOfMethods) {
                                    counter++;
                                    refManager.registerFieldReferenceReplacer(new PublicMethodReferenceReplacer(cn.name,
                                            mNode.name, mRetType, mParamType, counter));
                                    // autobox return type.
                                    if (AutoBoxing.isPrimitive(mRetType.getDescriptor())) {
                                        // replace any return reference with a boxing call and a areturn call
                                        mNode.instructions = replaceReturn(mNode.instructions, mRetType);
                                    }
                                    helperMethod[counter] = ByteCodeGenerator.insertNewNotVoidMethod(helperMethod[counter],
                                            mNode);
                                    // replace method call
                                    factory.add(new VirtualMethodReplacer(cn.name, mNode.name, mRetType, mParamType, counter));
                                }
                            }
                            // remove method
                            iterator.remove();

                        }
                    }
                }
            }
        }

        // start replacing references
        for (Object asmMethod : asmMethods) {
            MethodNode container = (MethodNode) asmMethod;
            for (MethodBox methodReplacer : factory) {
                methodReplacer.replaceInvoke(container);
            }

        }
        cn.accept(cw);
        return cw.toByteArray();
    }

    private InsnList replaceReturn(InsnList insnList, Type retType) {
        final Type rretType = retType;
        int retOpcode = MethodUtil.getRetOpcodeToReplace(retType);
        for (int i = 0; i < insnList.size(); i++) {
            AbstractInsnNode absIns = insnList.get(i);
            int opcode = absIns.getOpcode();
            if (opcode == retOpcode) {
                // if tries to return a Reference type into a primitive then
                // remove the unbox( we return an Object). If a primitive is returned
                // into a  primitive then we must try to box from primitive to Object/Integer, etc..

                // check if an unbox takes place before return
                final boolean[] isBoxUnbox = {false, false};
                AbstractInsnNode valueOf = null;
                AbstractInsnNode primitiveValue = null;
                if (i > 1) {
                    valueOf = insnList.get(i - 1);
                    primitiveValue = insnList.get(i - 2);
                    if (valueOf.getOpcode() == Opcodes.INVOKESTATIC) {
                        valueOf.accept(new MethodVisitor(Opcodes.ASM4) {
                            @Override
                            public void visitMethodInsn(int i, String s, String s2, String s3) {
                                if (AutoBoxing.isPrimitive(rretType.getDescriptor())) {
                                    if ((AutoBoxing.getBoxClassName(rretType) + ".valueOf").equals(s + s2)) {
                                        isBoxUnbox[0] = true;
                                    }
                                }
                                super.visitMethodInsn(i, s, s2, s3);
                            }
                        });
                    }

                    if (isBoxUnbox[0] && primitiveValue.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        primitiveValue.accept(new MethodVisitor(Opcodes.ASM4) {
                            @Override
                            public void visitMethodInsn(int i, String s, String s2, String s3) {
                                if ((s + s2).equals(AutoBoxing.getUnBoxInvoke(rretType))) {
                                    isBoxUnbox[1] = true;
                                }
                                super.visitMethodInsn(i, s, s2, s3);
                            }
                        });
                    }
                }

                if (isBoxUnbox[0] && isBoxUnbox[1]) {
                    // remove indexes
                    insnList.remove(valueOf);
                    insnList.remove(primitiveValue);
                } else {
                    InsnList iList = new InsnList();
                    iList.add(AutoBoxing.box(retType));
                    iList.add(new InsnNode(Opcodes.ARETURN));
                    insnList.insertBefore(absIns, iList);
                    insnList.remove(absIns);
                }
            }
        }
        return insnList;
    }

}

