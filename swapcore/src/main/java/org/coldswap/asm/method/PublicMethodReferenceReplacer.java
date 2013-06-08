package org.coldswap.asm.method;

import org.coldswap.asm.MethodBox;
import org.coldswap.asm.ReferenceReplacer;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

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
 * Created with IntelliJ IDEA.
 * User: faur
 * Date: 5/23/13
 * Time: 8:55 PM
 */

/**
 * Substitute an invoke of a public method contained in a specified class with a dummy one
 * created at transformation time.
 */
public class PublicMethodReferenceReplacer implements ReferenceReplacer {
    private MethodBox refactor;

    /**
     * Constructs a replacer for a give method.
     *
     * @param classContainer class which contains a method whose invoke should be replaced.
     * @param methodName     method whose invoke should be replaced.
     * @param retType        method return type.
     * @param paramType      an array of method parameters type.
     */
    public PublicMethodReferenceReplacer(String classContainer, String methodName, Type retType, Type[] paramType) {

    }

    @Override
    public int findAndReplace(ClassNode classNode) {
        List<MethodNode> methodes = classNode.methods;
        for (MethodNode methodNode : methodes) {
            refactor.replaceInvoke(methodNode);
        }

        return 0;
    }
}
