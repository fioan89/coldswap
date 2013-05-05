package org.coldswap.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

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
 * 8:52 PM       3/20/13
 */

public class ClassInstrumenter {
    private static final ClassInstrumenter instrumenter = new ClassInstrumenter();
    private Instrumentation inst;
    private final Map<String, Class<?>> loadedMap;

    private ClassInstrumenter() {
        this.loadedMap = new HashMap<String, Class<?>>();
    }

    public static ClassInstrumenter getInstance() {
        return instrumenter;
    }

    public Instrumentation getInstrumenter() {
        return inst;
    }

    public void setInstrumenter(Instrumentation inst) {
        this.inst = inst;
    }

    public Map<String, Class<?>> getLoadedClasses() throws NullPointerException {
        if (this.loadedMap.size() <= 0) {
            if (this.inst != null) {
                Class<?>[] classes = inst.getAllLoadedClasses();
                for (Class<?> cls : classes) {
                    this.loadedMap.put(cls.getName(), cls);
                }
            } else {
                throw new NullPointerException("Parameter inst is null! You must call first setInstrumenter() method!");
            }
        }
        return loadedMap;

    }


}
