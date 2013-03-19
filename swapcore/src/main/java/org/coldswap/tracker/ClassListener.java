package org.coldswap.tracker;


import net.contentobjects.jnotify.JNotifyListener;

import java.io.File;
import java.io.FileFilter;

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
 * 5:53 PM       3/17/13
 */

/**
 * Listen for any class file modifications and instrument
 * JVM class loader to reload any modification that has been
 * made to the byte code.
 */
public class ClassListener implements JNotifyListener {
    private final FileFilter filter = new ClassFileFilter();

    @Override
    public void fileCreated(int i, String s, String s2) {
        if (filter.accept(new File(s2))) {
            System.out.println("New File Created:");
            System.out.println("watchID:" + String.valueOf(i));
            System.out.println("RootPath:" + s);
            System.out.println("Name:" + s2);
        }
    }

    @Override
    public void fileDeleted(int i, String s, String s2) {
        if (filter.accept(new File(s2))) {
            System.out.println("File Deleted:");
            System.out.println("watchID:" + String.valueOf(i));
            System.out.println("RootPath:" + s);
            System.out.println("Name:" + s2);
        }
    }

    @Override
    public void fileModified(int i, String s, String s2) {
        if (filter.accept(new File(s2))) {
            System.out.println("File Modified:");
            System.out.println("watchID:" + String.valueOf(i));
            System.out.println("RootPath:" + s);
            System.out.println("Name:" + s2);
        }
    }

    @Override
    public void fileRenamed(int i, String s, String s2, String s3) {
        if (filter.accept(new File(s3))) {
            System.out.println("File Renamed:");
            System.out.println("watchID:" + String.valueOf(i));
            System.out.println("RootPath:" + s);
            System.out.println("Old Name:" + s2);
            System.out.println("New Name:" + s2);
        }
    }
}
