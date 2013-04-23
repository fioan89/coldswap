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
 * 3:59 PM       4/1/13
 */

/**
 * Transform classes before they are loaded by a class loader
 */
public interface ASMClassLoadTransformer {

    /**
     * Transforms a class.
     *
     * @param classNode A {@link org.objectweb.asm.tree.ClassNode} containing the class object model.
     * @return a value greater or equal if it was
     *         successfully, negative otherwise.
     */
    public int transformClass(ClassNode classNode);
}
