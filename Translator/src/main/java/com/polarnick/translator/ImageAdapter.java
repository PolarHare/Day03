package com.polarnick.translator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.polarnick.polaris.concurrency.AsyncCallbackWithFailures;
import com.polarnick.polaris.services.BingSearchService;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Никита
 */
public class ImageAdapter extends BaseAdapter {
    private static final String ACCOUNT_KEY = "cEbtMs0M2NMFC9DQg4TogGtkuorbFQhjaW3dzhQjEVg=";
    private static final int IMAGE_LIMIT = 10;

    private final BingSearchService bingSearchServiceStrict;
    private final Context context;
    private final String query;
    private final int imageSize;
    private final List<Bitmap> images = new ArrayList<Bitmap>();
    private final Handler handler = new Handler();

    public ImageAdapter(Context context, String query) {
        this.context = context;
        this.query = query;

        Resources resources = context.getResources();
        imageSize = (int) resources.getDimension(R.dimen.image_size);

        bingSearchServiceStrict = new BingSearchService(
                ACCOUNT_KEY, imageSize, imageSize, IMAGE_LIMIT, BingSearchService.FilterStrictness.STRICT);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void loadMore(final Runnable onLoadDone) {
        final int offset = images.size();
        bingSearchServiceStrict.searchAsync(query, offset, new AsyncCallbackWithFailures<List<BingSearchService.ResultImage>, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                onLoadDone.run();
            }

            @Override
            public void onSuccess(final List<BingSearchService.ResultImage> resultImages) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String[] urls = new String[resultImages.size()];
                        int cnt = 0;
                        for (BingSearchService.ResultImage image : resultImages) {
                            images.add(null);
                            urls[cnt++] = image.getImageURL();
                        }
                        new ImagesDownloadTask(offset).execute(urls);
                    }
                });
                onLoadDone.run();
            }
        }, null);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
            view.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imageSize));
        }

        ImageView imageView = (ImageView)view.findViewById(R.id.image);
        imageView.setImageBitmap(images.get(position));

        return view;
    }

    private class ImagesDownloadTask extends AsyncTask<String, Bitmap, Void> {
        private int offset;

        private ImagesDownloadTask(int offset) {
            this.offset = offset;
        }

        @Override
        protected Void doInBackground(String... strings) {
            for (String url : strings) {
                try {
                    URLConnection connection = new URL(url).openConnection();
                    publishProgress(BitmapFactory.decodeStream(connection.getInputStream()));
                } catch (Exception e) {
                    Log.w(ImageAdapter.class.getName(), e);
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            images.set(offset++, values[0]);
            notifyDataSetChanged();
        }
    }
}
