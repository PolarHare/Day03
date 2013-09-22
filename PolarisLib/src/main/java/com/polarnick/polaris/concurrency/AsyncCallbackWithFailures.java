package com.polarnick.polaris.concurrency;

/**
 * Date: 17.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public interface AsyncCallbackWithFailures<Result, Exception> extends AsyncCallback<Result> {

    public void onFailure(Exception reason);

}
