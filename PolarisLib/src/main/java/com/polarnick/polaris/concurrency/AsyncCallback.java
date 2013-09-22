package com.polarnick.polaris.concurrency;

/**
 * Date: 22.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public interface AsyncCallback<Result> {

    public void onSuccess(Result result);

}
