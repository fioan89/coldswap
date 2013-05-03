package org.coldswap.transformer;

import org.coldswap.asm.ReferenceReplacer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Vector;

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
 * 5:08 PM       4/30/13
 */

public class ReferenceReplacerManager {
    private final static ReferenceReplacerManager ourInstance = new ReferenceReplacerManager();
    private final List<ReferenceReplacer> fieldReferences = new Vector<ReferenceReplacer>();

    public static ReferenceReplacerManager getInstance() {
        return ourInstance;
    }

    private ReferenceReplacerManager() {
    }

    /**
     * Registers a field reference that would be replaced.
     *
     * @param replacer field reference replacer.
     */
    public void registerFieldReferenceReplacer(ReferenceReplacer replacer) {
        fieldReferences.add(replacer);
    }

    /**
     * Starts the proces of find and replace for the given class node.
     *
     * @param clazz where to find and replace.
     * @return transformed class byte array.
     */
    public byte[] runReferenceReplacer(byte[] clazz) {
        ClassNode classNode = new ClassNode(Opcodes.ASM4);
        ClassReader classReader = new ClassReader(clazz);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classReader.accept(classNode, 0);
        // replace public fields
        for (ReferenceReplacer replacer : fieldReferences) {
            replacer.findAndReplace(classNode);
        }

        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
