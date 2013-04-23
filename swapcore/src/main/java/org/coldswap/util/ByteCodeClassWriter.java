package org.coldswap.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
 * 5:56 PM       4/19/13
 */

/**
 * Writes an array of bytes to a file.
 */
public class ByteCodeClassWriter {
    private static File outDir = null;

    /**
     * Writes the given byte array to a file using the last class path that was
     * set with {@link ByteCodeClassWriter#setClassPath(String)}.
     *
     * @param className the name of the class.
     * @param bytes     array of bytes representing the class body.
     * @throws IOException                   if the file could not be written.
     * @throws ClassPathNullPointerException if the class path was not settled before.
     */
    public static void writeClass(String className, byte[] bytes) throws IOException, ClassPathNullPointerException {
        if (outDir == null) {
            throw new ClassPathNullPointerException("Class Path is null! You must specify where to write class " +
                    className + "!");
        }
        // create the file and schedule for removing when jvm stops
        File classToRemove = new File(outDir, className + ".class");
        classToRemove.deleteOnExit();
        DataOutputStream dout = new DataOutputStream(new FileOutputStream(classToRemove));
        dout.write(bytes);
        dout.flush();
        dout.close();
    }

    public static void setClassPath(String location) {
        outDir = new File(location);
        outDir.mkdir();
    }

    public static String getClassPath() {
        return outDir.getAbsolutePath();
    }

}
