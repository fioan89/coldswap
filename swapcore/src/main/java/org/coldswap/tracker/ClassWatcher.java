package org.coldswap.tracker;

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
 * 6:07 PM       3/17/13
 */

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Start watching the specified directory and if any modifications occur, than
 * notify a listener.
 */
public class ClassWatcher implements Runnable {
    private String path = System.getProperty("user.home");
    private boolean watchSubtree;
    private int mask;
    private int watchID;
    private boolean alive;
    private static final Logger logger = Logger.getLogger(ClassWatcher.class.getName());

    /**
     * Watch this directory for any modifications like file removing,
     * adding or modifying files
     *
     * @param path The selected directory to watch
     */
    public ClassWatcher(String path) {
        // check firs if this path is a directory, if not, than it is a file
        // therefore we can extract a directory location from the file
        File f = new File(path);
        if (!f.isDirectory()) {
            path = f.getParentFile().getName();
        }
        this.path = path;
        // what kind of modification this watcher should notify
        this.mask = JNotify.FILE_ANY;
        this.alive = false;
        logger.setLevel(Level.ALL);
    }

    /**
     * Watch this directory and the subtree directories, for any modifications like file removing,
     * adding or modifying files
     *
     * @param path         The selected directory to watch
     * @param watchSubtree if <code>true</code> it will watch for any file modification in
     *                     the subtree, <code>false</code> otherwise
     */
    public ClassWatcher(String path, boolean watchSubtree) {
        this(path);
        this.watchSubtree = watchSubtree;
    }

    synchronized private void startWatcher() {
        try {
            watchID = JNotify.addWatch(path, mask, watchSubtree, new ClassListener());
            String msg = watchSubtree ? " and it's subfolders.\n" : "\n";
            logger.info("Starting to watch:" + path + msg);
        } catch (JNotifyException e) {
            logger.severe("ColdSwap can't watch folder:" + path + "\n" + e.toString());
        }
        while (alive) {
            try {
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                // just ignore
            }
        }
    }

    /**
     * Stop this daemon thread from watching directories
     */
    public synchronized void stopWatcher() {
        this.alive = false;
        try {
            JNotify.removeWatch(this.watchID);
        } catch (JNotifyException e) {
            // just ignore
        }
    }

    public void run() {
        this.alive = true;
        startWatcher();
    }
}
