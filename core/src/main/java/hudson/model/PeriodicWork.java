/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.model;

import hudson.triggers.SafeTimerTask;
import hudson.triggers.Trigger;
import hudson.ExtensionPoint;
import hudson.Extension;
import hudson.DescriptorExtensionList;
import hudson.ExtensionList;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCM;
import hudson.util.StreamTaskListener;
import hudson.util.NullStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Timer;

/**
 * Extension point to perform a periodic task in Hudson (through {@link Timer}.)
 *
 * <p>
 * This extension point is useful if your plugin needs to perform some work in the background periodically
 * (for example, monitoring, batch processing, garbage collection, etc.)
 *
 * <p>
 * Put {@link Extension} on your class to have it picked up and registered automatically, or
 * manually insert this to {@link Trigger#timer}.
 *
 * <p>
 * This class is designed to run a short task. Implementations whose periodic work takes a long time
 * to run should extend from {@link AsyncPeriodicWork} instead. 
 *
 * @author Kohsuke Kawaguchi
 * @see AsyncPeriodicWork
 */
public abstract class PeriodicWork extends SafeTimerTask implements ExtensionPoint {
    protected final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Gets the number of milliseconds between successive executions.
     *
     * <p>
     * Hudson calls this method once to set up a recurring timer, instead of
     * calling this each time after the previous execution completed. So this class cannot be
     * used to implement a non-regular recurring timer.
     *
     * <p>
     * IOW, the method should always return the same value.
     */
    public abstract long getRecurrencePeriod();

    /**
     * Gets the number of milliseconds til the first execution.
     *
     * <p>
     * By default it chooses the value randomly between 0 and {@link #getRecurrencePeriod()}
     */
    public long getInitialDelay() {
        return Math.abs(new Random().nextLong())%getRecurrencePeriod();
    }

    /**
     * Returns all the registered {@link PeriodicWork}s.
     */
    public static ExtensionList<PeriodicWork> all() {
        return Hudson.getInstance().getExtensionList(PeriodicWork.class);
    }

// time constants
    protected static final long MIN = 1000*60;
    protected static final long HOUR =60*MIN;
    protected static final long DAY = 24*HOUR;
}
