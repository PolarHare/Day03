package com.polarnick.polaris.services;

import com.polarnick.polaris.concurrency.AsyncCallback;
import com.polarnick.polaris.concurrency.AsyncCallbackWithFailures;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date: 22.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class BingSearchServiceTest {

    public static final long MAX_WAIT_MS = 4000;

    public static final String ACCOUNT_KEY = "cEbtMs0M2NMFC9DQg4TogGtkuorbFQhjaW3dzhQjEVg=";
    public static final int IMAGE_LIMIT = 4;
    public static final int OPTIMAL_WIDTH = 400;
    public static final int OPTIMAL_HEIGHT = 400;

    public static final String simpleQuery = "зеленое дерево";
    public static final String queryToBeFiltered = "sex";

    private final BingSearchService bingSearchServiceNoFilter = new BingSearchService(
            ACCOUNT_KEY, OPTIMAL_WIDTH, OPTIMAL_HEIGHT, IMAGE_LIMIT, BingSearchService.FilterStrictness.NONE);
    private final BingSearchService bingSearchServiceStrict = new BingSearchService(
            ACCOUNT_KEY, OPTIMAL_WIDTH, OPTIMAL_HEIGHT, IMAGE_LIMIT, BingSearchService.FilterStrictness.STRICT);

    @Test
    public void testSearch() throws IOException, JSONException {
        final List<BingSearchService.ResultImage> resultImages = bingSearchServiceNoFilter.search(simpleQuery, null);
        Assert.assertEquals(IMAGE_LIMIT, resultImages.size());
        assertEachImage(resultImages);
    }

    @Test
    public void testFilter() throws IOException, JSONException {
        final List<BingSearchService.ResultImage> resultImages = bingSearchServiceStrict.search(queryToBeFiltered, null);
        Assert.assertEquals(0, resultImages.size());
        assertEachImage(resultImages);
    }

    @Test
    public void testSearchImageByImageAsync() throws IOException, JSONException, InterruptedException {
        final AtomicInteger imageDownloaded = new AtomicInteger(0);
        final List<BingSearchService.ResultImage> resultImages =
                bingSearchServiceStrict.search(simpleQuery, new AsyncCallback<BingSearchService.ResultImage>() {
                    @Override
                    public void onSuccess(BingSearchService.ResultImage resultImage) {
                        assertResultImage(resultImage);
                        imageDownloaded.incrementAndGet();
                    }
                });
        Assert.assertEquals(IMAGE_LIMIT, imageDownloaded.get());
        Assert.assertEquals(IMAGE_LIMIT, resultImages.size());
    }

    @Test
    public void testSearchAsync() throws InterruptedException {
        final AtomicInteger imageDownloaded = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(1);
        bingSearchServiceStrict.searchAsync(simpleQuery,
                new AsyncCallbackWithFailures<List<BingSearchService.ResultImage>, Exception>() {

                    @Override
                    public void onSuccess(List<BingSearchService.ResultImage> resultImages) {
                        assertEachImage(resultImages);
                        Assert.assertEquals(imageDownloaded.get(), resultImages.size());
                        Assert.assertEquals(IMAGE_LIMIT, imageDownloaded.get());
                        latch.countDown();
                    }

                    @Override
                    public void onFailure(Exception reason) {
                        Assert.fail();
                    }
                },
                new AsyncCallback<BingSearchService.ResultImage>() {
                    @Override
                    public void onSuccess(BingSearchService.ResultImage resultImage) {
                        assertResultImage(resultImage);
                        imageDownloaded.incrementAndGet();
                    }
                }
        );
        latch.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        Assert.assertEquals(0, latch.getCount());
    }

    private void assertEachImage(List<BingSearchService.ResultImage> resultImages) {
        for (BingSearchService.ResultImage image : resultImages) {
            assertResultImage(image);
        }
    }

    private void assertResultImage(BingSearchService.ResultImage resultImage) {
        Assert.assertNotNull(resultImage);
        Assert.assertNotNull(resultImage.getImageURL());
        Assert.assertNotSame("", resultImage.getImageURL());
        Assert.assertNotSame(0, resultImage.getWidth());
        Assert.assertNotSame(0, resultImage.getHeight());
    }

}
