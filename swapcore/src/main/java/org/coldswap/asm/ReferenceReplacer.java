package org.coldswap.asm;

import org.objectweb.asm.tree.ClassNode;

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
 * 4:40 PM       4/30/13
 */


public interface ReferenceReplacer {
    /**
     * Finds and replace a reference in the bytecode.
     *
     * @param classNode class node that contains the references who should be replaced.
     * @return the number of references found and replaced.
     */
    public int findAndReplace(ClassNode classNode);
}
