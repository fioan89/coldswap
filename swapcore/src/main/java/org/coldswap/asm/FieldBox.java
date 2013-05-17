package org.coldswap.asm;

import org.objectweb.asm.tree.FieldNode;

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
 * 4:43 PM       4/30/13
 */

/**
 * Contains info about a field that need to be searched and replaced.
 */
public class FieldBox {

    protected final FieldNode fieldToReplace;
    protected final String oldClass;
    protected final String newClass;

    /**
     * Constructs a container for a field reference that should be searched and replaced.
     *
     * @param oldClass       the old class reference.
     * @param fieldToReplace the field that should be instrumented.
     * @param newClass       the new class reference for the field.
     */
    public FieldBox(String oldClass, FieldNode fieldToReplace, String newClass) {
        this.oldClass = oldClass;
        this.fieldToReplace = fieldToReplace;
        this.newClass = newClass;
    }

}
