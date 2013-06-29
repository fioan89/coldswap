package org.coldswap.agent;

import org.coldswap.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
 * 7:51 PM       3/17/13
 */

/**
 * Parse agent args and build a map from the name of the argument and it's values.
 * The following list of args can be used by the agent:
 * <p>
 * <h1>cp</h1>- a list of folders where coldswap will search for classes.
 * Ex: cp=home/user/Public:/home/user/Documents/:/home/user/Templates
 * </p>
 * <p>
 * <h1>recursive</h1> - <code>true</code> if you want coldswap to watch recursive in the folders, <code>false</code> otherwise. By
 * default this value is false.
 * Ex: recursive=true
 * </p>
 * <p/>
 * Argument's are separated by ",".
 */
class AgentArgsParser {
    private final Map<String, Object> opts;
    private final String args;

    public AgentArgsParser(String args) {
        this.args = args;
        this.opts = new HashMap<String, Object>();
        this.opts.put("recursive", "false");
        this.opts.put("maxNumberOfMethods", 10);
        this.opts.put("logLevel", Level.ALL);
    }

    /**
     * Builds a map provided options and their corresponding list of values.
     *
     * @throws IllegalArgumentException
     */
    public void buildArgs() throws IllegalArgumentException {
        if (args.length() <= 0) {
            throw new IllegalArgumentException("No argument has been passed to ColdSwap\n");
        } else {
            String[] opts = this.args.split(",");
            for (String opt : opts) {
                String[] hashes = opt.split("=");
                if ("cp".equals(hashes[0])) {
                    String[] paths = hashes[1].split(":");
                    this.opts.put("cp", paths);
                } else if ("recursive".equals(hashes[0])) {
                    if ("true".equals(hashes[1])) {
                        this.opts.put("recursive", "true");
                    }
                } else if ("maxNumberOfMethods".equals(hashes[0])) {
                    int methodsNumber = Integer.valueOf((hashes[1]));
                    if (methodsNumber > Constants.MAX_METHODS) {
                        methodsNumber = Constants.MAX_METHODS;
                    }
                    this.opts.put("maxNumberOfMethods", methodsNumber);
                } else if ("logLevel".equals(hashes[0])) {
                    String level = hashes[1].toUpperCase();
                    if ("SEVERE".equals(level)) {
                        this.opts.put("logLevel", Level.SEVERE);
                    } else if ("WARNING".equals(level)) {
                        this.opts.put("logLevel", Level.WARNING);
                    } else if ("INFO".equals(level)) {
                        this.opts.put("logLevel", Level.INFO);
                    }

                } else {
                    throw new IllegalArgumentException("Illegal argument:" + hashes[0] + "\n");
                }
            }
        }
    }

    /**
     * Returns the corresponding value for an argument. If argName is not valid,
     * null is returned.
     *
     * @param argName the name of the argument
     * @return argument value.
     */
    public Object getArgument(String argName) {
        return this.opts.get(argName);
    }

}
