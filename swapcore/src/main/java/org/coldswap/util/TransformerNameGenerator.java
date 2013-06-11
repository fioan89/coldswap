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
 * 6:52 PM       3/30/13
 */

/**
 * A simple class helper that builds name for every field added at
 * loading time.
 */
public class TransformerNameGenerator {

    /**
     * Returns a name for a public field.
     *
     * @param className that will be transformed.
     * @return a {@link java.lang.String}, which is a concatenation of class name  and "__publicMethodsContainer".
     */
    public static String getPublicMethodName(String className) {

        return getClassName(className) + "__publicMethodsContainer";
    }


    public static String getPublicStaticFieldName(String className) {
        return className + "__publicStaticFieldContainer";
    }

    public static String getPublicStaticFieldClassName(String containerClass, String fieldName) {
        return "PS" + getClassName(containerClass) + fieldName;
    }

    public static String getPrivateStaticFieldClassName(String containerClass, String fieldName) {
        return "PVS" + getClassName(containerClass) + fieldName;
    }

    public static String getProtectedStaticFieldClassName(String containerClass, String fieldName) {
        return "PRS" + getClassName(containerClass) + fieldName;
    }

    public static String getObjectMethodName(String containerClass) {
        return "NMO" + getClassName(containerClass) + "newObjectMethod";
    }

    public static String getObjectMethodNameWithCounter(String containerClass, int counter) {
        return "NMO" + getClassName(containerClass) + "newObjectMethod" + String.valueOf(counter);
    }

    public static String getIntMethodNameWithCounter(String containerClass, int counter) {
        return "NMO" + getClassName(containerClass) + "newIntMethod" + String.valueOf(counter);
    }

    public static String getFloatMethodNameWithCounter(String containerClass, int counter) {
        return "NMO" + getClassName(containerClass) + "newFloatMethod" + String.valueOf(counter);
    }

    public static String getStringMethodNameWithCounter(String containerClass, int counter) {
        return "NMO" + getClassName(containerClass) + "newStringMethod" + String.valueOf(counter);
    }

    public static String getLongMethodNameWithCounter(String containerClass, int counter) {
        return "NMO" + getClassName(containerClass) + "newLongMethod" + String.valueOf(counter);
    }

    /**
     * Gets class name from a string that looks like package/name/ClassName.
     *
     * @param containerClass full name of a class(package name + class name).
     * @return class name.
     */
    private static String getClassName(String containerClass) {
        if (containerClass != null) {
            int index = containerClass.lastIndexOf("/");
            if (index > -1) {
                return containerClass.substring(index + 1);
            } else {
                return containerClass;
            }
        }
        return null;
    }

}
