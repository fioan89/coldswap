package org.fioan89.coldswap.dummy.tests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

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
 * Created with IntelliJ IDEA.
 * User: faur
 * Date: 6/27/13
 * Time: 4:09 PM
 */
public class TestRunner {
    private static final int DELAY = 3000;

    public static void testPrivateNewStaticField() {
        int total = TestNewPrivateStaticField.METHODNUMBER;
        int counter = 0;

        System.out.println("===================================================================================");
        System.out.println("           New Private Static Field Insertion Tests                                ");
        System.out.println("===================================================================================");

        Result result = JUnitCore.runClasses(TestNewPrivateStaticField.class);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
            counter++;
        }

        System.out.println("Successful tests:" + result.wasSuccessful());
        System.out.println("Failed:" + counter + " out of " + total);
        System.out.println("Successful:" + (total - counter) + " out of " + total);
        System.out.println("===================================================================================");


    }

    public static void main(String[] args) {
        // wait a few seconds until coldswap is running
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testPrivateNewStaticField();
    }
}
