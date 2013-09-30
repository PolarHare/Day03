package com.polarnick.polaris.services;


import com.google.common.base.Preconditions;
import com.polarnick.polaris.concurrency.AsyncCallback;
import com.polarnick.polaris.concurrency.AsyncCallbackWithFailures;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Date: 22.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class BingSearchService {

    private static final String ENCODING_CHARSET = "UTF-8";
    /**
     * Arguments: query text(String), image width(int), image height(int), image count(int), filter strictness(String),
     * count of images to be skipped(offset) (int)
     */
    private static final String IMAGE_SEARCH = "https://api.datamarket.azure.com/Bing/Search/v1/Composite" +
            "?Sources=%%27image%%27" +
            "&Query=%%27" + "%s" + "%%27" +//search query
            "&ImageFilters=%%27Size%%3AWidth%%3A" + "%d" + "%%2BSize%%3AHeight%%3A" + "%d" + "%%27" +//image width and height recommendation
            "&$top=" + "%d" +//image count limit
            "&Adult=%%27" + "%s" + "%%27" +//filter strictness
            "&$skip=" + "%d";//offset of images(count of images to be skipped - useful to load packs of images one by one on demand)

    private final String accountKeyEnc;
    private final ExecutorService threadsPool;
    private final int optimalWidth;
    private final int optimalHeight;
    private final int imageLimit;
    private final FilterStrictness filterStrictness;

    public BingSearchService(String accountKey, int optimalWidth, int optimalHeight, int imageLimit, FilterStrictness filterStrictness) {
        this.accountKeyEnc = new String(Base64.encodeBase64((accountKey + ":" + accountKey).getBytes()));
        this.threadsPool = Executors.newCachedThreadPool();
        this.optimalWidth = optimalWidth;
        this.optimalHeight = optimalHeight;
        this.imageLimit = imageLimit;
        this.filterStrictness = filterStrictness;
    }

    public void searchAsync(final String text, final AsyncCallbackWithFailures<List<ResultImage>, Exception> allImagesLoaded,
                            @Nullable final AsyncCallback<ResultImage> nextImageLoaded) {
        searchAsync(text, 0, allImagesLoaded, nextImageLoaded);
    }

    public void searchAsync(final String text, final int offset, final AsyncCallbackWithFailures<List<ResultImage>, Exception> allImagesLoaded,
                            @Nullable final AsyncCallback<ResultImage> nextImageLoaded) {
        Preconditions.checkNotNull(allImagesLoaded, "AllImagesLoaded callback must not be null! Because you must handle possible exceptions.");
        threadsPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ResultImage> resultImages = search(text, offset, nextImageLoaded);
                    allImagesLoaded.onSuccess(resultImages);
                } catch (IOException e) {
                    allImagesLoaded.onFailure(new IOException("Internet connection problem!", e));
                } catch (JSONException e) {
                    allImagesLoaded.onFailure(new IllegalStateException("Problem with JSON Bing API!", e));
                }
            }
        });
    }

    public List<ResultImage> search(String text, @Nullable AsyncCallback<ResultImage> nextImageLoaded) throws IOException, JSONException {
        return search(text, 0, nextImageLoaded);
    }

    public List<ResultImage> search(String text, int offset, @Nullable AsyncCallback<ResultImage> nextImageLoaded) throws IOException, JSONException {
        String url = String.format(IMAGE_SEARCH, URLEncoder.encode(text, ENCODING_CHARSET),
                optimalWidth, optimalHeight, imageLimit, filterStrictness, offset);

        HttpGet getRequest = new HttpGet(url);
        getRequest.setHeader("Authorization", "Basic " + accountKeyEnc);
        getRequest.setHeader("Accept", "application/json");
        HttpResponse httpResponse = new DefaultHttpClient().execute(getRequest);
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(httpResponse.getEntity().getContent(), stringWriter, ENCODING_CHARSET);
        String content = stringWriter.toString();

        JSONArray results = new JSONObject(content).getJSONObject("d").getJSONArray("results");
        List<ResultImage> resultImages = new ArrayList<ResultImage>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject object = results.getJSONObject(i);
            JSONArray images = object.getJSONArray("Image");
            for (int j = 0; j < images.length(); j++) {
                JSONObject image = images.getJSONObject(j);

                String imageUrl = image.getString("MediaUrl");
                int width = image.getInt("Width");
                int height = image.getInt("Height");
                ResultImage resultImage = new ResultImage(imageUrl, width, height);
                resultImages.add(resultImage);

                if (nextImageLoaded != null) {
                    nextImageLoaded.onSuccess(resultImage);
                }
            }
        }
        return resultImages;
    }

    public static class ResultImage {
        private final String imageURL;
        private final int width;
        private final int height;

        public ResultImage(String imageURL, int width, int height) {
            this.imageURL = imageURL;
            this.width = width;
            this.height = height;
        }

        public String getImageURL() {
            return imageURL;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public static enum FilterStrictness {
        STRICT("Strict"),
        MODERATE("Moderate"),
        NONE("Off");

        private final String jsonBingArgument;

        FilterStrictness(String jsonBingArgument) {
            this.jsonBingArgument = jsonBingArgument;
        }

        @Override
        public String toString() {
            return jsonBingArgument;
        }
    }

}