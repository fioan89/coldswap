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
 * 9:01 PM       4/1/13
 */

import java.io.*;
import java.util.logging.Logger;

/**
 * Loads the bytecode of a class.
 */

public class BytecodeClassLoader {
    private final static Logger logger = Logger.getLogger(BytecodeClassLoader.class.getName());

    /**
     * Loads the byte file of a class file.
     *
     * @param path file path
     * @return the byte array of .class file
     */
    public static byte[] loadClassBytes(String path) {
        InputStream in = null;
        ByteArrayOutputStream bao = null;
        byte[] ret = null;
        try {
            in = new FileInputStream(new File(path));
            bao = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int counter;
            while ((counter = in.read(buffer)) != -1) {
                bao.write(buffer, 0, counter);
            }

        } catch (FileNotFoundException e) {
            logger.severe(e.toString());
        } catch (IOException e) {
            logger.severe(e.toString());
        } finally {
            if (bao != null) {
                ret = bao.toByteArray();
            }
            try {
                in.close();
                bao.close();
            } catch (IOException e) {
                // ignore
            }

        }
        return ret;
    }
}
