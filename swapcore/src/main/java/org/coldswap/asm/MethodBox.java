package org.coldswap.asm;

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
 * Date: 5/28/13
 * Time: 5:17 PM
 */

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

/**
 * Store info about a method that should be replaced with another invoke.
 */
public abstract class MethodBox {
    protected final String classContainer;
    protected final String methodName;
    protected final Type retType;
    protected final Type[] paramType;

    /**
     * Constructs an object that will contain basic information about a
     * method whose call will be replaced.
     *
     * @param classContainer the name of the class containig the method
     *                       whose invoke should be replaced.
     * @param methodName     the name of the method whose invoke should be replaced.
     * @param retType        return {@link org.objectweb.asm.Type} of the method
     * @param paramType      parameters {@link org.objectweb.asm.Type}.
     */
    public MethodBox(String classContainer, String methodName, Type retType, Type[] paramType) {
        this.classContainer = classContainer;
        this.methodName = methodName;
        this.retType = retType;
        this.paramType = paramType;
    }

    public abstract MethodNode replaceInvoke(MethodNode methodNode);
}
