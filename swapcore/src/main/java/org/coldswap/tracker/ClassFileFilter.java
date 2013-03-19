package org.coldswap.tracker;

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
 * 7:01 PM       3/17/13
 */

/**
 * A file filter for {@link ClassListener}
 */
public class ClassFileFilter implements FileFilter {
    private final String[] acceptedExt = new String[]{"class"};

    @Override
    public boolean accept(File file) {
        for (String ext : acceptedExt) {
            if (file.getName().endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
