package com.polarnick.polaris.concurrency;

/**
 * Date: 17.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public abstract class AsyncProgressCallback {

    private double fullProgress = 0;
    private final Object lock = new Object();

    public void registerProgressPassed(double progressDelta) {
        synchronized (lock) {
            fullProgress += progressDelta;
            progressPassed(progressDelta, fullProgress);
        }
    }

    /**
     * @param progressDelta from <b>0.0</b> to <b>1.0</b> - shows the progress of executing task passed from previous
     *                      progressPassed call.
     * @param fullProgress  show the progress of task progress. <b>0.0<b/> shows that it was only started. <b>1.0<b/> -
     *                      the task was finished.
     */
    public abstract void progressPassed(double progressDelta, double fullProgress);

}
