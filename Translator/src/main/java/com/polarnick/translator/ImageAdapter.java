package com.polarnick.translator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.polarnick.polaris.concurrency.AsyncCallback;
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
    private static final int OPTIMAL_WIDTH = 150;
    private static final int OPTIMAL_HEIGHT = 150;
    private static final BingSearchService bingSearchServiceStrict = new BingSearchService(
                ACCOUNT_KEY, OPTIMAL_WIDTH, OPTIMAL_HEIGHT, IMAGE_LIMIT, BingSearchService.FilterStrictness.STRICT);

    private Context context;
    private List<Bitmap> images = new ArrayList<Bitmap>();

    public ImageAdapter(Context context, String query) {
        this.context = context;
        bingSearchServiceStrict.searchAsync(query, null, new AsyncCallback<BingSearchService.ResultImage>() {
            @Override
            public void onSuccess(BingSearchService.ResultImage resultImage) {
                new ImageDownloadTask().execute(resultImage.getImageURL());
            }
        });
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(OPTIMAL_WIDTH, OPTIMAL_HEIGHT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(images.get(position));

        return imageView;
    }

    private class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URLConnection connection = new URL(strings[0]).openConnection();
                return BitmapFactory.decodeStream(connection.getInputStream());
            } catch (Exception e) {
                Log.w(ImageAdapter.class.getName(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            images.add(bitmap);
            notifyDataSetChanged();
        }
    }
}
