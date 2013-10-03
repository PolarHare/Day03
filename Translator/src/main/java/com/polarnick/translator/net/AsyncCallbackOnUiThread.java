package com.polarnick.translator.net;

import android.os.Handler;
import com.polarnick.polaris.concurrency.AsyncCallbackWithFailures;

/**
 * @author Никита
 */
public abstract class AsyncCallbackOnUiThread<Result, Exception> implements AsyncCallbackWithFailures<Result, Exception> {
    private final Handler handler = new Handler();

    protected AsyncCallbackOnUiThread() {}

    public abstract void fail(Exception reason);

    public abstract void success(Result result);

    @Override
    public final void onFailure(final Exception reason) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                fail(reason);
            }
        });
    }

    @Override
    public final void onSuccess(final Result result) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                success(result);
            }
        });
    }
}
