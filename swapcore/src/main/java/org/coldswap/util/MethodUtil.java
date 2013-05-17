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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

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
        String retType = descs[1];
        // get a list of params that this method has
        String[] paramType = null;
        if (!descs[0].equals("(")) {
            paramType = descs[0].substring(1).split(";");
            // append ";"
            for (int i = 0; i < paramType.length; i++) {
                paramType[i] = paramType + ";";
            }
        }
        Type[] toRet = null;
        if (paramType != null) {
            toRet = new Type[paramType.length];
            for (int i = 0; i < toRet.length; i++) {
                toRet[i] = Type.getReturnType(paramType[i]);
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
}
